package views;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.kestutis.cargauges.R;

import java.util.Locale;

import static constants.Constants.*;

//https://www.codeproject.com/Articles/820615/Android-Create-rotating-Needle

public class GaugeView extends View {

    // *--------------------------------------------------------------------- *//
    // Customizable properties
    // *--------------------------------------------------------------------- *//

    private boolean mShowScale;
    private boolean mShowRanges;

    private float mNeedleWidth;
    private float mNeedleHeight;

    private float mScalePosition;
    private float mScaleStartValue;
    private float mScaleEndValue;
    private float mScaleStartAngle;
    private float[] mRangeValues;

    private int[] mRangeColors;
    private int mDivisions;
    private int mSubdivisions;

    private RectF mScaleRect;

    private Bitmap mBackground;
    private Paint mBackgroundPaint;
    private Paint[] mRangePaints;
    private Paint mNeedleRightPaint;
    private Paint mNeedleLeftPaint;
    private Paint mNeedleScrewPaint;

    private Path mNeedleRightPath;
    private Path mNeedleLeftPath;

    // *--------------------------------------------------------------------- *//

    private float mScaleRotation;
    private float mDivisionValue;
    private float mSubdivisionValue;
    private float mSubdivisionAngle;

    private float mTargetValue;
    private float mCurrentValue;

    private float mNeedleVelocity;
    private float mNeedleAcceleration;
    private long mNeedleLastMoved = -1;
    private boolean mNeedleInitialized;

    public GaugeView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        readAttrs(context, attrs, defStyle);
        init();
    }

    public GaugeView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GaugeView(final Context context) {
        this(context, null, 0);
    }

    private void readAttrs(final Context context, final AttributeSet attrs, final int defStyle) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GaugeView, defStyle, 0);
        mShowScale = a.getBoolean(R.styleable.GaugeView_showScale, SHOW_SCALE);
        mShowRanges = a.getBoolean(R.styleable.GaugeView_showRanges, SHOW_RANGES);

        mNeedleWidth = a.getFloat(R.styleable.GaugeView_needleWidth, NEEDLE_WIDTH);
        mNeedleHeight = a.getFloat(R.styleable.GaugeView_needleHeight, NEEDLE_HEIGHT);

        mScalePosition = (mShowScale || mShowRanges) ? a.getFloat(R.styleable.GaugeView_scalePosition, SCALE_POSITION) : 0.0f;
        mScaleStartValue = a.getFloat(R.styleable.GaugeView_scaleStartValue, SCALE_START_VALUE);
        mScaleEndValue = a.getFloat(R.styleable.GaugeView_scaleEndValue, SCALE_END_VALUE);
        mScaleStartAngle = a.getFloat(R.styleable.GaugeView_scaleStartAngle, SCALE_START_ANGLE);

        mDivisions = a.getInteger(R.styleable.GaugeView_divisions, SCALE_DIVISIONS);
        mSubdivisions = a.getInteger(R.styleable.GaugeView_subdivisions, SCALE_SUBDIVISIONS);

        if (mShowRanges) {
            final int rangesId = a.getResourceId(R.styleable.GaugeView_rangeValues, 0);
            final int colorsId = a.getResourceId(R.styleable.GaugeView_rangeColors, 0);
            readRanges(context.getResources(), rangesId, colorsId);
        }

        a.recycle();
    }

    private void readRanges(final Resources res, final int rangesId, final int colorsId) {
        if (rangesId > 0 && colorsId > 0) {
            final String[] ranges = res.getStringArray(R.array.ranges);
            final String[] colors = res.getStringArray(R.array.rangeColors);
            if (ranges.length != colors.length) {
                throw new IllegalArgumentException(
                        "The ranges and colors arrays must have the same length.");
            }

            final int length = ranges.length;
            mRangeValues = new float[length];
            mRangeColors = new int[length];
            for (int i = 0; i < length; i++) {
                mRangeValues[i] = Float.parseFloat(ranges[i]);
                mRangeColors[i] = Color.parseColor(colors[i]);
            }
        } else {
            mRangeValues = RANGE_VALUES;
            mRangeColors = RANGE_COLORS;
        }
    }

    private void init() {
        // TODO Why isn't this working with HA layer?
        // The needle is not displayed although the onDraw() is being triggered by invalidate() calls.

        initDrawingRects();
        initDrawingTools();

        // Compute the scale properties
        if (mShowRanges) {
            initScale();
        }
    }

    public void initDrawingRects() {
        // The drawing area is a rectangle of width 1 and height 1,
        // where (0,0) is the top left corner of the canvas.
        // Note that on Canvas X axis points to right, while the Y axis points downwards.
        RectF _faceRect = new RectF(LEFT, TOP, RIGHT, BOTTOM);

        mScaleRect = new RectF(_faceRect.left + mScalePosition, _faceRect.top + mScalePosition, _faceRect.right - mScalePosition,
                _faceRect.bottom - mScalePosition);
    }

    private void initDrawingTools() {
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setFilterBitmap(true);

        if (mShowRanges) {
            setDefaultScaleRangePaints();
        }

        setDefaultNeedlePaths();
        mNeedleLeftPaint = getDefaultNeedleLeftPaint();
        mNeedleRightPaint = getDefaultNeedleRightPaint();
        mNeedleScrewPaint = getDefaultNeedleScrewPaint();

    }

    public void setDefaultNeedlePaths() {
        final float x = 0.5f, y = 0.5f;
        mNeedleLeftPath = new Path();
        mNeedleLeftPath.moveTo(x, y);
        mNeedleLeftPath.lineTo(x - mNeedleWidth, y);
        mNeedleLeftPath.lineTo(x, y - mNeedleHeight);
        mNeedleLeftPath.lineTo(x, y);
        mNeedleLeftPath.lineTo(x - mNeedleWidth, y);

        mNeedleRightPath = new Path();
        mNeedleRightPath.moveTo(x, y);
        mNeedleRightPath.lineTo(x + mNeedleWidth, y);
        mNeedleRightPath.lineTo(x, y - mNeedleHeight);
        mNeedleRightPath.lineTo(x, y);
        mNeedleRightPath.lineTo(x + mNeedleWidth, y);
    }

    public Paint getDefaultNeedleLeftPaint() {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(0, 0, 0));
        return paint;
    }

    public Paint getDefaultNeedleRightPaint() {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(0, 0, 0));
        return paint;
    }

    public Paint getDefaultNeedleScrewPaint() {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(0, 0, 0));
        return paint;
    }

    public void setDefaultScaleRangePaints() {
        final int length = mRangeValues.length;
        mRangePaints = new Paint[length];
        for (int i = 0; i < length; i++) {
            mRangePaints[i] = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            mRangePaints[i].setColor(mRangeColors[i]);
            mRangePaints[i].setStyle(Paint.Style.STROKE);
            mRangePaints[i].setStrokeWidth(0.005f);
            mRangePaints[i].setTextSize(0.05f);
            mRangePaints[i].setTypeface(Typeface.SANS_SERIF);
            mRangePaints[i].setTextAlign(Align.CENTER);
            mRangePaints[i].setShadowLayer(0.005f, 0.002f, 0.002f, 0);
        }
    }

    @Override
    protected void onRestoreInstanceState(final Parcelable state) {
        final Bundle bundle = (Bundle) state;
        final Parcelable superState = bundle.getParcelable("superState");
        super.onRestoreInstanceState(superState);

        mNeedleInitialized = bundle.getBoolean("needleInitialized");
        mNeedleVelocity = bundle.getFloat("needleVelocity");
        mNeedleAcceleration = bundle.getFloat("needleAcceleration");
        mNeedleLastMoved = bundle.getLong("needleLastMoved");
        mCurrentValue = bundle.getFloat("currentValue");
        mTargetValue = bundle.getFloat("targetValue");
    }

    private void initScale() {
        mScaleRotation = (mScaleStartAngle + 180) % 360;
        mDivisionValue = (mScaleEndValue - mScaleStartValue) / mDivisions;
        Log.d("mDivisionValue:", String.valueOf(mDivisionValue));
        mSubdivisionValue = mDivisionValue / mSubdivisions;
        mSubdivisionAngle = (360 - 2 * mScaleStartAngle) / (mDivisions * mSubdivisions);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();

        final Bundle state = new Bundle();
        state.putParcelable("superState", superState);
        state.putBoolean("needleInitialized", mNeedleInitialized);
        state.putFloat("needleVelocity", mNeedleVelocity);
        state.putFloat("needleAcceleration", mNeedleAcceleration);
        state.putLong("needleLastMoved", mNeedleLastMoved);
        state.putFloat("currentValue", mCurrentValue);
        state.putFloat("targetValue", mTargetValue);
        return state;
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        final int chosenWidth = chooseDimension(widthMode, widthSize);
        final int chosenHeight = chooseDimension(heightMode, heightSize);
        setMeasuredDimension(chosenWidth, chosenHeight);
    }

    private int chooseDimension(final int mode, final int size) {
        switch (mode) {
            case View.MeasureSpec.AT_MOST:
            case View.MeasureSpec.EXACTLY:
                return size;
            case View.MeasureSpec.UNSPECIFIED:
            default:
                return getDefaultDimension();
        }
    }

    private int getDefaultDimension() {
        return SIZE;
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        drawGauge();
    }

    private void drawGauge() {
        if (null != mBackground) {
            // Let go of the old background
            mBackground.recycle();
        }

        // Create a new background according to the new width and height
        mBackground = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(mBackground);
        final float scale = Math.min(getWidth(), getHeight());
        canvas.scale(scale, scale);
        canvas.translate((scale == getHeight()) ? ((getWidth() - scale) / 2) / scale : 0
                , (scale == getWidth()) ? ((getHeight() - scale) / 2) / scale : 0);

        if (mShowRanges) {
            drawScale(canvas);
        }
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        drawBackground(canvas);

        final float scale = Math.min(getWidth(), getHeight());
        canvas.scale(scale, scale);
        canvas.translate((scale == getHeight()) ? ((getWidth() - scale) / 2) / scale : 0
                , (scale == getWidth()) ? ((getHeight() - scale) / 2) / scale : 0);

        drawNeedle(canvas);
        computeCurrentValue();
    }

    private void drawBackground(final Canvas canvas) {
        if (null != mBackground) {
            canvas.drawBitmap(mBackground, 0, 0, mBackgroundPaint);
        }
    }

    private void drawScale(final Canvas canvas) {
        canvas.save();
        // On canvas, North is 0 degrees, East is 90 degrees, South is 180 etc.
        // We start the scale somewhere South-West so we need to first rotate the canvas.
        canvas.rotate(mScaleRotation, 0.5f, 0.5f);

        final int totalTicks = mDivisions * mSubdivisions + 1;
        for (int i = 0; i < totalTicks; i++) {
            final float y1 = mScaleRect.top;
            Log.d("mScaleRect.top: ", String.valueOf(mScaleRect.top));

            final float y2 = y1 + 0.045f; // height of division
            final float y3 = y1 + 0.090f; // height of subdivision

            final float value = getValueForTick(i);
            final Paint paint = getRangePaint(value);

            float div = mScaleEndValue / (float) mDivisions;
            float mod = value % div;
            if ((Math.abs(mod - 0) < 0.001) || (Math.abs(mod - div) < 0.001)) {
                // Draw a division tick
                paint.setStrokeWidth(0.01f);
                paint.setColor(Color.rgb(87,97,114));
                canvas.drawLine(0.5f, y1 - 0.015f, 0.5f, y3 - 0.03f, paint);
                // Draw the text 0.15 away from the division tick
                //paint.setTextSize(1F);
                paint.setStyle(Paint.Style.FILL);
                //canvas.drawText(valueString(value), 0.49f, y3 + 0.05f, paint);
                drawText(canvas, String.format(Locale.US, "%d", (int) value), 0.5f, y3 + 0.05f, paint);
            } else {
                // Draw a subdivision tick
                paint.setStrokeWidth(0.002f);
                paint.setColor(Color.rgb(209,209,209));
                canvas.drawLine(0.5f, y1, 0.5f, y2, paint);
            }
            canvas.rotate(mSubdivisionAngle, 0.5f, 0.5f);
            Log.d("mSubdivisionAngle: ", String.valueOf(mSubdivisionAngle));
        }
        canvas.restore();
    }

    private void drawText(Canvas canvas, String value, float x, float y, Paint paint) {
        //Save original font size
        float originalTextSize = paint.getTextSize();

        // set a magnification factor
        final float magnifier = 100f;

        // Scale the canvas
        canvas.save();
        canvas.scale(1f / magnifier, 1f / magnifier);

        // increase the font size
        paint.setTextSize(originalTextSize * magnifier);

        canvas.drawText(value, x*magnifier, y*magnifier,paint);

        // bring everything back to normal
        canvas.restore();
        paint.setTextSize(originalTextSize);
    }

    private float getValueForTick(final int tick) {
        return tick * (mDivisionValue / mSubdivisions);
    }

    private Paint getRangePaint(final float value) {
        final int length = mRangeValues.length;

        for (int i = 0; i < length - 1; i++) {
            if (value < mRangeValues[i]) {
                return mRangePaints[i];
            }
        }

        if (value <= mRangeValues[length - 1]) {
            return mRangePaints[length - 1];
        }

        throw new IllegalArgumentException("Value " + value + " out of range!");
    }

    private void drawNeedle(final Canvas canvas) {
        if (mNeedleInitialized) {
            final float angle = getAngleForValue(mCurrentValue);
            // Logger.log.info(String.format("value=%f -> angle=%f", mCurrentValue, angle));

            canvas.save();
            canvas.rotate(angle, 0.5f, 0.5f);

            setNeedleShadowPosition(angle);
            canvas.drawPath(mNeedleLeftPath, mNeedleLeftPaint);
            canvas.drawPath(mNeedleRightPath, mNeedleRightPaint);

            canvas.restore();

            // Draw the needle screw and its border

            canvas.drawCircle(0.5f, 0.5f, 0.04f, mNeedleScrewPaint);

            Log.d("NEEDLE", "Needle drawn :/");
            //canvas.drawCircle(0.5f, 0.5f, 0.04f, mNeedleScrewBorderPaint);
        }
    }

    private void setNeedleShadowPosition(final float angle) {
        if (angle > 180 && angle < 360) {
            // Move shadow from right to left
            mNeedleRightPaint.setShadowLayer(0, 0, 0, Color.BLACK);
            mNeedleLeftPaint.setShadowLayer(0.01f, -0.005f, 0.005f, Color.argb(127, 0, 0, 0));
        } else {
            // Move shadow from left to right
            mNeedleLeftPaint.setShadowLayer(0, 0, 0, Color.BLACK);
            mNeedleRightPaint.setShadowLayer(0.01f, 0.005f, -0.005f, Color.argb(127, 0, 0, 0));
        }
    }

    private float getAngleForValue(final float value) {
        return (mScaleRotation + (value / mSubdivisionValue) * mSubdivisionAngle) % 360;
    }

    private void computeCurrentValue() {
        if (!(Math.abs(mCurrentValue - mTargetValue) > 0.01f)) {
            return;
        }

        if (-1 != mNeedleLastMoved) {
            final float time = (System.currentTimeMillis() - mNeedleLastMoved) / 1000.0f;
            final float direction = Math.signum(mNeedleVelocity);
            if (Math.abs(mNeedleVelocity) < 90.0f) {
                mNeedleAcceleration = 5.0f * (mTargetValue - mCurrentValue);
            } else {
                mNeedleAcceleration = 0.0f;
            }

            mNeedleAcceleration = 5.0f * (mTargetValue - mCurrentValue);
            mCurrentValue += mNeedleVelocity * time;
            mNeedleVelocity += mNeedleAcceleration * time;

            if ((mTargetValue - mCurrentValue) * direction < 0.01f * direction){
                mCurrentValue = mTargetValue;
                mNeedleVelocity = 0.0f;
                mNeedleAcceleration = 0.0f;
                mNeedleLastMoved = -1L;
            } else {
                mNeedleLastMoved = System.currentTimeMillis();
            }

            invalidate();

        } else {
            mNeedleLastMoved = System.currentTimeMillis();
            computeCurrentValue();
        }
    }

    public void setTargetValue(final float value) {
        if (mShowScale || mShowRanges) {
            if (value < mScaleStartValue) {
                mTargetValue = mScaleStartValue;
            } else if (value > mScaleEndValue) {
                mTargetValue = mScaleEndValue;
            } else {
                mTargetValue = value;
            }
        } else {
            mTargetValue = value;
        }
        mNeedleInitialized = true;
        invalidate();
    }

}

package views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.kestutis.cargauges.R;

import java.util.Locale;

import constants.Constants;
import constants.Enums;

/**
 * View can only be used in 3:2 ratio when its in horizontal mode or
 * in 2:3 ratio when in vertical mode. Round is a square view*/

public class GaugeView extends View {

    private Paint _linePaint;
    private Path _linePath;

    private Bitmap _background;
    private Paint _backgroundPaint;

    private int _divisions;
    private int _subDivisions;

    private RectF _scaleRect;
    private float _scalePosition;
    private float _scaleStartValue;
    private float _scaleEndValue;

    private int _gaugeRedSide;
    private int _redDivisions;

    private float _divisionValue;

    private float _needleCenterCoordinatesX;
    private float _needleCenterCoordinatesY;

    private boolean _showScaleValues;

    private float _newValue = 0.0f;
    private float _oldValue = 0.0f;

    private Matrix _matrix;

    public GaugeView(Context context) {
        this(context, null, 0);
    }

    public GaugeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GaugeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        readAttrs(context, attrs, defStyleAttr);
        _matrix = new Matrix();
        this.postInvalidate();
        init();
    }

    private void readAttrs(final Context context, final AttributeSet attrs, final int defStyle) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GaugeView, defStyle, 0);
        _showScaleValues = a.getBoolean(R.styleable.GaugeView_showScaleValues, Constants.SHOW_SCALE);

        _scalePosition = _showScaleValues ? a.getFloat(R.styleable.GaugeView_scalePosition, Constants.SCALE_POSITION) : 0.0f;
        _scaleStartValue = a.getFloat(R.styleable.GaugeView_scaleStartValue, Constants.SCALE_START_VALUE);
        _scaleEndValue = a.getFloat(R.styleable.GaugeView_scaleEndValue, Constants.SCALE_END_VALUE);

        _divisions = a.getInteger(R.styleable.GaugeView_divisions, Constants.SCALE_DIVISIONS);
        _subDivisions = a.getInteger(R.styleable.GaugeView_subdivisions, Constants.SCALE_SUBDIVISIONS);

        _gaugeRedSide = a.getInteger(R.styleable.GaugeView_gaugeRedSide, Constants.GAUGE_RED_SIDE);
        _redDivisions = a.getInteger(R.styleable.GaugeView_redDivisions, Constants.SCALE_RED_DIVISIONS);

        a.recycle();
    }

    private void init(){

        RectF _faceRect = new RectF(Constants.LEFT, Constants.TOP, Constants.RIGHT, Constants.BOTTOM);
        _scaleRect = new RectF(_faceRect.left + _scalePosition,
                _faceRect.top + _scalePosition,
                _faceRect.right - _scalePosition,
                _faceRect.bottom - _scalePosition);

        _backgroundPaint = new Paint();
        _backgroundPaint.setFilterBitmap(true);

        _linePaint = new Paint();
        _linePaint.setColor(Color.BLACK/*Color.rgb(87,97,114)*/); // Set the color
        _linePaint.setStyle(Paint.Style.FILL_AND_STROKE); // set the border and fills the inside of needle
        _linePaint.setAntiAlias(true);
        _linePaint.setStrokeWidth(5.0f); // width of the border
        _linePaint.setShadowLayer(8.0f, 0.1f, 0.1f, Color.GRAY); // Shadow of the needle

        Paint needleScrewPaint = new Paint();
        needleScrewPaint.setColor(Color.BLACK);
        needleScrewPaint.setAntiAlias(true);
        needleScrewPaint.setShader(new RadialGradient(130.0f, 50.0f, 10.0f,
                Color.DKGRAY, Color.BLACK, Shader.TileMode.CLAMP));

        _divisionValue = (_scaleEndValue - _scaleStartValue) / _divisions;
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

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        drawGauge(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (null != _background) {
            canvas.drawBitmap(_background, 0, 0, _backgroundPaint);
        }

        canvas.concat(_matrix);
        canvas.drawPath(_linePath, _linePaint);

        _matrix.postRotate(countDegrees(), _needleCenterCoordinatesX, _needleCenterCoordinatesY);

        invalidate();
    }


    public void setValue(float value) {
        _newValue = value;
        invalidate();
    }

    private float countDegrees(){

        if (_newValue > _oldValue && _newValue - _oldValue >= 2){
            _oldValue += 2;
            Log.d("oldValue: " + String.valueOf(_oldValue), "newValue" + String.valueOf(_newValue));
            return  2.0f;
        } else if (_newValue <= _oldValue && _newValue - _oldValue <= -2){
            _oldValue -= 2;
            Log.d("oldValue: " + String.valueOf(_oldValue), "newValue" + String.valueOf(_newValue));
            return  -2.0f;
        } else {
            return 0.0f;
        }
    }

    private int chooseDimension(final int mode, final int size) {
        switch (mode) {
            case View.MeasureSpec.AT_MOST:
            case View.MeasureSpec.EXACTLY:
                return size;
            case View.MeasureSpec.UNSPECIFIED:
            default:
                return Constants.SIZE;
        }
    }

    private void drawGauge(int width, int height) {
        if (null != _background) {
            _background.recycle();
        }

        // Create a new background according to the new width and height
        _background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(_background);
        final float scale = Math.max(width, height);
        canvas.scale(scale, scale);
        canvas.translate((scale == height) ? ((width - scale) / 2) / scale : 0
                , (scale == width) ? ((height - scale) / 2) / scale : 0);

        drawScale(canvas);
        drawNeedle(width, height);
    }

    private void drawScale(final Canvas canvas) {
        boolean isDivisionTick;

        canvas.save();
        canvas.rotate(260 % 360, 0.58f, 0.60f);

        final int totalTicks = _divisions * _subDivisions + 1;
        for (int i = 0; i < totalTicks; i++) {

            final float value = getValueForTick(i);
            float div = _scaleEndValue / (float) _divisions;
            float mod = value % div;
            isDivisionTick = (Math.abs(mod - 0) < 0.001) || (Math.abs(mod - div) < 0.001);

            final float y1 = _scaleRect.top;

            final float y2 = y1 + 0.045f; // height of division
            final float y3 = y1 + 0.090f; // height of subdivision

            final Paint paint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);

            switch (_gaugeRedSide){
                case Enums.GAUGE_RED_SIDE_RIGHT:

                    if (i >= (totalTicks - _redDivisions)){
                        paint.setColor(Color.rgb(255,10,10));
                    }
                    break;
                case Enums.GAUGE_RED_SIDE_LEFT:

                    if (i < _redDivisions){
                        paint.setColor(Color.rgb(255,10,10));
                    }
                    break;
                case Enums.GAUGE_RED_SIDE_BOTH:

                    if (i < _redDivisions || i >= (totalTicks - _redDivisions)){
                        paint.setColor(Color.rgb(255,10,10));
                    }
                    break;
                default:

                    if(isDivisionTick){
                        paint.setColor(Color.rgb(87,97,114));
                    } else {
                        paint.setColor(Color.rgb(255,10,10));
                    }
                    break;
            }

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(0.005f);
            paint.setTextSize(0.05f);
            paint.setTypeface(Typeface.SANS_SERIF);
            paint.setTextAlign(Align.CENTER);
            paint.setShadowLayer(0.005f, 0.002f, 0.002f, 0);

            if (isDivisionTick) {
                // Draw a division tick
                paint.setStrokeWidth(0.01f);
                canvas.drawLine(0.5f, y1 - 0.015f, 0.5f, y3 - 0.03f, paint);
                // Draw the text 0.15 away from the division tick
                paint.setStyle(Paint.Style.FILL);
                drawText(canvas, String.format(Locale.US, "%d", (int) value), 0.5f, y3 + 0.05f, paint);
            } else {
                // Draw a subdivision tick
                paint.setStrokeWidth(0.002f);
                canvas.drawLine(0.5f, y1, 0.5f, y2, paint);
            }
            canvas.rotate((200 / (_divisions * _subDivisions)), 0.5f, 0.5f);
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
        return tick * (_divisionValue / _subDivisions);
    }

    private void drawNeedle(final int w, final int h){
        _needleCenterCoordinatesX = w / 2;
        _needleCenterCoordinatesY = (h / 5) * 4;

        float needleTip = w - w / 30;
        float upperNeedleEnd = _needleCenterCoordinatesY - h / 65;
        float lowerNeedleEnd = _needleCenterCoordinatesY + h / 65;

        _linePath = new Path();
        _linePath.moveTo(_needleCenterCoordinatesX, upperNeedleEnd);
        _linePath.lineTo(needleTip, _needleCenterCoordinatesY);
        _linePath.lineTo(_needleCenterCoordinatesX, lowerNeedleEnd);
        _linePath.lineTo(_needleCenterCoordinatesX - w / 15, _needleCenterCoordinatesY + h / 65);
        _linePath.lineTo(_needleCenterCoordinatesX - w / 15 , upperNeedleEnd);
        _linePath.lineTo(_needleCenterCoordinatesX, upperNeedleEnd);
        _linePath.addCircle(_needleCenterCoordinatesX, _needleCenterCoordinatesY, w / 30, Path.Direction.CW);
        _linePath.close();

        _matrix.setRotate(170.0f, _needleCenterCoordinatesX, _needleCenterCoordinatesY);
        _newValue = 0.0f;
    }
}
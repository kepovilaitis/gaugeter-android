package constants;

import android.graphics.Color;

public class Constants {
    public final static int REQUEST_ENABLE_BT = 1;
    public static final int SIZE = 300;
    public static final float TOP = 0.0f;
    public static final float LEFT = 0.0f;
    public static final float RIGHT = 1.0f;
    public static final float BOTTOM = 1.0f;
    public static final boolean SHOW_SCALE = false;
    public static final boolean SHOW_RANGES = true;

    public static final float NEEDLE_WIDTH = 0.025f;
    public static final float NEEDLE_HEIGHT = 0.32f;

    public static final float SCALE_POSITION = 0.015f;
    public static final float SCALE_START_VALUE = 0.0f;
    public static final float SCALE_END_VALUE = 100.0f;
    public static final float SCALE_START_ANGLE = 60.0f;
    public static final int SCALE_DIVISIONS = 10;
    public static final int SCALE_SUBDIVISIONS = 5;

    public static final float[] RANGE_VALUES = {0.0f, 100.0f};
    public static final int[] RANGE_COLORS = {Color.rgb(0, 0, 0), Color.rgb(0, 0, 0), Color.rgb(0, 0, 0),
            Color.rgb(0, 0, 0)};
}

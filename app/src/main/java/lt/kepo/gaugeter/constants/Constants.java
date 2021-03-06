package lt.kepo.gaugeter.constants;

public class Constants {
    public final static int REQUEST_ENABLE_BT = 1;
    public final static int REQUEST_EDIT_DEVICE = 2;
    public static final int PERMISSION_COARSE_LOCATION = 1;
    public static final int PERMISSION_BLUETOOTH_ADMIN = 2;

    public static final int SIZE = 300;
    public static final float TOP = 0.0f;
    public static final float LEFT = 0.0f;
    public static final float RIGHT = 1.0f;
    public static final float BOTTOM = 1.0f;
    public static final boolean SHOW_SCALE = false;

    public static final float SCALE_POSITION = 0.015f;
    public static final float SCALE_START_VALUE = 0.0f;
    public static final float SCALE_END_VALUE = 100.0f;
    public static final int SCALE_DIVISIONS = 10;
    public static final int SCALE_SUBDIVISIONS = 3;
    public static final int SCALE_RED_DIVISIONS = 2;

    public static final int GAUGE_RED_SIDE_LEFT = 0;
    public static final int GAUGE_RED_SIDE_RIGHT = 1;
    public static final int GAUGE_RED_SIDE_BOTH = 2;

    public static final int MEASUREMENET_SYSTEM_METRIC = 0;
    public static final int MEASUREMENET_SYSTEM_IMPERIAL = 1;

    public static final String USER_ID = "user_id";
    public static final String USER_MEASUREMENT_SYSTEM = "user_measurement_system";
    public static final String LOGGED_IN = "logged_in";
    public static final String USER_TOKEN = "user_token";

    public static final int MIN_JOB_TELEM_COUNT = 300;
}

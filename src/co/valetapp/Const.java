package co.valetapp;

public class Const {
    public static final int REQUEST_CODE_GOOGLE_PLAY = 1;
    public static final int REQUEST_ENABLE_BT = 1;
    public static final float MIN_ACCURACY = 10f;
    public static final int ZOOM_LEVEL = 15;
    public static final String SHARED_PREFS_NAME = "co.valetapp.prefs";
    public static final String LAT_KEY = "co.valetapp.lat";
    public static final String LONG_KEY = "co.valetapp.long";
    public static final String TIME_KEY = "co.valetapp.time";
    public static final String BLUETOOTH_KEY = "co.valetapp.bluetoothSpinner";
    public static final float METERS_TO_MILES = 0.00062137f;
    public static final long MS_IN_HOUR = 3600000;
    public static final long MS_IN_MINUTE = 60000;
    public static final long MS_IN_SECOND = 1000;
    public static final String KEY_1 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlXr1t5G9vWH2vrMt1n1mApuvJrhs1c";
    public static final String KEY_2 = "M4uBG6Nr702h8kZtI1v/vgza4m83jzj3c3K+NCCGF45NmJnc3llbi+vIcgP6+z9jlcyweOOTCjKBFNPn0UX1CBoKNRcb70igL5";
    public static final String KEY_3 = "kj5ZM/6Gi9eU2uDCePdL+cLIYo1LCa17F1j3KVV2Aavx39XBVoYxEfVesf38s012OiUVZAqjPxHgRI6UD5eJRQM3Cttun3aSf2";
    public static final String KEY_4 = "2aZXqXxpyZUnhI+AfkGRIlchbxontVsdEaJ9OUJtns6kx9azJkSuUQgVJKkA91sGWlV0HNLpKYzixurQjW6OSKiIweYWSpu1G9";
    public static final String KEY_5 = "XOAZg9o1q0QV+LNFnwIDAQAB";
    public static final String IAB_KEY = Const.KEY_1 + Const.KEY_2 + Const.KEY_3 + Const.KEY_4 + Const.KEY_5;
    public static final String SKU_AUTO_PARK = "android.test.purchased";
    public static final String TAG = "Valet";
    public static final String ACTION_ALARM = "co.valetapp.alarm";

    private Const() {
    }
}

package com.appsandlabs.telugubeats.config;

/**
 * Created by abhinav on 9/27/15.
 */
public class Config {
    public static final boolean IS_TEST_BUILD = false;
    public static final String GCM_SAVED = "gcm_saved";
    public static final String ERR_LOG_TAG = "telugubeats_error_log";
    public static  String APP_LOADING_VIEW_IMAGE = null;
    public static final String REGISTRATION_COMPLETE = "registration_complete";
    public static final String PREF_ENCODED_KEY = "user_auth_key";
    public static final String PREF_IS_FIRST_TIME_LOAD = "is_first_time";
    public static final int NOTIFICATION_ID = 12323;
    public static final String NOTIFICATION_KEY_MESSAGE_TYPE = "messageType";
    public static final String NOTIFICATION_KEY_TEXT_MESSAGE = "message";
    public static final String GCM_NOTIFICATION_INTENT_ACTION = "com.appsandlabs.gcmnotification";


    public static boolean currentVersionSupportBigNotification() {
        int sdkVersion = android.os.Build.VERSION.SDK_INT;
        if(sdkVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN){
            return true;
        }
        return false;
    }

    public static boolean currentVersionSupportLockScreenControls() {
        int sdkVersion = android.os.Build.VERSION.SDK_INT;
        if(sdkVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH){
            return true;
        }
        return false;
    }
}

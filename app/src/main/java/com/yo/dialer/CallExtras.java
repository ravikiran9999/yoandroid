package com.yo.dialer;

/**
 * Created by root on 18/7/17.
 */

public interface CallExtras {
    public static final String CALLER_NO = "caller_number";
    public static final String IS_PSTN = "is_pstn";
    public static final String REGISTER = "register";
    public static final String UN_REGISTER = "un-register";
    public static final String IMAGE = "image_link";
    public static final String NAME = "name";
    public static final String PHONE_NUMBER = "phone_number";

    public static final String MAKE_CALL = "android.yo.MAKE_CALL";
    public static final String ACCEPT_CALL = "android.yo.ACCEPT_CALL";
    public static final String REJECT_CALL = "android.yo.REJECT_CALL";
    public static final String CONFIRMED = "CONFIRMED";
    public static final String CONNECTING = "CONNECTING";
    public static final String NETWORK_NOT_REACHABLE = "network is reachable";
    public static final String REGISTRATION_SUCCESS = "Registration successful";
    public static final String NORMAL_CALL_CLEARING = "Normal call clearing";
    public static final String DISCONNECTED = "Disconnected";
    public static final String REQUEST_TIMEOUT = "Request Timeout";
    public static final String CONNECTION_TIMEOUT = "Connection timed out";
    public static final String NO_ROUTE_TO_HOST = "No route to host";//CODE PJSIP_SC_SERVICE_UNAVAILABLE
    public static final String DISPLAY_NUMBER = "display_number";

    public interface StatusCode {
        public static final int PJSUA_CALL_MEDIA_REMOTE_HOLD = 1;
        public static final int PJSUA_CALL_MEDIA_ACTIVE = 2;
        public static final int PJSIP_INV_STATE_CONFIRMED = 3;
        public static final int PJSIP_INV_STATE_DISCONNECTED = 4;
        public static final int PJSIP_INV_STATE_SC_RINGING = 5;

    }
}

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
    public static final String REGISTRATION_STATUS = "registration_status";
    public static final String REGISTRATION_STATUS_MESSAGE = "registration_status_message";

    public static final String CALL_STATE = "call_state";
    public static final String RE_REGISTERING = "re-registering";
    public static final String GOOGLE_DATA = "google_data";


    public interface StatusCode {
        public static final int YO_INV_STATE_SC_UNKNOWN = 108;
        public static final int YO_CALL_MEDIA_REMOTE_HOLD = 1;
        public static final int YO_CALL_MEDIA_ACTIVE = 2; //Media active mean its unhold or connected.
        public static final int YO_INV_STATE_CONNECTED = 3;
        public static final int YO_INV_STATE_DISCONNECTED = 4; //Reason  - Service unavailable,Not found,Not Acceptable Here,Forbidden
        public static final int YO_INV_STATE_SC_RINGING = 5;
        public static final int YO_INV_STATE_SC_CALLING = 6;
        public static final int YO_INV_STATE_SC_RE_CONNECTING = 7;
        public static final int YO_CALL_MEDIA_LOCAL_HOLD = 8;
        public static final int YO_CALL_NETWORK_NOT_REACHABLE = 9;
        public static final int YO_INV_STATE_SC_CONNECTING = 10;
        public static final int YO_INV_STATE_SC_NO_ANSWER = 11;
        public static final int YO_INV_STATE_CALLEE_NOT_ONLINE = 12;
        public static final int YO_REQUEST_TIME_OUT = 13;
        public static final int YO_BUSY_HERE = 14;
        public static final int YO_FORBIDDEN = 15;

        //Google data codes
        public static final String OTHER = "999";
        public static final String USER_NOT_FOUND_SERVICE_NOT_AVAILABLE = "1000";
        public static final String NO_ANSWER_NOT_ACCEPTABLE = "1001";
        public static final String YO_NEXGE_SERVER_DOWN = "1002";
        public static final String BUSY = "1003";
        public static final String FORBIDDEN = "1004";


        public static final int YO_NORMAL_PHONE_INCOMING_CALL = 50;//put yo call on hold
        public static final int YO_NORMAL_PHONE_INCOMING_CALL_DISCONNECTED = 51;//put yo call on hold


    }

    public interface StatusReason {
        public static final String YO_SERVICE_UNAVAILABLE = "Service unavailable";
        public static final String YO_NOT_FOUND = "Not found";
        public static final String YO_RINGING = "Ringing";
        public static final String YO_REQUEST_TIMEOUT = "Request Timeout";
        public static final String YO_NEXGE_SERVER_DOWN = "Connection refused";
        public static final String YO_NOT_ACCEPTABLE_HERE = "Not Acceptable Here";
        public static final String YO_NETWORK_IS_UNREACHABLE = "Network is unreachable";
        public static final String YO_BUSY_HERE = "Busy Here";
        public static final String YO_FORBIDDEN = "Forbidden";


    }

    public interface Actions {
        public static final String COM_YO_ACTION_CLOSE = "com.yo.action.close";
        public static final String COM_YO_ACTION_CALL_ACCEPTED = "com.yo.action.CALL_ACCEPTED";
        public static final String COM_YO_ACTION_CALL_UPDATE_STATUS = "com.yo.action.CALL_UPDATE_STATUS";
        public static final String COM_YO_ACTION_CALL_NO_NETWORK = "com.yo.action.CALL_NO_NETWORK";
        public static final String COM_YO_ACTION_CALL_NORMAL_CALL = "com.yo.action.CALL_NORMAL_CALL";
        public static final String COM_YO_ACTION_CALL_NORMAL_CALL_DISCONNECTED = "com.yo.action.CALL_NORMAL_CALL_DISCONNECTED";

        public static final String CHAT_GROUP_CREATED = "com.yo.action.CHAT_GROUP_CREATED";


    }
}

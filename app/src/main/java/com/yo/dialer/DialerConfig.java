package com.yo.dialer;

/**
 * Created by Rajesh Babu on 11/7/17.
 */

public class DialerConfig {
    public static final boolean ENABLE_LOGS = true;
    public static final boolean SHOW_POPUPS = true;

    //public static final String NEXGE_SERVER_IP = "173.82.147.172";
    public static final String NEXGE_SERVER_IP = "185.106.240.205";
    public static final String STUN_SERVER = "34.232.154.97:3478";
    //public static final String STUN_SERVER = "stun.pjsip.org:5080";
    //public static final String TURN_SERVER = "turn.pjsip.org:33478";
    public static final String TURN_SERVER = "34.230.108.83:3478";


    public static final String NEXGE_SERVER_TCP_PORT = "6000";
    public static final String TCP = ";transport=tcp;lr";

    public static final String UDP = "transport=udp";
    public static final String NEXGE_SERVER_UDP_PORT = "5060";


    public static final String SIP = "sip:";

    public static final boolean IS_NEW_SIP = true;
    //public static final String USERNAME = "abzlute01";
    public static final String USERNAME = "turn123";
    public static final String PASSWORD = USERNAME;
    public static final boolean UPLOAD_REPORTS_GOOGLE_SHEET = true;
}

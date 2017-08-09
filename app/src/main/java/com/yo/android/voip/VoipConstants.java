package com.yo.android.voip;

/**
 * Created by Ramesh on 28/6/16.
 */
public interface VoipConstants {


    String CALL_ACTION_OUT_GOING = "android.yo.OUTGOING_CALL";
    String CALL_ACTION_IN_COMING = "android.SipPrac.INCOMING_CALL";
    String NEW_ACCOUNT_REGISTRATION = "com.yo.NewAccountSipRegistration";
    String ACCOUNT_LOGOUT = "com.yo.android.ACCOUNT_LOGOUT";

    String MAKE_CALL = "android.yo.MAKE_CALL";


    int BASE = 100;
    int CALL_DIRECTION_OUT = BASE + 1;
    int CALL_DIRECTION_IN = BASE + 2;
    int CALL_DIRECTION_IN_MISSED = BASE + 3;
    int CALL_MODE_VOIP = BASE + 10;

    String PSTN = "PSTN";
}

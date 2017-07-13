package com.yo.dialer.yopj;

import com.yo.android.pjsip.MyAppObserver;
import com.yo.android.pjsip.MyBuddy;
import com.yo.android.pjsip.MyCall;

import org.pjsip.pjsua2.OnIncomingCallParam;
import org.pjsip.pjsua2.pjsip_status_code;

/**
 * Created by root on 12/7/17.
 */

public class YOCallNotifiy implements MyAppObserver {
    @Override
    public void notifyRegState(pjsip_status_code code, String reason, int expiration) {

    }

    @Override
    public void notifyIncomingCall(MyCall call, OnIncomingCallParam prm) {

    }

    @Override
    public void notifyCallState(MyCall call) {

    }

    @Override
    public void notifyCallMediaState(MyCall call) {

    }

    @Override
    public void notifyBuddyState(MyBuddy buddy) {

    }
}

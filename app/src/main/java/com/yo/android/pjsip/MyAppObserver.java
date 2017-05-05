package com.yo.android.pjsip;

import org.pjsip.pjsua2.OnIncomingCallParam;
import org.pjsip.pjsua2.pjsip_status_code;

/* Interface to separate UI & engine a bit better */
interface MyAppObserver {
    void notifyRegState(pjsip_status_code code, String reason,
                        int expiration);

    void notifyIncomingCall(MyCall call, OnIncomingCallParam prm);

    void notifyCallState(MyCall call);

    void notifyCallMediaState(MyCall call);

    void notifyBuddyState(MyBuddy buddy);
}

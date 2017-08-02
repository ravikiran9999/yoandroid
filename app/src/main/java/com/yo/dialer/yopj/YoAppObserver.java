package com.yo.dialer.yopj;

import org.pjsip.pjsua2.pjsip_status_code;

/**
 * Created by Rajesh Babu on 17/7/17.
 */

public interface YoAppObserver {
    abstract void notifyRegState(pjsip_status_code code, String reason,
                                 int expiration);

    abstract void notifyIncomingCall(YoCall call);

    abstract void notifyCallState(YoCall call);

    abstract void notifyCallMediaState(YoCall call);

    abstract void notifyBuddyState(YoBuddy buddy);
}

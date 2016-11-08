package com.yo.android.pjsip;

import android.content.Intent;
import android.os.Bundle;

import org.pjsip.pjsua2.CallInfo;

/**
 * Created by Ramesh on 13/8/16.
 */
public interface SipServiceHandler {
    void addAccount(SipProfile sipProfile);

    MediaManager getMediaManager();

    void makeCall(String destination, Bundle options, Intent intent);

    void setHoldCall(boolean isHold);

    CallInfo getInfo();

    void acceptCall();

    void hangupCall(int callType);

    long getCallStartDuration();

    SipCallState getSipCallState();

    String getRegistrationStatus();

}

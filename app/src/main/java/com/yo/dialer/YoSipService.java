package com.yo.dialer;

import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.yo.android.di.InjectedService;
import com.yo.android.pjsip.CallDisconnectedListner;
import com.yo.android.pjsip.MediaManager;
import com.yo.android.pjsip.MyAccount;
import com.yo.android.pjsip.MyApp;
import com.yo.android.pjsip.SipBinder;
import com.yo.android.pjsip.SipCallState;
import com.yo.android.pjsip.SipProfile;
import com.yo.android.pjsip.SipServiceHandler;
import com.yo.dialer.yopj.YOPJRegConfig;

import org.pjsip.pjsua2.CallInfo;

/**
 * Created by Rajesh Babu on 11/7/17.
 */

public class YoSipService extends InjectedService implements SipServiceHandler {
    private static final String TAG = YoSipService.class.getSimpleName();
    public static final String START = "Start";

    //Media Manager to handle audio related events.
    private MediaManager mediaManager;

    private MyAccount myAccount;

    private MyApp myApp;

    private boolean isSipServiceCreated;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new SipBinder(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaManager = new MediaManager(this);
    }

    /**
     * Adding account, while adding account if its PSTN need to add display name as phone number.
     * For sip to sip it should pass total sip uri to handle sip to sip call cases.
     * These should be done before registering the account, so below logic will serve this purpose.
     *
     * @param sipProfile
     * @param isPSTN
     * @param number
     * @return
     */
    @Override
    public String addAccount(SipProfile sipProfile, boolean isPSTN, String number) {
        String displayname;
        if (isPSTN) {
            displayname = DialerHelper.parsePhoneNumber(sipProfile.getUsername());
        } else {
            displayname = sipProfile.getUsername();
        }
        String id = DialerHelper.getURI(displayname, sipProfile.getUsername(), sipProfile.getDomain());
        DialerLogs.messageI(TAG, "After formatting SIP id " + id);
        try {
            myAccount = YOPJRegConfig.buildAccount(this, myAccount, myApp, id, START, isSipServiceCreated);
            YOPJRegConfig.updateSIPConfig(myAccount, sipProfile, sipProfile.getUsername(), displayname);
        } catch (Exception | UnsatisfiedLinkError e) {
            DialerLogs.messageE(TAG, e.getMessage());
        }
        return id;
    }


    @Override
    public void createSipService(SipProfile sipProfile) {

    }

    @Override
    public MediaManager getMediaManager() {
        return mediaManager;
    }

    @Override
    public void makeCall(String destination, Bundle options, Intent intent) {

    }

    @Override
    public void setHoldCall(boolean isHold) {

    }

    @Override
    public CallInfo getInfo() {
        return null;
    }

    @Override
    public void acceptCall() {

    }

    @Override
    public void hangupCall(int callType) {

    }

    @Override
    public long getCallStartDuration() {
        return 0;
    }

    @Override
    public SipCallState getSipCallState() {
        return null;
    }

    @Override
    public String getRegistrationStatus() {
        return null;
    }

    @Override
    public boolean isOnGOingCall() {
        return false;
    }

    @Override
    public void disconnectCallBack(CallDisconnectedListner listner) {

    }

}

package com.yo.dialer.yopj;

import com.yo.dialer.DialerLogs;

import org.pjsip.pjsua2.Account;
import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.OnIncomingCallParam;
import org.pjsip.pjsua2.OnRegStartedParam;
import org.pjsip.pjsua2.OnRegStateParam;

import java.util.ArrayList;

/**
 * Created by Rajesh Babu on 17/7/17.
 */

public class YoAccount extends Account {
    public ArrayList<YoBuddy> buddyList = new ArrayList<YoBuddy>();
    public AccountConfig cfg;
    private static final String TAG = YoAccount.class.getSimpleName();

    YoAccount(AccountConfig config) {
        super();
        cfg = config;
    }

    @Override
    public void onRegState(OnRegStateParam prm) {
        super.onRegState(prm);
        DialerLogs.messageI(TAG, "YO========onRegState " + prm.getReason());
        YoApp.observer.notifyRegState(prm.getCode(), prm.getReason(),
                prm.getExpiration());
    }

    @Override
    public void onRegStarted(OnRegStartedParam prm) {
        super.onRegStarted(prm);
        DialerLogs.messageI(TAG, "YO========onRegStarted " + prm.getRenew());

    }

    @Override
    public void onIncomingCall(OnIncomingCallParam prm) {
        super.onIncomingCall(prm);
        DialerLogs.messageI(TAG, "YO========onIncomingCall " + prm.getCallId());
        YoCall call = new YoCall(this, prm.getCallId());
        YoApp.observer.notifyIncomingCall(call);

    }
}

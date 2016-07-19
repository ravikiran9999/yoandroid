package com.yo.android.voip;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.sip.SipErrorCode;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;

import com.orion.android.common.logger.Log;

import java.text.ParseException;

public class RegisterSip {

    private static final String TAG = "RegisterSip";
    private static final int MAX_ATTEMPTS = 3;
    public static final int UNDEFINED = 0;
    public static final int UNREGISTERED = 1;
    public static final int REGISTERING = 2;
    public static final int REGISTERED = 3;

    private SipManager manager;
    private SipProfile profile;
    private Context context;
    private String username;
    private String password;
    private String domain;
    private int attempts = 0;

    private int currentState = UNDEFINED;
    private Log mLog;

    public RegisterSip(Log log, SipManager manager, SipProfile profile, Context context,
                       String username, String password, String domain) {
        this.manager = manager;
        this.profile = profile;
        this.context = context;
        this.username = username;
        this.password = password;
        this.domain = domain;
        this.mLog = log;
        this.attempts = 0;
        this.currentState = UNDEFINED;
        register();
    }

    public SipProfile getProfile() {
        return profile;
    }

    public synchronized void register() {
        mLog.d("ATTEMPTS = ", Integer.toString(attempts));
        if (attempts < MAX_ATTEMPTS) {
            try {
                closeLocalProfile();
                SipProfile.Builder builder = new SipProfile.Builder(username, domain);
                builder.setPassword(password);
                //Need to test it once
//                builder.setPort(4460);
//                builder.setAutoRegistration(true);
                profile = builder.build();
                Intent intent = new Intent();
                intent.setAction(VoipConstants.CALL_ACTION_IN_COMING);
                PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, Intent.FILL_IN_DATA);
                manager.open(profile, pi, null);
                //Bug fix: http://stackoverflow.com/a/19540346/874752
                try {
                    manager.register(profile, 20, listener);
                } catch (Exception e) {
                    mLog.w(TAG, e);
                }
                manager.setRegistrationListener(profile.getUriString(), listener);
            } catch (ParseException e) {
                mLog.w(TAG, e);
            } catch (SipException e) {
                mLog.w(TAG, e);
            } catch (Exception e) {
                mLog.w(TAG, e);
            }
        } else {
            if (currentState != REGISTERED) {
                mLog.d("SIP REGISTRATION FAILED", "CHECK INTERNET CONNECTION");
            }
        }
    }

    public void closeLocalProfile() {
        if (manager == null) {
            return;
        }
        try {
            if (profile != null) {
                manager.close(profile.getUriString());
            }
        } catch (Exception ee) {
            mLog.w("WalkieTalkieActivity/onDestroy", "Failed to close local profile.", ee);
        }
    }

    public int getCurrentState() {
        return currentState;
    }

    private SipRegistrationListener listener = new SipRegistrationListener() {

        @Override
        public void onRegistrationFailed(String localProfileUri, int errorCode, String errorMessage) {
            currentState = UNREGISTERED;
            mLog.d(TAG, "SIP ACCOUNT - %s", "REGISTRATION FAILED");
            mLog.d(TAG, "SIP ACCOUNT - ERROR CODE  %s", SipErrorCode.toString(errorCode));
            mLog.d(TAG, "SIP ACCOUNT - ERROR MSG %s", errorMessage);
        }

        @Override
        public void onRegistrationDone(String localProfileUri, long expiryTime) {
            currentState = REGISTERED;
            mLog.d(TAG, "SIP ACCOUNT - %s", "REGISTRATION DONE");
        }

        @Override
        public void onRegistering(String localProfileUri) {
            currentState = REGISTERING;
            mLog.d(TAG, "SIP ACCOUNT -%s", "REGISTERING...");
        }
    };
}

package com.yo.android.voip;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.orion.android.common.logger.Log;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.di.InjectedBroadcastReceiver;

import javax.inject.Inject;
import javax.inject.Named;


public class Receiver extends InjectedBroadcastReceiver {
    private static final String TAG = "Receiver";
    private RegisterSip register;
    private SipManager manager;
    private SipAudioCall sipAudioCall;
    private SipProfile profile;
    private UserAgent callAgent;
    private String username;
    private String password;
    private static int call_state;
    //private static String DOMAIN_ADDRESS = "209.239.120.239";
    //private static String DOMAIN_ADDRESS = "173.82.147.172";
    private static String DOMAIN_ADDRESS = "185.106.240.205";

    @Inject
    protected Log mLog;
    @Inject
    @Named("login")
    protected PreferenceEndPoint mPreferenceEndPoint;


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String intentAction = intent.getAction();
        mLog.d(TAG, "INTENT-ACTION: %s", intentAction);
        if (intentAction.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            if (isOnline(context)) {
                mLog.d(TAG, "USER ONLINE", "<<<<< USER IS ONLINE >>>>");
                if (SipManager.isApiSupported(context) && SipManager.isVoipSupported(context)) {
                    doSipRegistration(context, intent);
                } else {
                    mLog.e(TAG, "DEVICE ERROR - %s", "SIP NOT SUPPORTED");

                }
            }

        } else if (VoipConstants.NEW_ACCOUNT_REGISTRATION.equals(intentAction)) {
            if (register != null) {
                register.closeLocalProfile();
            }
            doNewAccountRegistration(context, intent);
        } else if (intentAction.equals(Intent.ACTION_BOOT_COMPLETED)) {
            mLog.d("Yo.RECEIVER", "BOOT COMPLETE RECEIVED");
            Intent i = new Intent(context, SipService.class);
            context.startService(i);
        } else if (VoipConstants.CALL_ACTION_IN_COMING.equals(intentAction)) {
            doInComingCall(context, intent);
        } else if (VoipConstants.CALL_ACTION_OUT_GOING.equals(intentAction)) {
            doOutGoingCall(context, intent);
        }
    }

    private void doNewAccountRegistration(Context context, Intent intent) {
        if (isOnline(context)) {
            mLog.d(TAG, "USER ONLINE <<<<< USER IS ONLINE >>>>");
            if (SipManager.isApiSupported(context) && SipManager.isVoipSupported(context)) {
                doSipRegistration(context, intent);
            } else {
                mLog.e(TAG, "DEVICE ERROR : SIP NOT SUPPORTED");
            }
        }
    }

    private void doInComingCall(Context context, Intent intent) {
        if (register != null && register.getCurrentState() == RegisterSip.REGISTERED) {
            if (call_state == UserAgent.CALL_STATE_INCOMING_CALL || call_state == UserAgent.CALL_STATE_INCALL
                    || call_state == UserAgent.CALL_STATE_OUTGOING_CALL) {
                sendBusySignal(context, intent);
                return;
            }
            callAgent = new UserAgent(mLog, manager, sipAudioCall, context, intent, profile);
            callAgent.onCallIncoming();
        } else {
            doSipRegistration(context, intent);
        }
    }

    private void doOutGoingCall(Context context, Intent intent) {
        Bundle callData = intent.getExtras();
        if (register != null && register.getCurrentState() == RegisterSip.REGISTERED) {
            if (profile == null) {
                profile = register.getProfile();
            }
            if (callAgent == null) {
                callAgent = new UserAgent(mLog, manager, sipAudioCall, context, intent, profile);
            }
            if (callAgent.getCallState() == UserAgent.CALL_STATE_IDLE) {
                callAgent = new UserAgent(mLog, manager, sipAudioCall, context, intent, profile);
                callAgent.doOutgoingCall(callData);
            } else {
                mLog.e(TAG, "Receiver/OUTGOING_CALL : CALL STATE NOT IDLE");
            }
        } else {
            mLog.e(TAG, "Receiver/OUTGOING_CALL: %s", "VoIP not Registered");
            Toast.makeText(context, "VoIP Not Registered - Check Internet Connection / Restart Phone",
                    Toast.LENGTH_SHORT).show();
            doSipRegistration(context, intent);
        }
    }

    public void doSipRegistration(Context context, Intent intent) {
        if (register != null && register.getCurrentState() == RegisterSip.REGISTERED) {
            mLog.d("Receiver/doSipRegistration()", "RETURNED");
            return;
        }
        if (TextUtils.isEmpty(username)
                || TextUtils.isEmpty(password)) {
            username = mPreferenceEndPoint.getStringPreference("phone", null);
            password = mPreferenceEndPoint.getStringPreference("password", null);
            mLog.d(TAG, "Username is %s", username);
            mLog.d(TAG, "Password is %s", password);
        }
        if (!TextUtils.isEmpty(username)
                && !TextUtils.isEmpty(password)) {
            if (manager == null || register.getCurrentState() == RegisterSip.UNDEFINED
                    ||
                    register.getCurrentState() == RegisterSip.UNREGISTERED) {
                manager = SipManager.newInstance(context);
                if (register != null) {
                    register.closeLocalProfile();
                }
                register = new RegisterSip(mLog, manager, profile, context, username, password,
                        DOMAIN_ADDRESS);
                profile = register.getProfile();
            }
        } else {
            mLog.e(TAG, "username / password not exists");
        }
    }

    public void sendBusySignal(Context context, Intent intent) {
        mLog.d(TAG, "Receiver.sendBusySignal() - Sending BYE SIGNAL");
        try {
            SipAudioCall call2 = manager.takeAudioCall(intent, null);
            call2.endCall();
        } catch (SipException e) {
            mLog.w(TAG, e);
        }

    }

    //Todo check online status from util

    @SuppressWarnings("deprecation")
    public boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetInfo != null && activeNetInfo.isConnectedOrConnecting();
    }

    public static void setCallState(int callState) {
        Receiver.call_state = callState;
    }

}
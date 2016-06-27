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
import com.orion.android.common.preferences.PreferenceEndPointImpl;
import com.yo.android.di.InjectedBroadcastReceiver;

import javax.inject.Inject;


public class Receiver extends InjectedBroadcastReceiver {
    private static final String TAG = "Receiver";
    private RegisterSip register;
    private SipManager manager;
    private SipAudioCall call;
    private SipProfile profile;
    private UserAgent callAgent;
    private String username;
    private String password;

    public static int call_state;
    public static int call_end_reason = -1;
    protected PreferenceEndPoint mPreferenceEndPoint;
    private static String DOMAIN_ADDRESS = "209.239.120.239";

    @Inject
    protected Log mLog;


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        mPreferenceEndPoint = new PreferenceEndPointImpl(context, "sip_data");
        String intentAction = intent.getAction();
        mLog.d(TAG, "INTENT-ACTION: %s", intentAction);
        if (intentAction.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            if (isOnline(context)) {
                mLog.d(TAG, "USER ONLINE", "<<<<< USER IS ONLINE >>>>");
                if (SipManager.isApiSupported(context) && SipManager.isVoipSupported(context)) {
                    doSipRegistration(context, intent);
                } else {
                    mLog.e(TAG, "DEVICE ERROR - %s", "SIP NOT SUPPORTED");
                    Toast.makeText(context, "VoIP is not supported in your device",
                            Toast.LENGTH_SHORT).show();

                }
            }

        } else if (intentAction.equals("com.yo.NewAccountSipRegistration")) {
            if (isOnline(context)) {
                mLog.d(TAG, "USER ONLINE <<<<< USER IS ONLINE >>>>");
                if (SipManager.isApiSupported(context) && SipManager.isVoipSupported(context)) {
                    doSipRegistration(context, intent);
                } else {
                    mLog.e(TAG, "DEVICE ERROR : SIP NOT SUPPORTED");
                }
            }
        } else if (intentAction.equals(Intent.ACTION_BOOT_COMPLETED)) {
            mLog.d("Yo.RECEIVER", "BOOT COMPLETE RECEIVED");
            Intent i = new Intent(context, SipService.class);
            context.startService(i);
        } else if (intentAction.equals("android.SipPrac.INCOMING_CALL")) {
            if (register == null)
                return;
            if (register.currentState == RegisterSip.REGISTERED) {
                if (call_state == UserAgent.CALL_STATE_INCOMING_CALL || call_state == UserAgent.CALL_STATE_INCALL
                        || call_state == UserAgent.CALL_STATE_OUTGOING_CALL) {
                    sendBusySignal(context, intent);
                    return;
                }
                callAgent = new UserAgent(mLog, manager, call, context, intent, profile);
                callAgent.onCallIncoming();
            }
        } else if (intentAction.equals("android.yo.OUTGOING_CALL")) {
            Bundle callData = intent.getExtras();
            if (register != null && register.currentState == RegisterSip.REGISTERED) {
                if (profile == null)
                    profile = register.getProfile();
                if (callAgent == null)
                    callAgent = new UserAgent(mLog, manager, call, context, intent, profile);
                if (callAgent.call_state == UserAgent.CALL_STATE_IDLE) {
                    callAgent = new UserAgent(mLog, manager, call, context, intent, profile);
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
    }

    public void doSipRegistration(Context context, Intent intent) {
        if (register != null && register.currentState == RegisterSip.REGISTERED) {
            mLog.d("Receiver/doSipRegistration()", "RETURNED");
            return;
        }
        if (TextUtils.isEmpty(username)
                || TextUtils.isEmpty(password)) {
            username = mPreferenceEndPoint.getStringPreference("username", null);
            password = mPreferenceEndPoint.getStringPreference("password", null);
            mLog.d(TAG, "Username is %s", username);
            mLog.d(TAG, "Password is %s", password);
        }
        if (!TextUtils.isEmpty(username)
                && !TextUtils.isEmpty(password)) {
            if (manager == null) {
                manager = SipManager.newInstance(context);
                register = new RegisterSip(mLog, manager, profile, context, username, password,
                        "209.239.120.239");
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

    @SuppressWarnings("deprecation")
    public boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetInfo != null && activeNetInfo.isConnectedOrConnecting();
        return isConnected;
    }

    public static void onState(int state, String caller) {

        if (call_state != state) {
            if (state != UserAgent.CALL_STATE_IDLE) {
                call_end_reason = -1;
            }
            call_state = state;
            switch (call_state) {
                case UserAgent.CALL_STATE_INCOMING_CALL:
                    break;
                case UserAgent.CALL_STATE_OUTGOING_CALL:
                    break;
                case UserAgent.CALL_STATE_IDLE:
                    break;
                case UserAgent.CALL_STATE_INCALL:
                    break;
                case UserAgent.CALL_STATE_HOLD:
                    break;
            }
        }
    }
}
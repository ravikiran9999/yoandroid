package com.yo.android.pjsip;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.orion.android.common.logger.Log;
import com.orion.android.common.util.ToastFactory;
import com.yo.android.di.InjectedService;
import com.yo.android.ui.BottomTabsActivity;
import com.yo.android.util.Util;
import com.yo.android.voip.InComingCallActivity;
import com.yo.android.voip.OutGoingCallActivity;
import com.yo.android.voip.SipCallModel;
import com.yo.android.voip.UserAgent;
import com.yo.android.voip.VoipConstants;

import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.AuthCredInfo;
import org.pjsip.pjsua2.AuthCredInfoVector;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.StringVector;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_status_code;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/**
 * Created by Ramesh on 13/8/16.
 */
public class YoSipService extends InjectedService implements MyAppObserver, SipServiceHandler {

    private static final String TAG = "YoSipService";
    private boolean created;

    private MyApp myApp;
    private MyAccount myAccount;
    private String domain;
    private MediaManager mediaManager;
    private long callStarted;
    // Time in ms during which we should not relaunch call activity again
    final static long LAUNCH_TRIGGER_DELAY = 2000;
    private long lastLaunchCallHandler = 0;
    private int inComingCallNotificationId;
    private int outGoingCallNotificationId;
    @Inject
    Log mLog;
    @Inject
    ToastFactory mToastFactory;
    public static MyCall currentCall = null;
    private SipCallState sipCallState;
    private Handler mHandler;
    private String registrationStatus = "";

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler(Looper.getMainLooper());
        sipCallState = new SipCallState();
        //TODO:Store in shared prefs and retrieve it
        domain = "209.239.120.239";
        mediaManager = new MediaManager(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new SipBinder(this);
    }

    public MediaManager getMediaManager() {
        return mediaManager;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!created) {
            startSipService();
        }
        performAction(intent);
        return START_STICKY;
    }

    private void performAction(Intent intent) {
        if (intent == null) {
            return;
        }
        if (VoipConstants.CALL_ACTION_OUT_GOING.equalsIgnoreCase(intent.getAction())) {
            if (currentCall == null) {
                String number = intent.getStringExtra(OutGoingCallActivity.CALLER_NO);
                Bundle bundle = intent.getBundleExtra("data");
                if (bundle == null) {
                    bundle = new Bundle();
                }
                makeCall(number, bundle);
            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mToastFactory.showToast("Already call is in progress");
                    }
                });
                mLog.i(TAG, "performAction>>> Already call is in progress.");
            }
        } else if (VoipConstants.ACCOUNT_LOGOUT.equalsIgnoreCase(intent.getAction())) {
            myApp.deinit();
            created = false;
            stopSelf();
        }

    }


    private void startSipService() {
        myApp = new MyApp();
        myApp.init(this, getFilesDir().getAbsolutePath());
        created = true;
    }

    @Override
    public void notifyRegState(pjsip_status_code code, String reason, int expiration) {
        if (expiration == 0) {
            registrationStatus = "Unregistration";
        } else {
            registrationStatus = "Registration";
        }

        if (code.swigValue() / 100 == 2) {
            registrationStatus += " successful";
        } else {
            registrationStatus += " failed: " + reason;
        }
        mLog.e("YoSipService", "notifyRegState>>>> %s", registrationStatus);
    }

    @Override
    public void notifyIncomingCall(MyCall call) {
        /* Incoming call */
        CallOpParam prm = new CallOpParam();

			/* Only one call at anytime */
        if (currentCall != null) {
            prm.setStatusCode(pjsip_status_code.PJSIP_SC_BUSY_HERE);
            try {
                call.hangup(prm);
                Util.createNotification(this,
                        getPhoneNumber(call.getInfo().getRemoteUri()),
                        "Missed call", BottomTabsActivity.class, new Intent(), false);
            } catch (Exception e) {
                mLog.w(TAG, e);
            }
            // TODO: set status code
            call.delete();
            return;
        }

			/* Answer with ringing */
        prm.setStatusCode(pjsip_status_code.PJSIP_SC_RINGING);
        try {
            call.answer(prm);
        } catch (Exception e) {
            mLog.w(TAG, e);
        }
        currentCall = call;
        try {
            sipCallState.setCallDir(SipCallState.INCOMING);
            sipCallState.setCallState(SipCallState.CALL_RINGING);
            showInComingCall(call);
            sipCallState.setMobileNumber(getPhoneNumber(call.getInfo().getRemoteUri()));
        } catch (Exception e) {
            mLog.w(TAG, e);
        }
    }

    private synchronized void showInComingCall(MyCall mycall) throws Exception {
        long currentElapsedTime = SystemClock.elapsedRealtime();
        if (lastLaunchCallHandler + LAUNCH_TRIGGER_DELAY < currentElapsedTime) {
            //Always set default speaker off
            mediaManager.setSpeakerOn(false);
            Intent intent = new Intent(this, InComingCallActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(InComingCallActivity.CALLER, getPhoneNumber(mycall.getInfo().getRemoteUri()));
            startActivity(intent);
            lastLaunchCallHandler = currentElapsedTime;
            mediaManager.playRingtone();
            inComingCallNotificationId = Util.createNotification(this, getPhoneNumber(mycall.getInfo().getRemoteUri()), "Incoming call", InComingCallActivity.class, intent);
        }
    }

    private String getPhoneNumber(String remoteUriStr) {
        String title;
        String part2 = "";
        String ip;
        //"8341569102" <sip:8341569102@209.239.120.239>
        String regex = "\"(.+?)\" \\<sip:(.+?)@(.+?)\\>";
        Pattern p = Pattern.compile(regex);
        Matcher matcher = p.matcher(remoteUriStr);
        if (matcher.matches()) {
            title = matcher.group(1);
            part2 = matcher.group(2);
            ip = matcher.group(3);
        }
        return part2;
    }

    @Override
    public void notifyCallState(MyCall call) {
        CallInfo ci;
        try {
            ci = call.getInfo();
        } catch (Exception e) {
            ci = null;
        }

        if (ci != null
                && ci.getState() == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
            //
            try {
                int statusCode = call.getInfo().getLastStatusCode().swigValue();
                //TODO:Handle more error codes to display proper messages to the user
                // 603 Decline - when end call
                //503 Service Unavailable  - Buddy is not available
                //603 Allocated Channels Busy -Lines are busy
                if (statusCode == 503) {
                    mLog.e(TAG, "503 >>> Buddy is not online at this moment");
                }
                mLog.e(TAG, "%d %s", call.getInfo().getLastStatusCode().swigValue(), call.getInfo().getLastReason());
            } catch (Exception e) {
                e.printStackTrace();
            }


            callDisconnected();
        } else if (ci != null
                && ci.getState() == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
            callAccepted();
        }


    }

    private void callAccepted() {
        callStarted = System.currentTimeMillis();
        sipCallState.setStartTime(callStarted);
        sipCallState.setCallState(SipCallState.IN_CALL);
        mediaManager.stopRingTone();
        mediaManager.setAudioMode(AudioManager.MODE_IN_COMMUNICATION);
        SipCallModel callModel = new SipCallModel();
        callModel.setOnCall(true);
        callModel.setEvent(OutGoingCallActivity.CALL_ACCEPTED_START_TIMER);
        EventBus.getDefault().post(callModel);
    }

    private void callDisconnected() {
        Util.cancelNotification(this, inComingCallNotificationId);
        Util.cancelNotification(this, outGoingCallNotificationId);
        mediaManager.setAudioMode(AudioManager.MODE_NORMAL);
        currentCall = null;
        mediaManager.stopRingTone();
        callStarted = 0;
        if (sipCallState.getCallDir() == SipCallState.INCOMING) {
            if (sipCallState.getCallState() == SipCallState.CALL_RINGING) {
                mLog.e(TAG, "Missed call >>>>>" + sipCallState.getMobileNumber());
                Util.createNotification(this,
                        sipCallState.getMobileNumber(),
                        "Missed call ", BottomTabsActivity.class, new Intent(), false);

            }
        }
        sipCallState.setCallState(SipCallState.CALL_FINISHED);
        //Reset
        sipCallState = new SipCallState();
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(new Intent(UserAgent.ACTION_CALL_END));
    }

    @Override
    public void notifyCallMediaState(MyCall call) {

    }

    @Override
    public void notifyBuddyState(MyBuddy buddy) {

    }

    private MyAccount buildAccount() {
        if (myAccount != null) {
            return myAccount;
        }
        AccountConfig accCfg = new AccountConfig();
        accCfg.setIdUri("sip:localhost");
        accCfg.getNatConfig().setIceEnabled(true);
        accCfg.getVideoConfig().setAutoTransmitOutgoing(true);
        accCfg.getVideoConfig().setAutoShowIncoming(true);
        return myApp.addAcc(accCfg);
    }

    public void addAccount(SipProfile sipProfile) {
        try {
            myAccount = buildAccount();
            String id = String.format("sip:%s@%s", sipProfile.getUsername(), sipProfile.getDomain());
            String registrar = String.format("sip:%s:%s", sipProfile.getDomain(), 5060);
            String proxy = String.format("sip:%s:%s", sipProfile.getDomain(), 5060);
            String username = sipProfile.getUsername();
            String password = sipProfile.getPassword();
            configAccount(myAccount.cfg, id, registrar, proxy, username, password);
            try {
                myAccount.modify(myAccount.cfg);
            } catch (Exception e) {
                mLog.w(TAG, e);
            }
        } catch (Exception e) {
            mLog.w(TAG, e);
        }

    }

    private void configAccount(AccountConfig accCfg, String acc_id, String registrar, String proxy,
                               String username, String password) {
        accCfg.setIdUri(acc_id);
        accCfg.getRegConfig().setRegistrarUri(registrar);
        AuthCredInfoVector creds = accCfg.getSipConfig().getAuthCreds();
        creds.clear();
        if (username.length() != 0) {
            creds.add(new AuthCredInfo("Digest", "*", username, 0, password));
        }
        StringVector proxies = accCfg.getSipConfig().getProxies();
        proxies.clear();
        if (proxy.length() != 0) {
            proxies.add(proxy);
        }

		/* Enable ICE */
        accCfg.getNatConfig().setIceEnabled(true);
    }

    public void makeCall(String destination, Bundle options) {
        String phone = destination;
        if (destination != null && !destination.startsWith("sip:")) {
            destination = "sip:" + destination;
        }
        String finalUri = String.format("%s@%s", destination, getDomain());
        /* Only one call at anytime */
        if (currentCall != null) {
            return;
        }
        MyCall call = new MyCall(myAccount, -1);
        CallOpParam prm = new CallOpParam(true);

        try {

            call.makeCall(finalUri, prm);
        } catch (Exception e) {
            mLog.w(TAG, e);
            call.delete();
            return;
        }

        currentCall = call;
        showCallActivity(phone, options);
    }

    private void showCallActivity(String destination, Bundle options) {
        //Always set default speaker off
        mediaManager.setSpeakerOn(false);

        sipCallState.setCallDir(SipCallState.OUTGOING);
        sipCallState.setCallState(SipCallState.CALL_RINGING);
        sipCallState.setMobileNumber(destination);

        Intent intent = new Intent(this, OutGoingCallActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("data", options);
        intent.putExtra(OutGoingCallActivity.CALLER_NO, destination);
        startActivity(intent);
        outGoingCallNotificationId = Util.createNotification(this, destination, "Outgoing call", OutGoingCallActivity.class, intent);
    }

    public String getDomain() {
        return domain;
    }

    public void setHoldCall(boolean isHold) {
        if (isHold) {
            if (currentCall != null) {
                CallOpParam prm = new CallOpParam(true);
                try {
                    currentCall.setHold(prm);
                } catch (Exception e) {
                    mLog.w(TAG, e);
                }
            }
        } else {
            if (currentCall != null) {
                CallOpParam prm = new CallOpParam(true);
                try {
                    currentCall.reinvite(prm);
                } catch (Exception e) {
                    mLog.w(TAG, e);
                }
            }
        }
    }

    public CallInfo getInfo() {
        if (currentCall != null) {
            try {
                return currentCall.getInfo();
            } catch (Exception e) {
                mLog.w(TAG, e);
            }
        }
        return null;
    }

    public void acceptCall() {
        CallOpParam prm = new CallOpParam();
        prm.setStatusCode(pjsip_status_code.PJSIP_SC_OK);
        try {
            currentCall.answer(prm);
            sipCallState.setCallState(SipCallState.IN_CALL);
        } catch (Exception e) {
            mLog.w(TAG, e);
        }
    }

    public void hangupCall() {
        if (currentCall != null) {
            CallOpParam prm = new CallOpParam();
            prm.setStatusCode(pjsip_status_code.PJSIP_SC_DECLINE);
            try {
                currentCall.hangup(prm);
                sipCallState.setCallState(SipCallState.CALL_FINISHED);
            } catch (Exception e) {
                mLog.w(TAG, e);
            }
        }
    }

    public long getCallStartDuration() {
        return callStarted;
    }

    @Override
    public SipCallState getSipCallState() {
        return sipCallState;
    }

    @Override
    public String getRegistrationStatus() {
        return registrationStatus;
    }


}
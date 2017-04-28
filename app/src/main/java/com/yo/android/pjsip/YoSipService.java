package com.yo.android.pjsip;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.orion.android.common.logger.Log;
import com.orion.android.common.logging.Logger;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.orion.android.common.util.ConnectivityHelper;
import com.orion.android.common.util.ToastFactory;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.calllogs.CallLog;
import com.yo.android.calllogs.CallerInfo;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.di.InjectedService;
import com.yo.android.model.Contact;
import com.yo.android.model.dialer.OpponentDetails;
import com.yo.android.networkmanager.NetworkStateChangeListener;
import com.yo.android.networkmanager.NetworkStateListener;
import com.yo.android.ui.BottomTabsActivity;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;
import com.yo.android.voip.InComingCallActivity;
import com.yo.android.voip.OutGoingCallActivity;
import com.yo.android.voip.PhoneStateReceiver;
import com.yo.android.voip.SipCallModel;
import com.yo.android.voip.UserAgent;
import com.yo.android.voip.VoipConstants;
import com.yo.android.vox.CodecPriority;

import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.AudDevManager;
import org.pjsip.pjsua2.AudioMedia;
import org.pjsip.pjsua2.AuthCredInfo;
import org.pjsip.pjsua2.AuthCredInfoVector;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallMediaInfo;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.CodecInfoVector;
import org.pjsip.pjsua2.Endpoint;
import org.pjsip.pjsua2.EpConfig;
import org.pjsip.pjsua2.Media;
import org.pjsip.pjsua2.OnIncomingCallParam;
import org.pjsip.pjsua2.StringVector;
import org.pjsip.pjsua2.TransportConfig;
import org.pjsip.pjsua2.pj_qos_type;
import org.pjsip.pjsua2.pjmedia_type;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_status_code;
import org.pjsip.pjsua2.pjsip_transport_type_e;
import org.pjsip.pjsua2.pjsua_call_flag;
import org.pjsip.pjsua2.pjsua_call_media_status;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

import de.greenrobot.event.EventBus;

import static android.media.AudioManager.FLAG_ALLOW_RINGER_MODES;
import static android.media.AudioManager.FLAG_PLAY_SOUND;

/**
 * Created by Ramesh on 13/8/16.
 */
public class YoSipService extends InjectedService implements MyAppObserver, SipServiceHandler {

    private static final String TAG = "YoSipService";
    public static final int DISCONNECT_IF_NO_ANSWER = 60 * 1000;
    public static final int DELAY_IN_AUDIO_START = 5 * 1000;

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
    public static String outgoingCallUri;
    private SipCallState sipCallState;
    private Handler mHandler;
    private String registrationStatus = "";

    private boolean isCallAccepted = false;
    private int statusCode;

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;

    @Inject
    ContactsSyncManager mContactsSyncManager;

    @Inject
    ConnectivityHelper mHelper;

    private Intent mIntent;

    //New changes

    private PowerManager.WakeLock ongoingCallLock;
    private PowerManager.WakeLock eventLock;
    private Endpoint mEndpoint;
    public static String AGENT_NAME = "YO!/" + BuildConfig.VERSION_NAME;


    private boolean localHold = false;
    private boolean localMute = false;
    private static final long[] VIBRATOR_PATTERN = {0, 1000, 1000};

    private MediaPlayer mRingTone;
    private AudioManager mAudioManager;
    private Vibrator mVibrator;
    private Uri mRingtoneUri;
    Ringtone ringtone;

    private boolean isPlayingAudio = false;

    private int callType = -1;
    private String phone;

    private static ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_VOICE_CALL, 100);
    public static final int EXPIRE = 3600;


    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler(Looper.getMainLooper());
        sipCallState = new SipCallState();
        //TODO:Store in shared prefs and retrieve it
        //domain = "209.239.120.239";
        domain = "173.82.147.172";
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
        mRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(YoSipService.this, RingtoneManager.TYPE_RINGTONE);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (!created) {
            startSipService();
        }
        NetworkStateListener.registerNetworkState(listener);
        performAction(intent);
        return START_STICKY;
    }

    private void performAction(Intent intent) {
        if (intent == null) {
            return;
        }
        mIntent = intent;
        if (mHelper.isConnected()) {
            if (VoipConstants.CALL_ACTION_OUT_GOING.equalsIgnoreCase(intent.getAction())) {
                if (currentCall == null) {
                    String number = intent.getStringExtra(OutGoingCallActivity.CALLER_NO);
                    Bundle bundle = intent.getBundleExtra("data");

                    if (bundle == null) {
                        bundle = new Bundle();
                    }
                    int value = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getCallState();
                    if (value == 0) {
                        showCallActivity(number, bundle, intent);
                        makeCall(number, bundle, intent);
                    } else {
                        mToastFactory.showToast("Already call is in progress");
                    }
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
        } else {
            mToastFactory.showToast(getResources().getString(R.string.calls_no_network));
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
    public void notifyIncomingCall(MyCall call, OnIncomingCallParam prms) {
        outgoingCallUri = null;
        /* Incoming call */
        CallOpParam prm = new CallOpParam();
            /* Only one call at anytime */
        //pausePlayingAudio();
        callType = CallLog.Calls.INCOMING_TYPE;
        // if user is already in yo call or if any default phone calls it should show missed call.
        if (currentCall != null || PhoneStateReceiver.isDefaultCall) {
            prm.setStatusCode(pjsip_status_code.PJSIP_SC_BUSY_HERE);
            try {
                String source = getPhoneNumber(call.getInfo().getRemoteUri());
                call.answer(prm);
                source = parseVoxUser(source);
                Util.createNotification(this, source, getResources().getString(R.string.missed_call), BottomTabsActivity.class, new Intent(), false);
                //Util.setBigStyleNotification(this, source, "Missed call", "Missed call", "", false, true, BottomTabsActivity.class, new Intent());
                callType = CallLog.Calls.MISSED_TYPE;
                storeCallLog(source);
            } catch (Exception e) {
                mLog.w(TAG, e);
            }
            // TODO: set status code
            call.delete();
            //call.delete();

            return;
        }
        currentCall = call;
        /*try {
            currentCall.answer(prm);
        } catch (Exception e) {
            mLog.w(TAG, e);
        }*/
        try {
            sipCallState.setCallDir(SipCallState.INCOMING);
            sipCallState.setCallState(SipCallState.CALL_RINGING);
            showInComingCall(call);
            sipCallState.setMobileNumber(getPhoneNumber(call.getInfo().getRemoteUri()));
        } catch (Exception e) {
            mLog.w(TAG, e);
        }
    }

    private void pausePlayingAudio() {
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        isPlayingAudio = mAudioManager.isMusicActive();
        if (isPlayingAudio) {
            Util.sendMediaButton(this, KeyEvent.KEYCODE_MEDIA_PAUSE);
        }
    }

    private synchronized void showInComingCall(MyCall mycall) throws Exception {
        long currentElapsedTime = SystemClock.elapsedRealtime();
        if (lastLaunchCallHandler + LAUNCH_TRIGGER_DELAY < currentElapsedTime) {

            int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
            if (volume == 0) {
                volume = 1;
            }
            startDefaultRingtone(volume);
            Intent intent = new Intent(this, InComingCallActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(InComingCallActivity.CALLER, getPhoneNumber(mycall.getInfo().getRemoteUri()));
            startActivity(intent);
            lastLaunchCallHandler = currentElapsedTime;
            if (preferenceEndPoint.getBooleanPreference(Constants.NOTIFICATION_ALERTS)) {
                inComingCallNotificationId = Util.createNotification(this, parseVoxUser(getPhoneNumber(mycall.getInfo().getRemoteUri())), "Incoming call", InComingCallActivity.class, intent);
/*                inComingCallNotificationId = Notifications.NOTIFICATION_ID;
                Util.setBigStyleNotification(this, parseVoxUser(getPhoneNumber(mycall.getInfo().getRemoteUri())), "Incoming call", "Incoming call", "", true, true, InComingCallActivity.class, intent);*/
            }
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
    public void notifyCallState(@NonNull MyCall call) {

        CallInfo ci;
        try {
            ci = call.getInfo();
        } catch (Exception e) {
            ci = null;
        }

        mLog.e(TAG, "notifyCallState =  " + ci.getState());
        if (ci != null && ci.getState() == pjsip_inv_state.PJSIP_INV_STATE_CALLING) {
            pausePlayingAudio();
        } else if (ci != null && ci.getState() == pjsip_inv_state.PJSIP_INV_STATE_INCOMING) {
            startRingtone();
        } else if (ci != null
                && ci.getState() == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {

            playPausedAudio();

            try {
                //TODO:Handle more error codes to display proper messages to the user
                handlerErrorCodes(call.getInfo(), sipCallState);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (ci != null
                && ci.getState() == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
            stopRingtone();
            callAccepted();
        } else if (ci != null && ci.getState() == pjsip_inv_state.PJSIP_INV_STATE_EARLY) {
            stopRingtone();
        }
    }

    private void handlerErrorCodes(final CallInfo call, final SipCallState sipCallstate) {
        statusCode = call.getLastStatusCode().swigValue();
        mLog.e(TAG, sipCallstate.getMobileNumber() + ",Call Object " + call.toString());
        if (statusCode == 487) {
            callType = CallLog.Calls.MISSED_TYPE;
            callDisconnected();
        } else if (statusCode == 503) {
            mLog.e(TAG, "503 >>> Buddy is not online at this moment. calltype =  " + callType);
            callDisconnected();
        } else if (statusCode == 603) {
            callDisconnected();
        } else if (statusCode == 200) {
            /*try {
                String dumpString = currentCall.dump(true, "");
                mLog.d(TAG, "The dump string is " + dumpString);
            } catch (Exception e) {
                e.printStackTrace();
            }*/
            callDisconnected();
        } else if (statusCode == 480 || statusCode == 486 || statusCode == 404 || statusCode == 403 || statusCode == 408) {
            callDisconnected();
        }
        if (sipCallstate != null && sipCallstate.getMobileNumber() != null) {
            storeCallLog("+"+sipCallstate.getMobileNumber());
        } else if (callType == CallLog.Calls.OUTGOING_TYPE) {
            storeCallLog(phone);
        }


        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String phoneNumber = sipCallstate.getMobileNumber() == null ? sipCallstate.getMobileNumber() : phone;
                Contact contact = mContactsSyncManager.getContactByVoxUserName(phoneNumber);
                if (!isCallAccepted) {
                    statusCode = StatusCodes.TWO_THOUSAND_ONE;
                }
                OpponentDetails details = new OpponentDetails(phoneNumber, contact, statusCode);
                if (isHangup) {
                    details.setSelfReject(true);
                }
                if (statusCode == 603 && !isHangup) {
                    isHangup = !isHangup;
                } else if (statusCode != 603) {
                    isHangup = false;
                } else {
                    isHangup = false;
                }
                EventBus.getDefault().post(details);

            }
        });

        // 603 Decline - when end call
        //503 Service Unavailable  - Buddy is not available
        //603 Allocated Channels Busy -Lines are busy
        // 487 missed call

    }

    private void callAccepted() {
        isCallAccepted = true;
        callStarted = System.currentTimeMillis();
        sipCallState.setStartTime(callStarted);
        sipCallState.setCallState(SipCallState.IN_CALL);
        //mediaManager.stopRingTone();
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
        stopRingtone();
        currentCall = null;
        //callStarted = 0;
        if (sipCallState.getCallDir() == SipCallState.INCOMING) {
            if (sipCallState.getCallState() == SipCallState.CALL_RINGING) {
                mLog.e(TAG, "Missed call >>>>>" + sipCallState.getMobileNumber());
                // callType = CallLog.Calls.MISSED_TYPE;
                Util.createNotification(this,
                        parseVoxUser(sipCallState.getMobileNumber()),
                        "Missed call ", BottomTabsActivity.class, new Intent(), false);
                //Util.setBigStyleNotification(this, parseVoxUser(sipCallState.getMobileNumber()), "Missed call", "Missed call", "", false, true, BottomTabsActivity.class, new Intent());

            }
        }
        sipCallState.setCallState(SipCallState.CALL_FINISHED);
        //Reset
        sipCallState = new SipCallState();
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.sendBroadcast(new Intent(UserAgent.ACTION_CALL_END));
    }

    private void playPausedAudio() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isPlayingAudio) {
                    //previously playing true before attend the call
                    Util.sendMediaButton(YoSipService.this, KeyEvent.KEYCODE_MEDIA_PLAY);
                }
            }
        }, DELAY_IN_AUDIO_START);
    }

    @Override
    public void notifyCallMediaState(MyCall call) {

    }

    @Override
    public void notifyBuddyState(MyBuddy buddy) {

    }

    private MyAccount buildAccount() throws UnsatisfiedLinkError {
        if (myAccount != null) {
            return myAccount;
        }
        AccountConfig accCfg = new AccountConfig();
        accCfg.getRegConfig().setTimeoutSec(YoSipService.EXPIRE);
        accCfg.setIdUri("sip:localhost");
        accCfg.getNatConfig().setIceEnabled(true);
        accCfg.getVideoConfig().setAutoTransmitOutgoing(true);
        accCfg.getVideoConfig().setAutoShowIncoming(true);
        if (myApp == null) {
            startSipService();
        }
        return myApp.addAcc(accCfg);
    }

    public void addAccount(SipProfile sipProfile) {
        try {
            //startStack();
            myAccount = buildAccount();
            String id = String.format("sip:%s@%s", sipProfile.getUsername(), sipProfile.getDomain());
            String registrar = String.format("sip:%s:%s", sipProfile.getDomain(), 5060);
            String proxy = String.format("sip:%s:%s", sipProfile.getDomain(), 5060);
            String username = sipProfile.getUsername();
            String password = sipProfile.getPassword();
            if (myAccount != null) {
                configAccount(myAccount.cfg, id, registrar, proxy, username, password);
                try {
                    myAccount.cfg.getRegConfig().setTimeoutSec(YoSipService.EXPIRE);
                    myAccount.modify(myAccount.cfg);
                } catch (Exception e) {
                    mLog.w(TAG, e);
                }
            } else {
                mLog.w(TAG, "Created account object is null");
            }
        } catch (Exception | UnsatisfiedLinkError e) {
            mLog.w(TAG, e);
        }

    }

    private void configAccount(AccountConfig accCfg, String acc_id, String registrar, String proxy,
                               String username, String password) {
        accCfg.setIdUri(acc_id);
        accCfg.getRegConfig().setRegistrarUri(registrar);
        AuthCredInfoVector creds = accCfg.getSipConfig().getAuthCreds();
        creds.clear();
        if (username != null && !username.isEmpty() && username.length() != 0) {
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

    public void makeCall(String destination, Bundle options, Intent intent) {
        phone = destination;
        isCallAccepted = false;
        if (destination != null && !destination.startsWith("sip:")) {
            destination = "sip:" + destination;
        }
        String finalUri = String.format("%s@%s", destination, getDomain());
        mLog.e(TAG, "Final uri to make a call " + finalUri);
        outgoingCallUri = finalUri;
        /* Only one call at anytime */
        if (currentCall != null) {
            return;
        }

        if (myAccount == null) {
            myAccount = buildAccount();
        }


        callType = CallLog.Calls.OUTGOING_TYPE;
        if (myAccount != null) {

            MyCall call = new MyCall(myAccount, -1);
            CallOpParam prm = new CallOpParam(true);
            try {
                //call.isActive(finalUri, prm);
                call.isActive();
                call.makeCall(finalUri, prm);

            } catch (Exception e) {
                mLog.w(TAG, "Exception making call " + e.getMessage());
                call.delete();
                return;
            }

            currentCall = call;
            //showCallActivity(phone, options, intent);
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mToastFactory.showToast(R.string.call_account_null);
                }
            });
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isCallAccepted) {
                    //After one minute there is no response/call back for answered call
                    hangupCall(callType);
                }
            }
        }, DISCONNECT_IF_NO_ANSWER);
    }

    private void showCallActivity(String destination, Bundle options, Intent oldintent) {
        //Always set default speaker off
        sipCallState.setCallDir(SipCallState.OUTGOING);
        sipCallState.setCallState(SipCallState.CALL_RINGING);
        sipCallState.setMobileNumber(destination);
        //For PSTN calls ringtone is playing from library but app to app calls its not playing.
        if (!oldintent.hasExtra(VoipConstants.PSTN)) {
            startDefaultRingtone(1);
        }
        Intent intent = new Intent(this, OutGoingCallActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("data", options);
        intent.putExtra(OutGoingCallActivity.CALLER_NO, destination);
        intent.putExtra(VoipConstants.PSTN, oldintent.hasExtra(VoipConstants.PSTN));
        intent.putExtra(OutGoingCallActivity.DISPLAY_NUMBER, oldintent.getStringExtra(OutGoingCallActivity.DISPLAY_NUMBER));


        startActivity(intent);
        destination = parseVoxUser(destination);
        if (preferenceEndPoint.getBooleanPreference(Constants.NOTIFICATION_ALERTS)) {
            outGoingCallNotificationId = Util.createNotification(this, destination, "Outgoing call", OutGoingCallActivity.class, intent);
            /*outGoingCallNotificationId = Notifications.NOTIFICATION_ID;
            Util.setBigStyleNotification(this, destination, "Outgoing call", "Outgoing call", "", true, true, OutGoingCallActivity.class, intent);*/
        }
    }

    private String parseVoxUser(String destination) {
        Contact contact = mContactsSyncManager.getContactByVoxUserName(destination);
        CallerInfo info = new CallerInfo();
        if (contact != null) {
            if (contact.getName() != null) {
                destination = contact.getName();
            } else if (contact.getPhoneNo() != null) {
                destination = contact.getPhoneNo();
            }
        }
        return destination;
    }

    public String getDomain() {
        return domain;
    }

    public void setHoldCall(boolean isHold) {
        if (isHold) {
            mToastFactory.showToast(R.string.hold);
            getMediaManager().setMicrophoneMuteOn(true);
            try {
                if (mRingTone != null && mRingTone.isPlaying()) {
                    mRingTone.pause();
                    mVibrator.cancel();
                }
            } catch (IllegalStateException e) {
                mLog.w(TAG, e);
            }
            if (currentCall != null) {
                CallOpParam prm = new CallOpParam(true);
                prm.getOpt().setFlag(pjsua_call_flag.PJSUA_CALL_UPDATE_CONTACT.swigValue());
                try {

                    currentCall.setHold(prm);
                } catch (Exception e) {
                    mLog.w(TAG, e);
                }
            }
        } else {
            if (currentCall != null) {
                CallOpParam prm = new CallOpParam(true);
                prm.getOpt().setFlag(pjsua_call_flag.PJSUA_CALL_UNHOLD.swigValue());
                getMediaManager().setMicrophoneMuteOn(false);
                try {
                    if (currentCall.getInfo() != null
                            && currentCall.getInfo().getState() != pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
                        if (mRingTone != null) {
                            mRingTone.start();
                            mVibrator.vibrate(VIBRATOR_PATTERN, 0);
                        }
                    }
                    currentCall.reinvite(prm);
                    mToastFactory.showToast(R.string.unhold);
                } catch (Exception e) {
                    mLog.w(TAG, e);
                }
            }
        }
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        super.unbindService(conn);
        NetworkStateListener.unregisterNetworkState(listener);
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

    NetworkStateChangeListener listener = new NetworkStateChangeListener() {
        public void onNetworkStateChanged(int networkstate) {
            mLog.w(TAG, "Network change listener and state is " + networkstate);

            if (networkstate == NetworkStateListener.NETWORK_CONNECTED) {
                // Network is connected.
            } else {
                hangupCall(callType);
                OpponentDetails details = new OpponentDetails(null, null, 404);
                EventBus.getDefault().post(details);
            }
        }
    };

    public void acceptCall() {
        CallOpParam prm = new CallOpParam();
        prm.setStatusCode(pjsip_status_code.PJSIP_SC_OK);
        try {
            if (currentCall != null)
                currentCall.answer(prm);
            sipCallState.setCallState(SipCallState.IN_CALL);
        } catch (Exception e) {
            mLog.w(TAG, e);
        }
    }

    private boolean isHangup;

    public void hangupCall(int callType) {
        Util.cancelNotification(this, inComingCallNotificationId);
        Util.cancelNotification(this, outGoingCallNotificationId);

        if (currentCall != null) {
            CallOpParam prm = new CallOpParam();
            prm.setStatusCode(pjsip_status_code.PJSIP_SC_DECLINE);
            try {
                if (currentCall != null)
                    currentCall.hangup(prm);
                isHangup = true;
                mLog.i(TAG, "check : 1");
                //sipCallState.setCallState(SipCallState.CALL_FINISHED);
            } catch (Exception e) {
                mLog.w(TAG, e);
            }
            //callDisconnected();
        }
    }

    private void storeCallLog(String mobileNumber) {
        long currentTime = System.currentTimeMillis();
        long callDuration = TimeUnit.MILLISECONDS.toSeconds(currentTime - callStarted);
        if (callStarted == 0 || callType == -1) {
            callDuration = 0;
        }
        callStarted = 0;
        int pstnorapp = 0;
        Contact contact = mContactsSyncManager.getContactByVoxUserName(mobileNumber);
        CallerInfo info = new CallerInfo();
        if (contact != null && contact.getName() != null) {
            info.name = contact.getName();
            pstnorapp = CallLog.Calls.APP_TO_APP_CALL;
        } else {
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            try {
                // phone must begin with '+'
                Phonenumber.PhoneNumber numberProto = phoneUtil.parse("+" + mobileNumber, "");
                int countryCode = numberProto.getCountryCode();
                String mobileTemp = mobileNumber;
                String phoneNumber = mobileTemp.replace(countryCode + "", "");
                contact = mContactsSyncManager.getContactPSTN(countryCode, phoneNumber);
            } catch (NumberParseException e) {
                mLog.e(TAG, "NumberParseException was thrown: " + e.toString());
            }
            if (contact != null && contact.getName() != null) {
                info.name = contact.getName();
            }
            pstnorapp = CallLog.Calls.APP_TO_PSTN_CALL;
        }
        CallLog.Calls.addCall(info, getBaseContext(), mobileNumber, callType, callStarted, callDuration, pstnorapp);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Util.cancelNotification(this, inComingCallNotificationId);
        Util.cancelNotification(this, outGoingCallNotificationId);
        if (currentCall != null) {
            CallOpParam prm = new CallOpParam();
            prm.setStatusCode(pjsip_status_code.PJSIP_SC_DECLINE);
            try {
                currentCall.hangup(prm);
                isHangup = true;
                sipCallState.setCallState(SipCallState.CALL_FINISHED);
                //ReCreateService.getInstance(this).start(this);
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


    //New changes

    public void initService(YoSipService srv) {
        //  pjService = srv;
        // notificationManager = pjService.service.notificationManager;

        /*if (handlerThread == null) {
            handlerThread = new HandlerThread("UAStateAsyncWorker");
            handlerThread.start();
        }
        if (msgHandler == null) {
            msgHandler = new WorkerHandler(handlerThread.getLooper(), this);
        }*/

        if (eventLock == null) {
            PowerManager pman = (PowerManager) getSystemService(Context.POWER_SERVICE);
            eventLock = pman.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "com.csipsimple.inEventLock");
            eventLock.setReferenceCounted(true);

        }
        if (ongoingCallLock == null) {
            PowerManager pman = (PowerManager) getSystemService(Context.POWER_SERVICE);
            ongoingCallLock = pman.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "com.csipsimple.ongoingCallLock");
            ongoingCallLock.setReferenceCounted(false);
        }
    }

    public void stopService() {

      /*  Threading.stopHandlerThread(handlerThread, true);
        handlerThread = null;
        msgHandler = null;
*/
        // Ensure lock is released since this lock is a ref counted one.
        if (eventLock != null) {
            while (eventLock.isHeld()) {
                eventLock.release();
            }
        }
        if (ongoingCallLock != null) {
            if (ongoingCallLock.isHeld()) {
                ongoingCallLock.release();
            }
        }
    }

    /**
     * Starts PJSIP Stack.
     */
    private void startStack() {

        //if (mStarted) return;

        try {
            Logger.warn("Starting PJSIP");
            mEndpoint = new Endpoint();
            mEndpoint.libCreate();

            EpConfig epConfig = new EpConfig();
            epConfig.getUaConfig().setUserAgent(AGENT_NAME);
            epConfig.getMedConfig().setHasIoqueue(true);
            epConfig.getMedConfig().setClockRate(16000);
            epConfig.getMedConfig().setQuality(10);
            epConfig.getMedConfig().setEcOptions(1);
            epConfig.getMedConfig().setEcTailLen(200);
            epConfig.getMedConfig().setThreadCnt(2);
            mEndpoint.libInit(epConfig);

            TransportConfig udpTransport = new TransportConfig();
            udpTransport.setQosType(pj_qos_type.PJ_QOS_TYPE_VOICE);
            TransportConfig tcpTransport = new TransportConfig();
            tcpTransport.setQosType(pj_qos_type.PJ_QOS_TYPE_VOICE);

            mEndpoint.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_UDP, udpTransport);
            mEndpoint.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_TCP, tcpTransport);
            mEndpoint.libStart();
           mEndpoint.codecSetPriority("PCMA/8000", (short) (CodecPriority.PRIORITY_MAX - 1));
            mEndpoint.codecSetPriority("PCMU/8000", (short) (CodecPriority.PRIORITY_MAX - 2));
            mEndpoint.codecSetPriority("speex/8000", (short) CodecPriority.PRIORITY_DISABLED);
            mEndpoint.codecSetPriority("speex/16000", (short) CodecPriority.PRIORITY_DISABLED);
            mEndpoint.codecSetPriority("speex/32000", (short) CodecPriority.PRIORITY_DISABLED);
            mEndpoint.codecSetPriority("GSM/8000", (short) CodecPriority.PRIORITY_DISABLED);
            mEndpoint.codecSetPriority("G722/16000", (short) CodecPriority.PRIORITY_DISABLED);
            mEndpoint.codecSetPriority("G7221/16000", (short) CodecPriority.PRIORITY_DISABLED);
            mEndpoint.codecSetPriority("G7221/32000", (short) CodecPriority.PRIORITY_DISABLED);
            mEndpoint.codecSetPriority("ilbc/8000", (short) CodecPriority.PRIORITY_DISABLED);



            Logger.warn("PJSIP started!");
            // mStarted = true;
            // mBroadcastEmitter.stackStatus(true);

        } catch (Exception exc) {
            Logger.warn("Error while starting PJSIP");
            //mStarted = false;
        }
    }

    /**
     * Shuts down PJSIP Stack
     *
     * @throws Exception if an error occurs while trying to shut down the stack
     */
    private void stopStack() {

        // if (!mStarted) return;

        try {
            Logger.warn("Stopping PJSIP");

            //  removeAllActiveAccounts();

            // try to force GC to do its job before destroying the library, since it's
            // recommended to do that by PJSUA examples
            Runtime.getRuntime().gc();

            mEndpoint.libDestroy();
            mEndpoint.delete();
            mEndpoint = null;

            Logger.warn("PJSIP stopped");
            // mBroadcastEmitter.stackStatus(false);

        } catch (Exception exc) {
            Logger.warn("Error while stopping PJSIP");

        } finally {
            // mStarted = false;
            mEndpoint = null;
        }
    }

    /**
     * Utility method to mute/unmute the device microphone during a call.
     *
     * @param mute true to mute the microphone, false to un-mute it
     */
    public void setMute(boolean mute) {
        // return immediately if we are not changing the current state
        if ((localMute && mute) || (!localMute && !mute)) return;

        CallInfo info;
        try {
            info = getInfo();
        } catch (Exception exc) {
            Logger.warn("setMute: error while getting call info");
            return;
        }

        for (int i = 0; i < info.getMedia().size(); i++) {
            Media media = null;// getMedia(i);
            CallMediaInfo mediaInfo = info.getMedia().get(i);

            if (mediaInfo.getType() == pjmedia_type.PJMEDIA_TYPE_AUDIO
                    && media != null
                    && mediaInfo.getStatus() == pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE) {
                AudioMedia audioMedia = AudioMedia.typecastFromMedia(media);

                // connect or disconnect the captured audio
                try {
                    AudDevManager mgr = null;//account.getService().getAudDevManager();

                    if (mute) {
                        mgr.getCaptureDevMedia().stopTransmit(audioMedia);
                        localMute = true;
                    } else {
                        mgr.getCaptureDevMedia().startTransmit(audioMedia);
                        localMute = false;
                    }

                } catch (Exception exc) {
                    Logger.warn("setMute: error while connecting audio media to sound device");
                }
            }
        }
    }

    public boolean isLocalMute() {
        return localMute;
    }

    public boolean toggleMute() {
        if (localMute) {
            setMute(false);
            return !localHold;
        }

        setMute(true);
        return localHold;
    }

    private void startRingBack() {
        if (toneGenerator != null) {
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_NETWORK_USA_RINGBACK, 1000);
        }
    }

    private void stopRingBack() {
        if (toneGenerator != null) {
            toneGenerator.stopTone();
        }
    }

    protected synchronized void startDefaultRingtone(int volume) {

        try {

            mAudioManager.setSpeakerphoneOn(false);
            mRingTone = MediaPlayer.create(this, R.raw.calling);
            mRingTone.setVolume(volume, volume);
            mRingTone.setLooping(true);
            mAudioManager.setMode(AudioManager.RINGER_MODE_SILENT);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        mRingTone.start();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }, 1000);
        } catch (Exception exc) {
            Logger.warn("Error while trying to play ringtone!" + exc.getMessage());
        }

    }

    protected synchronized void startRingtone() {
        mVibrator.vibrate(VIBRATOR_PATTERN, 0);
        try {
            mRingTone = MediaPlayer.create(this, mRingtoneUri);
            mRingTone.setLooping(true);
            int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
            mAudioManager.setSpeakerphoneOn(false);
            mAudioManager.setMode(AudioManager.MODE_IN_CALL);
            mRingTone.setVolume(volume, volume);
            mRingTone.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }
            });
            mRingTone.start();
        } catch (Exception exc) {
            Logger.warn("Error while trying to play ringtone!");
        }
    }

    protected synchronized void stopRingtone() {
        try {
            if (ringtone != null) {
                ringtone.stop();
                ringtone = null;
            }
        } catch (Exception exc) {
            Logger.warn("Error while trying to play ringtone!");
        }

        mVibrator.cancel();

        if (mRingTone != null) {
            try {
                if (mRingTone.isPlaying())
                    mRingTone.stop();
            } catch (Exception ignored) {
            }
            try {
                mRingTone.reset();
                mRingTone.release();
            } catch (Exception ignored) {
            }
        }
    }

    protected synchronized AudDevManager getAudDevManager() {
        return mEndpoint.audDevManager();
    }

}

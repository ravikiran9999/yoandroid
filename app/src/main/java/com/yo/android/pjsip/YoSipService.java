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
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.Toast;

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
import com.yo.android.usecase.WebserviceUsecase;
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
import com.yo.android.voip.CallEvents;
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
import org.pjsip.pjsua2.Endpoint;
import org.pjsip.pjsua2.OnIncomingCallParam;
import org.pjsip.pjsua2.StreamStat;
import org.pjsip.pjsua2.StringVector;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_status_code;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

import de.greenrobot.event.EventBus;

/**
 * Created by Ramesh on 13/8/16.
 */
public class YoSipService extends InjectedService implements MyAppObserver, SipServiceHandler, CallEvents {

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
    @Inject
    public WebserviceUsecase webserviceUsecase;
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
    private CallDisconnectedListner callDisconnectedListner;

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
    private boolean isOnGoingCall = false;
    private boolean mySelfEndCall = false;
    private boolean isCallDeleted;
    private boolean isPSTN;
    private String number;

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler(Looper.getMainLooper());
        sipCallState = new SipCallState();
        //TODO:Store in shared prefs and retrieve it
        //domain = "209.239.120.239";
        //domain = "173.82.147.172";
        domain = "185.106.240.205";
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
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        if (intent != null && intent.hasExtra(OutGoingCallActivity.CALLER_NO)) {
            mLog.d(TAG, "In the onStartCommand() of YoSipService");
            mRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(YoSipService.this, RingtoneManager.TYPE_RINGTONE);
            mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            //created = preferenceEndPoint.getBooleanPreference(Constants.CREATED);
            mLog.e(TAG, created + ".....");
            if (!created) {
                startSipService();
                // preferenceEndPoint.saveBooleanPreference(Constants.CREATED, created);
            }
            number = intent.getStringExtra(OutGoingCallActivity.CALLER_NO);

            isPSTN = intent.hasExtra(VoipConstants.PSTN);
           /* if (myAccount == null) {
                addAccount(isPSTN, number);
            }
            NetworkStateListener.registerNetworkState(listener);
            performAction(intent);*/
        }
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
                    boolean validPhoneNumberForPstnCalls = isValidPhoneNumberForPstnCalls(number, intent);
                    if (validPhoneNumberForPstnCalls || number.contains(BuildConfig.RELEASE_USER_TYPE)) {
                        if (bundle == null) {
                            bundle = new Bundle();
                        }
                        int value = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getCallState();
                        if (value == 0) {
                            isCallDeleted = false;
                            showCallActivity(number, bundle, intent);
                            makeCall(number, bundle, intent);
                        } else {
                            mToastFactory.showToast("Already call is in progress");
                        }
                    } else {
                        storeCallLog(number);
                        if (!validPhoneNumberForPstnCalls) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mToastFactory.showToast(getString(R.string.not_valid_pstn_number));
                                }
                            });
                        }
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
                // preferenceEndPoint.saveBooleanPreference(Constants.CREATED, created);
                stopSelf();
            }
        } else {
            mToastFactory.showToast(getResources().getString(R.string.calls_no_network));
        }

    }


    private void startSipService() {
        myApp = new MyApp();
       // myApp.register(this, getFilesDir().getAbsolutePath());
        created = true;
        // preferenceEndPoint.saveBooleanPreference(Constants.CREATED, created);

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
        try {
            String sources = getPhoneNumber(call.getInfo().getRemoteUri());
            mLog.e("YoSipService", "notifyIncomingCall>>>> %s", sources);

        } catch (Exception e) {
            e.printStackTrace();
        }


        CallOpParam prm = new CallOpParam();
            /* Only one call at anytime */
        //pausePlayingAudio();
        callType = CallLog.Calls.INCOMING_TYPE;
        // if user is already in yo call or if any default phone calls it should show missed call.
        if (currentCall != null) {
            try {
                String source = getPhoneNumber(call.getInfo().getRemoteUri());
                sendBusyHereToIncomingCall();
                source = parseVoxUser(source);
                Util.createNotification(this, source, getResources().getString(R.string.missed_call), BottomTabsActivity.class, new Intent(), false);
                //Util.setBigStyleNotification(this, source, "Missed call", "Missed call", "", false, true, BottomTabsActivity.class, new Intent());
                callType = CallLog.Calls.MISSED_TYPE;
                storeCallLog(source);
            } catch (Exception e) {
                mLog.w(TAG, e);
            }

            // Dont remove below logic.
            isCallDeleted = true;
            call.delete();

            return;
        }
        currentCall = call;

        try {
            sipCallState.setCallDir(SipCallState.INCOMING);
            sipCallState.setCallState(SipCallState.CALL_RINGING);
            showInComingCall(call);
            isOnGoingCall = true;
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
/*                inComingCallNotificationId = Notifications.CHAT_NOTIFICATION_ID;
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
        mLog.d(TAG, "Remote uri " + remoteUriStr + " Part 2 " + part2);
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
            if (mIntent != null && !mIntent.hasExtra(VoipConstants.PSTN)) {
                startDefaultRingtone(1);
            }
        } else if (ci != null && ci.getState() == pjsip_inv_state.PJSIP_INV_STATE_INCOMING) {
            startRingtone();
        } else if (ci != null
                && ci.getState() == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {

            // playPausedAudio()

            try {
                //TODO:Handle more error codes to display proper messages to the user
                statusCode = call.getInfo().getLastStatusCode().swigValue();
                mLog.e(TAG, "notifyCallState = Status code  " + statusCode);

                handleErrorCodes(sipCallState, call.getInfo().getLastReason(), call.getInfo().getLastStatusCode());
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (ci != null
                && ci.getState() == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
            stopRingtone();
            callAccepted(call);
        }/* else if (ci != null && ci.getState() == pjsip_inv_state.PJSIP_INV_STATE_EARLY) {
            stopRingtone();
        }*/
    }


    private void handleErrorCodes(final SipCallState sipCallstate, String message, pjsip_status_code code) {
        if (!mySelfEndCall) {
            pjsip_status_code lastStatusCode = code;
            mLog.d(TAG, "The lastStatusCode code is " + lastStatusCode + " and statusCode is " + statusCode);
            if (lastStatusCode == pjsip_status_code.PJSIP_SC_REQUEST_TERMINATED || lastStatusCode == pjsip_status_code.PJSIP_SC_OK) {
                //mToastFactory.showToast(R.string.call_ended);
            } else if (lastStatusCode == pjsip_status_code.PJSIP_SC_DECLINE) {
                showFailedToast(getString(R.string.call_ended));
            } else if (lastStatusCode == pjsip_status_code.PJSIP_SC_BUSY_HERE || lastStatusCode == pjsip_status_code.PJSIP_SC_INTERNAL_SERVER_ERROR) {
                showFailedToast(getString(R.string.busy));
            } else if (lastStatusCode == pjsip_status_code.PJSIP_SC_NOT_FOUND) {
                showFailedToast(getString(R.string.not_online_unavailable));
                if (sipCallstate != null && sipCallstate.getMobileNumber() != null) {
                    Contact contact = mContactsSyncManager.getContactByVoxUserName(sipCallstate.getMobileNumber());
                    OpponentDetails details = new OpponentDetails(sipCallstate.getMobileNumber(), contact, statusCode);
                    EventBus.getDefault().post(details);
                }
            } else if (lastStatusCode == pjsip_status_code.PJSIP_SC_REQUEST_TIMEOUT || lastStatusCode == pjsip_status_code.PJSIP_SC_TEMPORARILY_UNAVAILABLE) {
                showFailedToast(getString(R.string.not_in_coverage_area));
            } else if (lastStatusCode == pjsip_status_code.PJSIP_SC_FORBIDDEN) {
                EventBus.getDefault().post(Constants.BALANCE_RECHARGE_ACTION);
            } else {
                if (statusCode != 503) {
                    showFailedToast(message);
                }
            }

        }

        if (statusCode == 487) {
            callType = CallLog.Calls.MISSED_TYPE;
            callDisconnected();
        } else if (statusCode == 503) {
            mLog.e(TAG, "503 >>> Buddy is not online at this moment. calltype =  " + callType);
            callDisconnected();
            if (sipCallstate != null && !sipCallstate.getMobileNumber().contains(BuildConfig.RELEASE_USER_TYPE)) {
                showFailedToast(getString(R.string.not_supported_country));
            } else {
                showFailedToast(getString(R.string.not_online));
                if (sipCallstate != null && sipCallstate.getMobileNumber() != null) {
                    Contact contact = mContactsSyncManager.getContactByVoxUserName(sipCallstate.getMobileNumber());
                    OpponentDetails details = new OpponentDetails(sipCallstate.getMobileNumber(), contact, statusCode);
                    EventBus.getDefault().post(details);
                }
            }
        } else if (statusCode == 603) {
            callDisconnected();
        } else {
            callDisconnected();
        }
        if (sipCallstate != null && sipCallstate.getMobileNumber() != null) {
            storeCallLog(sipCallstate.getMobileNumber());
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

    private void showFailedToast(final String message) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!mySelfEndCall) {
                    mToastFactory.showToast(message);
                } else {
                    mySelfEndCall = !mySelfEndCall;
                }

            }
        });
    }

    private void callAccepted(MyCall ci) {
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
        startRepeatingTask(ci);
    }

    private void callDisconnected() {
        isOnGoingCall = false;
        if (callDisconnectedListner != null) {
            callDisconnectedListner.callDisconnected();
        }
        mLog.e(TAG, "disconnected call >>>>>");

        try {
            String dumpString = currentCall.dump(true, "");
            mLog.d(TAG, "The call disconnected dump string is " + dumpString);
            Util.appendLog(dumpString);
        } catch (Exception e) {
            e.printStackTrace();
        }

        stopRepeatingTask();
        Util.cancelNotification(this, inComingCallNotificationId);
        Util.cancelNotification(this, outGoingCallNotificationId);
        mediaManager.setAudioMode(AudioManager.MODE_NORMAL);
        stopRingtone();
        isCallDeleted = true;
        currentCall = null;
        isCallDeleted = true;
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
        EventBus.getDefault().post(OutGoingCallActivity.DISCONNECTED);
        if (mEndpoint != null) {
            try {
                mEndpoint.libDestroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //
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
        try {
            mLog.e(TAG, "notifyCallState Media =  " + call.getInfo().getLastReason());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void notifyBuddyState(MyBuddy buddy) {
        if (buddy != null) {
            buddy.getStatusText();
        }

    }

    private MyAccount buildAccount(String id, String msg) throws UnsatisfiedLinkError {
        if (myAccount != null) {
            return myAccount;
        }
        AccountConfig accCfg = new AccountConfig();
        accCfg.setIdUri(id);
        accCfg.getRegConfig().setTimeoutSec(YoSipService.EXPIRE);
        accCfg.getNatConfig().setIceEnabled(false);
        accCfg.getVideoConfig().setAutoTransmitOutgoing(true);
        accCfg.getVideoConfig().setAutoShowIncoming(true);
        if (myApp == null) {
            startSipService();
        }

        accCfg.getNatConfig().setIceEnabled(false);
             /* Enable ICE/TURN */

        accCfg.getNatConfig().setTurnEnabled(true);
        /*accCfg.getNatConfig().setTurnServer("turn.pjsip.org:33478");
        accCfg.getNatConfig().setTurnUserName("abzlute01");
        accCfg.getNatConfig().setTurnPasswordType(0);
        accCfg.getNatConfig().setTurnPassword("abzlute01");*/
/*        accCfg.getNatConfig().setTurnServer("34.230.108.83:3478");
        accCfg.getNatConfig().setTurnUserName("tadmin");
        accCfg.getNatConfig().setTurnPasswordType(0);
        accCfg.getNatConfig().setTurnPassword("test123");*/
        //accCfg.getNatConfig().setTurnConnType(pj_turn_tp_type.PJ_TURN_TP_TCP);
        android.util.Log.d(TAG, msg + " Setting TURN server");
        return myApp.addAcc(accCfg);
    }

    private String addAccount(boolean isPSTN, String number) {

        String username = preferenceEndPoint.getStringPreference(Constants.VOX_USER_NAME, null);
        String password = preferenceEndPoint.getStringPreference(Constants.PASSWORD, null);

        SipProfile sipProfile = new SipProfile.Builder()
                .withUserName(username == null ? "" : username)
                .withPassword(password)
                //.withServer("173.82.147.172")
                .withServer("185.106.240.205")

                // .withServer("pjsip.org")
                .build();
        return addAccount(sipProfile, isPSTN, number);

    }


    public String addAccount(SipProfile sipProfile, boolean isPSTN, String number) {
        String id = null;
        try {
            String displayname;
            //startStack();
            String usernameDisplayName = sipProfile.getUsername();

            if (isPSTN) {
                displayname = usernameDisplayName.substring(usernameDisplayName.indexOf(BuildConfig.RELEASE_USER_TYPE) + 6, usernameDisplayName.length() - 1);
                // if local number dont add country code
                //String countryCode = preferenceEndPoint.getStringPreference(Constants.COUNTRY_CODE_FROM_SIM, null);
                //displayname = countryCode + displayname;
            } else {
                displayname = usernameDisplayName;
            }
            id = String.format("\"%s\"<sip:%s@%s>", displayname, usernameDisplayName, sipProfile.getDomain());
            mLog.w(TAG, "SIP ID "+id);
            myAccount = buildAccount(id, "Start");
            // this is for sip to sip should send sip  number as displayname otherwise phone number need to parse from sip number

            updateUserDetails(sipProfile, usernameDisplayName, displayname);
        } catch (Exception | UnsatisfiedLinkError e) {
            mLog.w(TAG, e);
        }
        return id;

    }

    private void updateUserDetails(SipProfile sipProfile, String usernameDisplayName, String displayname) {
        String id = String.format("\"%s\"<sip:%s@%s>", displayname, usernameDisplayName, sipProfile.getDomain());
        String registrar = String.format("sip:%s:%s", sipProfile.getDomain(), 5060);
        //String registrar = String.format("sip:%s:%s", "pjsip.org", 5060);

        String proxy = String.format("sip:%s:%s", sipProfile.getDomain(), 5060);
        //String proxy = String.format("sip:%s:%s", "pjsip.org", 5060);
        String username = usernameDisplayName;
        String password = sipProfile.getPassword();
        if (myAccount != null) {
            mLog.w(TAG, "Display Name " + id);

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
    }


    private void configAccount(AccountConfig accCfg, String acc_id, String registrar, String proxy,
                               String username, String password) {

        accCfg.setIdUri(acc_id);
        mLog.w(TAG, "username and password "+username+"==="+password);

        accCfg.getRegConfig().setRegistrarUri(registrar);
        AuthCredInfoVector creds = accCfg.getSipConfig().getAuthCreds();
        creds.clear();
        if (username != null && !username.isEmpty() && username.length() != 0) {

            creds.add(new AuthCredInfo("Digest", "*", username, 0, password));
        }

        /*//  StringVector proxies = accCfg.getSipConfig().getProxies();
        StringVector proxies = new StringVector();
        //proxies.add("sip:sip.pjsip.org;transport=tcp");
        proxies.add("sip:173.82.147.172:6000;transport=tcp");
        //proxies.add("sip:sip.pjsip.org;transport=tls");
        //proxies.add("sip:sip.pjsip.org:5080;transport=tcp");*/

        StringVector proxies = accCfg.getSipConfig().getProxies();
        // above code is giving proxies size is 0


       /* StringVector proxies = new StringVector();
        proxies.add("sip:173.82.147.172:5060;transport=tcp");*/

        accCfg.getSipConfig().setProxies(proxies);
        proxies.clear();
        if (proxy.length() != 0) {
            proxies.add(proxy);
        }

		/* Enable ICE */
        accCfg.getNatConfig().setIceEnabled(false);
    }

    public void makeCall(String destination, Bundle options, Intent intent) {
        phone = destination;
        isCallAccepted = false;
        if (destination != null && !destination.startsWith("sip:")) {
            destination = "sip:" + destination;
        }

        String finalUri = String.format("%s@%s", destination, getDomain());
        // String finalUri = String.format("%s", "sip:866@pjsip.org");

        mLog.e(TAG, "Final uri to make a call " + finalUri);
        outgoingCallUri = finalUri;
        /* Only one call at anytime */
        if (currentCall != null) {
            return;
        }


        if (myAccount == null) {
            myAccount = buildAccount(addAccount(isPSTN, number), "Make Call");
        }


        callType = CallLog.Calls.OUTGOING_TYPE;
        if (myAccount != null) {

            final MyCall call = new MyCall(myAccount, -1);


            CallOpParam prm = new CallOpParam(true);
            try {
                call.makeCall(finalUri, prm);
                isOnGoingCall = true;
            } catch (Exception e) {
                e.printStackTrace();
                mLog.w(TAG, "Exception making call " + e.getMessage());
                isCallDeleted = true;
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
                    //hangupCall(callType);
                }
            }
        }, DISCONNECT_IF_NO_ANSWER);
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                if (currentCall != null && !isCallDeleted) {
                    updateStatus(currentCall); //this function can change value of mInterval.
                } else {
                    mLog.w(TAG, "Call status update reconnecting .. else info null");
                }
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, 100);
            }
        }
    };

    private static long currentBytes;
    int count;
    boolean isAlreadyInReconnecting;

    private void updateStatus(final MyCall call) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!localHold) {
                        if (currentCall != null && !isCallDeleted) {
                            final StreamStat stats = currentCall.getStreamStat(0);
                            if (currentBytes != stats.getRtcp().getRxStat().getBytes()) {
                                count = 0;
                                currentBytes = stats.getRtcp().getRxStat().getBytes();
                                SipCallModel callModel = new SipCallModel();
                                callModel.setEvent(OutGoingCallActivity.CALL_ACCEPTED_START_TIMER);
                                callModel.setOnCall(true);
                                EventBus.getDefault().post(callModel);
                            } else {
                                count++;
                            }

                            //mLog.w(TAG, "UpdateStatus get bytes:  " + stats.getRtcp().getRxStat().getBytes());
                            //mLog.w(TAG, "UpdateStatus Count:  " + count);
                            if (count > 5 && count <= 20) {
                                SipCallModel callModel = new SipCallModel();
                                callModel.setEvent(SipCallModel.RECONNECTING);
                                EventBus.getDefault().post(callModel);
                            }
                            if (count > 600) {
                                if (currentCall != null) {
                                    mLog.e(TAG, "Disconnecting call from UPDATE STATUS  ");
                                    hangupCall(callType);
                                }
                                callDisconnected();
                                count = 0;
                                isAlreadyInReconnecting = false;
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    void startRepeatingTask(MyCall ci) {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }


    private void showCallActivity(String destination, Bundle options, Intent oldintent) {
        //Always set default speaker off
        mAudioManager.setSpeakerphoneOn(false);
        sipCallState.setCallDir(SipCallState.OUTGOING);
        sipCallState.setCallState(SipCallState.CALL_RINGING);
        sipCallState.setMobileNumber(destination);
        String displayName = parseVoxUser(destination);

        //For PSTN calls ringtone is playing from library but app to app calls its not playing.
        if (!oldintent.hasExtra(VoipConstants.PSTN)) {
            startDefaultRingtone(1);
        } else {
            if (myAccount != null) {
                String currentUserName = parseVoxUser(destination);

                myAccount.cfg.setIdUri(displayName);
            }
        }

        Intent intent = new Intent(this, OutGoingCallActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("data", options);
        intent.putExtra(OutGoingCallActivity.CALLER_NO, destination);
        intent.putExtra(VoipConstants.PSTN, oldintent.hasExtra(VoipConstants.PSTN));
        intent.putExtra(OutGoingCallActivity.DISPLAY_NUMBER, displayName);

        startActivity(intent);
        destination = parseVoxUser(destination);
        if (preferenceEndPoint.getBooleanPreference(Constants.NOTIFICATION_ALERTS)) {
            outGoingCallNotificationId = Util.createNotification(this, destination, "Outgoing call", OutGoingCallActivity.class, intent);
            /*outGoingCallNotificationId = Notifications.CHAT_NOTIFICATION_ID;
            Util.setBigStyleNotification(this, destination, "Outgoing call", "Outgoing call", "", true, true, OutGoingCallActivity.class, intent);*/
        }
    }

    private boolean isValidPhoneNumberForPstnCalls(String destination, Intent oldintent) {
        boolean validNumber = false;

        if (oldintent.hasExtra(VoipConstants.PSTN)) {
            PhoneNumberUtil util = PhoneNumberUtil.getInstance();
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            try {
                Phonenumber.PhoneNumber numberProto = util.parse("+" + destination, "");
                int countryCode = numberProto.getCountryCode();
                validNumber = PhoneNumberUtil.getInstance().isValidNumber(util.parse("+" + destination, PhoneCountryISOCodes.getISOCodes(countryCode)));
            } catch (NumberParseException e) {
                e.printStackTrace();
            }
            android.util.Log.e(TAG, "Is Valid Phonenumber " + validNumber);


        }
        return validNumber;
    }

    private String parseVoxUser(String destination) {
        Contact contact = mContactsSyncManager.getContactByVoxUserName(destination);
        CallerInfo info = new CallerInfo();
        if (contact != null) {
            if (!TextUtils.isEmpty(contact.getName())) {
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

        if (currentCall != null) {
            localHold = !localHold;
            if (isHold) {
                mToastFactory.showToast(R.string.hold);
                getMediaManager().setMicrophoneMuteOn(true);
                if (mEndpoint != null && mEndpoint.audDevManager() != null) {
                    mEndpoint.audDevManager().setNoDev();
                }
                if (currentCall != null) {
                    CallOpParam prm = new CallOpParam(true);
                    try {
                        currentCall.setHold(prm);
                    } catch (Exception e) {
                        mLog.w(TAG, e);
                    }
                }
            } else {
                CallOpParam prm = new CallOpParam(true);
                prm.getOpt().setFlag(1);
                getMediaManager().setMicrophoneMuteOn(false);

                try {
                    currentCall.reinvite(prm);
                    mEndpoint.audDevManager().setPlaybackDev(0);
                    mEndpoint.audDevManager().setCaptureDev(0);
                    mToastFactory.showToast(R.string.unhold);
                } catch (Exception e) {
                    mLog.w(TAG, e);
                }
            }
        }
    }

    public void holdCall() {
        CallOpParam prm = new CallOpParam(true);

        try {
            currentCall.setHold(prm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unHoldCall() {
        CallOpParam prm = new CallOpParam(true);
        prm.getOpt().setFlag(1);
        try {
            currentCall.reinvite(prm);
        } catch (Exception e) {
            e.printStackTrace();
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
        public void onNetworkStateChanged(final int networkstate) {
            mLog.w(TAG, "Network change listener and state is " + networkstate);

            if (networkstate == NetworkStateListener.NETWORK_CONNECTED) {
                // Network is connected.
            } else if (networkstate == NetworkStateListener.NO_NETWORK_CONNECTIVITY) {
                SipCallModel callModel = new SipCallModel();
                callModel.setEvent(SipCallModel.RECONNECTING);
                callModel.setNetwork_availability(NetworkStateListener.NO_NETWORK_CONNECTIVITY);
                EventBus.getDefault().post(callModel);
            } else {
                // hangupCall(callType);
                OpponentDetails details = new OpponentDetails(null, null, 404);
                EventBus.getDefault().post(details);
            }
        }
    };

    public void acceptCall() {
        acceptIncomingCall();
    }

    private boolean isHangup;

    public void hangupCall(int callType) {
        mySelfEndCall = true;
        Util.cancelNotification(this, inComingCallNotificationId);
        Util.cancelNotification(this, outGoingCallNotificationId);
        if (currentCall != null) {
            if (isCallAccepted) {
                hangUp();
            } else if (callType == CallLog.Calls.INCOMING_TYPE) {
                sendBusyHereToIncomingCall();
            } else {
                hangUp();
            }
            isHangup = true;
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
        EventBus.getDefault().unregister(this);
        Util.cancelNotification(this, inComingCallNotificationId);
        Util.cancelNotification(this, outGoingCallNotificationId);
        android.util.Log.d("debug", "Service Killed");
        Toast.makeText(this, "OnReceive from killed service - YouWillNeverKillMe", Toast.LENGTH_SHORT).show();

        sendBroadcast(new Intent("YouWillNeverKillMe"));
        if (currentCall != null) {
            CallOpParam prm = new CallOpParam();
            prm.setStatusCode(pjsip_status_code.PJSIP_SC_DECLINE);
            try {
                currentCall.hangup(prm);
                isHangup = true;
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


    @Override
    public void disconnectCallBack(CallDisconnectedListner listner) {
        callDisconnectedListner = listner;
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


    public void acceptIncomingCall() {
        CallOpParam param = new CallOpParam();
        param.setStatusCode(pjsip_status_code.PJSIP_SC_OK);
        try {
            if (currentCall != null) {
                currentCall.answer(param);
            }
        } catch (Exception exc) {
            mLog.e(TAG, "Failed to accept incoming call", exc);
        }
    }

    public void sendBusyHereToIncomingCall() {
        CallOpParam param = new CallOpParam();
        param.setStatusCode(pjsip_status_code.PJSIP_SC_BUSY_HERE);

        try {
            if (currentCall != null) {
                currentCall.answer(param);
            }
        } catch (Exception exc) {
            mLog.e(TAG, "Failed to send busy here", exc);
        }
    }

    public void declineIncomingCall() {
        CallOpParam param = new CallOpParam();
        param.setStatusCode(pjsip_status_code.PJSIP_SC_DECLINE);

        try {
            if (currentCall != null) {
                currentCall.answer(param);
            }
        } catch (Exception exc) {
            mLog.e(TAG, "Failed to decline incoming call", exc);
        }
    }

    public void hangUp() {
        CallOpParam param = new CallOpParam();
        param.setStatusCode(pjsip_status_code.PJSIP_SC_DECLINE);
        webserviceUsecase.appStatus(null);
        try {
            if (currentCall != null) {
                currentCall.hangup(param);
            }
        } catch (Exception exc) {
            mLog.e(TAG, "Failed to hangUp call" + exc);
        }
    }

    //    @Subscribe
    public void onEvent(Object object) {
        if (object instanceof Integer) {
            // while outgoing call is going on if default incoming call comes should put on hold
            int hold = (int) object;
            if (hold == 100) {
                setHoldCall(true);
            } else if (hold == 101) {
                setHoldCall(false);

            }
        }
    }

}

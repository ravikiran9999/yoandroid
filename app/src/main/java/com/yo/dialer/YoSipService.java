package com.yo.dialer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
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
import com.yo.android.networkmanager.NetworkStateChangeListener;
import com.yo.android.networkmanager.NetworkStateListener;
import com.yo.android.pjsip.MediaManager;
import com.yo.android.pjsip.SipBinder;
import com.yo.android.pjsip.SipHelper;
import com.yo.android.ui.BottomTabsActivity;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;
import com.yo.android.vox.BalanceHelper;
import com.yo.dialer.googlesheet.UploadCallDetails;
import com.yo.dialer.googlesheet.UploadModel;
import com.yo.dialer.ui.IncomingCallActivity;
import com.yo.dialer.ui.OutgoingCallActivity;
import com.yo.dialer.yopj.YoAccount;
import com.yo.dialer.yopj.YoApp;
import com.yo.dialer.yopj.YoCall;
import com.yo.dialer.yopj.YoSipServiceHandler;
import com.yo.feedback.AppFailureReport;

import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.pjsip_status_code;

import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import de.greenrobot.event.EventBus;


/**
 * Created by Rajesh Babu on 11/7/17.
 */

public class YoSipService extends InjectedService implements IncomingCallListener {
    private static final String TAG = YoSipService.class.getSimpleName();
    private static final int NO_RTP_DISCONNECT_DURATION = 15000;
    private static final int INITIAL_CONNECTION_DURATION = 30000;
    private static final long NO_ANSWER_TRIGGER_DURATION = 45000;

    private YoSipServiceHandler sipServiceHandler;
    private YoAccount yoAccount;
    private boolean isReconnecting = false;
    private Runnable checkNetworkLossRunnable;
    private boolean isRemoteHold = false;
    private boolean isLocalHold = false;

    private boolean isCallAccepted;

    //To play Ringtone
    private MediaPlayer mRingTone;
    private AudioManager mAudioManager;
    private Vibrator mVibrator;
    private Uri mRingtoneUri;
    private static final long[] VIBRATOR_PATTERN = {0, 1000, 1000};

    public boolean isLocalHold() {
        return isLocalHold;
    }

    public void setLocalHold(boolean localHold) {
        isLocalHold = localHold;
    }

    public boolean isCallAccepted() {
        return isCallAccepted;
    }

    public void setCallAccepted(boolean callAccepted) {
        isCallAccepted = callAccepted;
    }


    public boolean isRemoteHold() {
        return isRemoteHold;
    }

    public void setRemoteHold(boolean remoteHold) {
        isRemoteHold = remoteHold;
    }

    public PreferenceEndPoint getPreferenceEndPoint() {
        return preferenceEndPoint;
    }

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;
    @Inject
    ToastFactory mToastFactory;

    @Inject
    ConnectivityHelper mHelper;

    @Inject
    ContactsSyncManager mContactsSyncManager;

    @Inject
    BalanceHelper mBalanceHelper;

    //Media Manager to handle audio related events.
    private MediaManager mediaManager;
    //Maintain current makeCall Object
    private static YoCall yoCurrentCall;
    private Handler mHandler = new Handler();
    private long callStarted;

    public int getCallType() {
        return callType;
    }

    public void setCallType(int callType) {
        this.callType = callType;
    }

    private int callType = -1;
    public String phoneNumber;
    private int callNotificationId;


    public Contact getCalleeContact() {
        return calleeContact;
    }

    public void setCalleeContact(Contact calleeContact) {
        this.calleeContact = calleeContact;
    }

    private Contact calleeContact;

    private LocalBroadcastManager mLocalBroadcastManager; // for while gettin normal phone call, current call state should change

    public static boolean changeHoldUI;

    public YoSipServiceHandler getSipServiceHandler() {
        return sipServiceHandler;
    }

    public static YoCall getYoCurrentCall() {
        return yoCurrentCall;
    }

    public static void setYoCurrentCall(YoCall yoCurrentCall) {
        YoSipService.yoCurrentCall = yoCurrentCall;
    }

    public static SimpleDateFormat df;
    public static SimpleDateFormat sdf;


    static {
        df = new SimpleDateFormat("dd-MM-yyyy");
        sdf = new SimpleDateFormat("hh:mm a");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new SipBinder(sipServiceHandler);
    }

    public YoAccount getYoAccount() {
        return yoAccount;
    }

    public void setYoAccount(YoAccount yoAccount) {
        this.yoAccount = yoAccount;
    }

    public void register(Intent intent) {
        mediaManager = new MediaManager(this);
        String stringPreference = preferenceEndPoint.getStringPreference(CallExtras.REGISTRATION_STATUS_MESSAGE);
        if (TextUtils.isEmpty(stringPreference) || yoAccount == null) {
            sipServiceHandler = YoSipServiceHandler.getInstance(this, preferenceEndPoint);
            sipServiceHandler.setYoAccount(null);
            yoAccount = sipServiceHandler.addAccount(this);
            DialerLogs.messageI(TAG, "Adding account. yoaccount object is " + yoAccount);
        } else {
            DialerLogs.messageI(TAG, "Acccount is already registered====Previsous state is=======" + stringPreference);
            try {
                if (yoAccount != null && !yoAccount.isRegistrationPending()) {
                    DialerLogs.messageI(TAG, "Acccount is already registered===========So doing renew");
                    yoAccount.setRegistration(true);
                } else {
                    DialerLogs.messageI(TAG, "YO========Previous registration request is in pending state==");
                }
            } catch (Exception e) {
               /* yoAccount.delete();
                yoAccount = null;
                register();*/

                String formattedDate = df.format(System.currentTimeMillis());
                Date d = new Date();
                String currentDateTimeString = sdf.format(d);
                String failedMessage = formattedDate + " - " + currentDateTimeString + " Acccount registration renewal Failed, deleted existing account registration and doing new registration." + e.getMessage();
                DialerLogs.messageI(TAG, failedMessage);
                AppFailureReport.sendDetails(failedMessage);
            }
        }
        if (intent != null) {
            makeCall(intent);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NetworkStateListener.registerNetworkState(listener);
        parseIntentInfo(intent);
        initializeMediaPalyer();
        CheckStatus.registration(this, preferenceEndPoint);
        registerBroadCast();
        return START_STICKY;
    }

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(CallExtras.Actions.COM_YO_ACTION_CALL_NORMAL_CALL)) {
                setHold(true);
            } else if (intent.getAction().equals(CallExtras.Actions.COM_YO_ACTION_CALL_NORMAL_CALL_DISCONNECTED)) {
                setHold(false);
            }
        }
    };

    private void registerBroadCast() {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(CallExtras.Actions.COM_YO_ACTION_CALL_NORMAL_CALL);
        mIntentFilter.addAction(CallExtras.Actions.COM_YO_ACTION_CALL_NORMAL_CALL_DISCONNECTED);
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    @Override
    public void onDestroy() {
        mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    private void initializeMediaPalyer() {
        mRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(YoSipService.this, RingtoneManager.TYPE_RINGTONE);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void parseIntentInfo(Intent intent) {
        if (intent != null) {
            DialerLogs.messageI(TAG, "Intent Action===========" + intent.getAction());
            if (CallExtras.REGISTER.equals(intent.getAction())) {
                DialerLogs.messageI(TAG, "sipServiceHandler===========");

                //   if (sipServiceHandler == null || (sipServiceHandler != null && sipServiceHandler.getRegistersCount() == 0)) {
                DialerLogs.messageI(TAG, "Registering Account===========");
                setYoAccount(null);
                register(null);
                //   }
            } else if (CallExtras.UN_REGISTER.equals(intent.getAction())) {
                if (sipServiceHandler != null && sipServiceHandler.getRegistersCount() > 0) {
                    sipServiceHandler.deleteAccount(yoAccount);
                    getPreferenceEndPoint().saveStringPreference(CallExtras.REGISTRATION_STATUS_MESSAGE, null);
                    setYoAccount(null);
                    stopSelf();
                }
            } else if (CallExtras.MAKE_CALL.equals(intent.getAction())) {
                makeCall(intent);
            } else if (CallExtras.ACCEPT_CALL.equals(intent.getAction())) {
                acceptCall();
            } else if (CallExtras.REJECT_CALL.equals(intent.getAction())) {
                rejectCall();
            }
        }
    }

    public void rejectCall() {
        stopDefaultRingtone();
        DialerLogs.messageE(TAG, "rejectCall==" + yoCurrentCall);
        if (yoCurrentCall != null) {
            SipHelper.isAlreadyStarted = false;
            CallHelper.rejectCall(yoCurrentCall);
        }
    }

    public void acceptCall() {
        stopDefaultRingtone();
        try {
            CallHelper.accetpCall(yoCurrentCall);
            checkCalleeLossNetwork();
        } catch (Exception e) {
            DialerLogs.messageE(TAG, "YO===Accepting call ==" + e.getMessage());
        }
    }

    private void makeCall(Intent intent) {
        if (!mHelper.isConnected()) {
            mToastFactory.showToast(getResources().getString(R.string.connectivity_network_settings));
            return;
        }
        String username = preferenceEndPoint.getStringPreference(Constants.VOX_USER_NAME, null);

        if (yoCurrentCall == null) {
            yoCurrentCall = CallHelper.makeCall(this, yoAccount, intent);
            DialerLogs.messageE(TAG, "YO==makeCalling call...and YOCALL = " + yoCurrentCall);
            if (yoCurrentCall != null) {
                showOutgointCallActivity(yoCurrentCall, intent);
                AppFailureReport.sendSuccessDetails("Showing outgoing call screen:" + username);
            } else {
                if (intent != null && intent.hasExtra(CallExtras.RE_REGISTERING)) {
                    //Re-tried but agian problem.
                    AppFailureReport.sendDetails("Making call failed, try to delete account but still issue, restart app required. :" + username);
                    SipHelper.isAlreadyStarted = false;
                    return;
                } else {
                    AppFailureReport.sendDetails("Problem with Current call object, so deleting account and re-register and calling agian.:" + username);
                    if (yoAccount != null) {
                        sipServiceHandler.deleteAccount(yoAccount);
                        yoAccount = null;
                        if (intent != null) {
                            intent.putExtra(CallExtras.RE_REGISTERING, true);
                        }
                        sipServiceHandler.setYoAccount(null);
                        register(intent);
                        makeCall(intent);
                    } else {
                        AppFailureReport.sendDetails("While Making call failed to create Account object and Call Object :" + username);
                        SipHelper.isAlreadyStarted = false;
                    }
                }
            }
        } else {
            AppFailureReport.sendDetails("Previous call is not properly ended:" + username);
            DialerLogs.messageE(TAG, "Previous call is not properly ended" + yoCurrentCall);
            yoCurrentCall.delete();
            setCurrentCallToNull();
            makeCall(intent);
        }
    }

    private void showOutgointCallActivity(YoCall yoCall, Intent intent) {
        showCallUI(yoCall, true, intent.getBooleanExtra(CallExtras.IS_PSTN, false));
    }

    @Override
    public void OnIncomingCall(YoCall yoCall) {
        if (yoCurrentCall != null) {
            handleBusy(yoCall);
        } else {
            yoCurrentCall = yoCall;
            YoApp yoApp = YoSipServiceHandler.getYoApp();
            if (yoApp != null) {
                yoApp.setEchoOptions();
            }
            startRingtone(); // to play caller ringtone
            DialerLogs.messageE(TAG, "On Incoming call current call call obj==" + yoCurrentCall);
            triggerNoAnswerIfNotRespond();
            startInComingCallScreen(yoCurrentCall);
        }

    }

    private void triggerNoAnswerIfNotRespond() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                sendNoAnswer();
            }
        };
        mHandler.postDelayed(runnable, NO_ANSWER_TRIGGER_DURATION);
    }

    private void sendNoAnswer() {
        DialerLogs.messageE(TAG, "YO====sendNoAnswer==" + isCallAccepted());

        if (!isCallAccepted() && yoCurrentCall != null) {
            CallOpParam prm = new CallOpParam(true);
            prm.setStatusCode(pjsip_status_code.PJSIP_SC_NOT_ACCEPTABLE_HERE);
            try {
                yoCurrentCall.answer(prm);
                //callDisconnected();
            } catch (Exception e) {
                DialerLogs.messageE(TAG, "sendNoAnswer== " + e.getMessage());
            }
        }
    }

    private void startInComingCallScreen(final YoCall yoCall) {
        showCallUI(yoCall, false, false);
    }

    private void showCallUI(YoCall yoCall, boolean isOutgongCall, boolean isPSTNCall) {
        Intent intent;
        DialerLogs.messageE(TAG, "YO====showCallUI== isOutgoingcall" + isOutgongCall);
        if (isOutgongCall) {
            callType = CallLog.Calls.OUTGOING_TYPE;
            int regStatus = preferenceEndPoint.getIntPreference(CallExtras.REGISTRATION_STATUS);
            if (regStatus == CallExtras.StatusCode.YO_CALL_NETWORK_NOT_REACHABLE) {
                showToast(getResources().getString(R.string.calls_no_network));
                return;
            } else if (regStatus == CallExtras.StatusCode.YO_REQUEST_TIME_OUT) {
                showToast(getResources().getString(R.string.request_timeout));
                return;
            } else {
                intent = new Intent(YoSipService.this, OutgoingCallActivity.class);
                //PSTN Call it will play ringtone from IVR
                if (!isPSTNCall) {
                    startDefaultRingtone(1);
                }
            }
        } else {
            callType = CallLog.Calls.INCOMING_TYPE;
            intent = new Intent(YoSipService.this, IncomingCallActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        String calleeNumber = DialerHelper.getInstance(YoSipService.this).getPhoneNumber(yoCall);
        if (calleeNumber == null) {
            String errorMsg = "Unfortunately callee number got null " + calleeNumber;
            String comment = "Sending that no network, this may be request registration request timeout.";
            callDisconnected(CallExtras.StatusCode.OTHER, errorMsg, comment);
            sipServiceHandler.callDisconnected(errorMsg + comment);
            sipServiceHandler.sendAction(new Intent(CallExtras.Actions.COM_YO_ACTION_CALL_NO_NETWORK));
            return;
        }
        phoneNumber = calleeNumber;
        if (isPSTNCall) {
            //TODO: NEED TO THINK FOR BETTER LOGIC
            calleeContact = DialerHelper.getInstance(YoSipService.this).readCalleeDetailsFromDB(mContactsSyncManager, BuildConfig.RELEASE_USER_TYPE + calleeNumber + "D");
        } else {
            calleeContact = DialerHelper.getInstance(YoSipService.this).readCalleeDetailsFromDB(mContactsSyncManager, calleeNumber);
        }
        intent.putExtra(CallExtras.CALLER_NO, calleeNumber);
        intent.putExtra(CallExtras.IMAGE, calleeContact.getImage());
        intent.putExtra(CallExtras.PHONE_NUMBER, calleeContact.getPhoneNo());
        intent.putExtra(CallExtras.NAME, calleeContact.getName());
        intent.putExtra(CallExtras.IS_PSTN, isPSTNCall);
        //Wait until user profile image is loaded , it should not show blank image
        startActivity(intent);
        sendNotification(intent, isOutgongCall);
        //Check if no response from the calle side need to disconnect the call.
        // checkCalleeLossNetwork();


    }

    private void showToast(final String string) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mToastFactory.showToast(string);
            }
        });
    }

    private void sendNotification(Intent intent, boolean isOutgongCall) {
        Class classs = null;
        String title = null;

        if (isOutgongCall) {
            classs = OutgoingCallActivity.class;
            title = "Outgoing call";
        } else {
            classs = IncomingCallActivity.class;
            title = "Incoming call";
        }

        if (preferenceEndPoint.getBooleanPreference(Constants.NOTIFICATION_ALERTS)) {
            callNotificationId = Util.createNotification(this, phoneNumber, title, classs, intent);
        }
    }

    public void setCurrentCallToNull() {
        yoCurrentCall = null;
    }


    //When callee loss his network there wont be any callback to caller
    //So after 10sec change to reconnecting and  30sec if there are no rtp packets need to disconnect the call.
    public void checkCalleeLossNetwork() {
        DialerLogs.messageE(TAG, "YO====Starting Checking Network FAILURE== calleed");

        if (checkNetworkLossRunnable == null) {
            networkPacketsCheck();
        }
        mHandler.postDelayed(checkNetworkLossRunnable, INITIAL_CONNECTION_DURATION);
    }

    private void networkPacketsCheck() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (yoCurrentCall != null) {
                    DialerLogs.messageE(TAG, "YO===Re-Inviting from Thread...Remote Hold= " + isRemoteHold + ", isLocalHold = " + isLocalHold());
                    if (!isRemoteHold() && !isLocalHold() && isCallAccepted()) {
                        reInviteToCheckCalleStatus();
                    }
                }
                mHandler.postDelayed(checkNetworkLossRunnable, NO_RTP_DISCONNECT_DURATION);
            }
        };
        checkNetworkLossRunnable = runnable;
    }

    private void reInviteToCheckCalleStatus() {

        DialerLogs.messageE(TAG, "YO===Re-Inviting for the call to check active state " + yoCurrentCall);
        try {
            if (yoCurrentCall != null) {
                DialerLogs.messageE(TAG, "YO===Re-Inviting for the call to check active state " + yoCurrentCall.isActive());
            }
            if (yoCurrentCall != null && !yoCurrentCall.isPendingReInvite()) {
                CallHelper.unHoldCall(yoCurrentCall);
                changeHoldUI = true;
            } else {
                DialerLogs.messageE(TAG, "YO===Pending Re-Inviting");
                yoCurrentCall.setPendingReInvite(true);
                YoSipService.changeHoldUI = false;
            }
        } catch (Exception e) {
            getSipServiceHandler().updateWithCallStatus(CallExtras.StatusCode.YO_INV_STATE_SC_RE_CONNECTING);
            DialerLogs.messageE(TAG, "YO===Re-Inviting failed" + e.getMessage());
            yoCurrentCall.setPendingReInvite(true);
            YoSipService.changeHoldUI = false;
        }
    }

    public int getCallDurationInSec() {
        if (yoCurrentCall != null) {
            try {
                int mSec = yoCurrentCall.getInfo().getConnectDuration().getSec();
                return mSec;
            } catch (Exception e) {
                DialerLogs.messageE(TAG, "YO==getCallDurationInSec===" + e.getMessage());
            }
        }
        return 0;
    }

    // this is for registration fail case, there we cant get call state from yocall object
    public void setCallStatus(int status) {
        getSipServiceHandler().updateWithCallStatus(status);
    }


    public void setHold(boolean isHold) {
        DialerLogs.messageE(TAG, "Call HOld" + isHold);
        if (isHold) {
            if(!yoCurrentCall.isPendingReInvite()) {
                CallHelper.holdCall(yoCurrentCall, preferenceEndPoint, phoneNumber);
                changeHoldUI = true;
            }
        } else {
            try {
                if(!yoCurrentCall.isPendingReInvite()) {
                    CallHelper.unHoldCall(yoCurrentCall);
                    CallHelper.uploadToGoogleSheet(preferenceEndPoint, phoneNumber, "Hold Off");
                    changeHoldUI = true;
                }
            } catch (Exception e) {
                DialerLogs.messageE(TAG, "YO===Re-Inviting failed" + e.getMessage());
                yoCurrentCall.setPendingReInvite(true);
                changeHoldUI = false;
                CallHelper.uploadToGoogleSheet(preferenceEndPoint, phoneNumber, "Hold Off failed because of " + e.getMessage());
            }
        }
    }

    public void setMic(boolean flag) {
        CallHelper.setMute(YoSipServiceHandler.getYoApp(), yoCurrentCall, flag);
    }

    public void callDisconnected(String code, String reason, String comment) {
        if(getYoCurrentCall() == null && reason.equalsIgnoreCase("Registration request timeout")) {
            uploadGoogleSheet(code, reason, comment, 0, null);
        } else {
            SipHelper.isAlreadyStarted = false;
            setCurrentCallToNull();
            //If the call is rejected should stop rigntone
            stopDefaultRingtone();
            DialerLogs.messageE(TAG, "callDisconnected" + reason);
            long callduration = storeCallLog(phoneNumber, callType, callStarted);
            uploadGoogleSheet(code, reason, comment, callduration, null);
            Util.cancelNotification(this, callNotificationId);
            if (sipServiceHandler != null) {
                sipServiceHandler.callDisconnected(reason);
            } else {
                DialerLogs.messageE(TAG, "SipServiceHandler is null");
            }
            callStarted = 0;
        }
    }

    public void uploadGoogleSheet(String code, String reason, String comment, long callduration, String missedCallNumber) {
        if (DialerConfig.UPLOAD_REPORTS_GOOGLE_SHEET) {
            try {
                PreferenceEndPoint preferenceEndPoint = getPreferenceEndPoint();
                UploadModel model = new UploadModel(preferenceEndPoint);
                model.setCallee(phoneNumber);
                String callee = model.getCallee();
                if (callee != null && callee.contains(BuildConfig.RELEASE_USER_TYPE)) {
                    model.setCallMode("App to App");
                } else {
                    model.setCallMode("App to PSTN");
                }
                model.setDuration(callduration + "");
                if (callType == 1) {
                    model.setCallType("Incoming");
                    model.setCaller(phoneNumber);
                    model.setCallee(preferenceEndPoint.getStringPreference(Constants.VOX_USER_NAME));
                } else if (callType == 2) {
                    model.setCallType("Outgoing");
                    model.setCaller(preferenceEndPoint.getStringPreference(Constants.VOX_USER_NAME));
                    model.setCallee(phoneNumber);
                } else {
                    model.setCallType("Missed Call");
                    model.setCaller(missedCallNumber);
                    model.setCallee(preferenceEndPoint.getStringPreference(Constants.VOX_USER_NAME));
                }
                model.setStatusCode(code);
                model.setStatusReason(reason);
                model.setComments(comment);

                Calendar c = Calendar.getInstance();
                String formattedDate = YoSipService.df.format(c.getTime());
                model.setDate(formattedDate);
                Date d = new Date();
                String currentDateTimeString = YoSipService.sdf.format(d);
                model.setTime(currentDateTimeString);
                String balance = mBalanceHelper.getCurrentBalance();
                model.setCurrentBalance(balance);
                UploadCallDetails.postDataFromApi(model, "Calls");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void callAccepted() {
        //Callee accepted call so stop ringtone.
        stopDefaultRingtone();
        CheckStatus.callStateBasedOnRTP(this);
        callStarted = System.currentTimeMillis();
        sipServiceHandler.sendAction(new Intent(CallExtras.Actions.COM_YO_ACTION_CALL_ACCEPTED));
    }

    NetworkStateChangeListener listener = new NetworkStateChangeListener() {
        public void onNetworkStateChanged(final int networkstate) {
            DialerLogs.messageI(TAG, "Network change listener and state is " + networkstate);
            if (networkstate == NetworkStateListener.NETWORK_CONNECTED) {
                // Network is connected.
                DialerLogs.messageI(TAG, "YO========Register sipServiceHandler===========" + sipServiceHandler);
                if (sipServiceHandler != null) {
                    sipServiceHandler.updateWithCallStatus(CallExtras.StatusCode.YO_INV_STATE_SC_CONNECTING);
                    DialerLogs.messageI(TAG, "YO========Register yoCurrentCall===========" + yoCurrentCall);
                    //To check registration
                    if (yoAccount != null) {
                        try {
                            DialerLogs.messageI(TAG, "YO========Renew registration===========");
                            if (!yoAccount.isRegistrationPending()) {
                                yoAccount.setRegistration(true);
                            } else {
                                DialerLogs.messageI(TAG, "YO========Previous registration request is in pending state==");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        register(null);
                    }

                    //To check ongoing call.
                    if (yoCurrentCall != null) {
                        DialerLogs.messageE(TAG, "YO===Re-Inviting from Thread..." + isRemoteHold() + " and isCallAccepted " + isCallAccepted);
                        if (!isRemoteHold() && isCallAccepted) {
                            reInviteToCheckCalleStatus();
                        }
                    }
                }
                // its alredy registered so no need to register again when network connects
                //register();
            } else {
                DialerLogs.messageI(TAG, "Network change listener and state is Network not reachable ");
                if (sipServiceHandler != null) {
                    sipServiceHandler.updateWithCallStatus(CallExtras.StatusCode.YO_CALL_NETWORK_NOT_REACHABLE);
                }
            }
        }
    };

    protected synchronized void startDefaultRingtone(int volume) {

        try {
            mAudioManager.setSpeakerphoneOn(false);
            mRingTone = MediaPlayer.create(this, R.raw.calling);
            mRingTone.setVolume(volume, volume);
            mRingTone.setLooping(true);
            mAudioManager.setMode(AudioManager.RINGER_MODE_SILENT);
            try {
                mRingTone.start();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
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

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        SipHelper.isAlreadyStarted = false;
        Util.cancelNotification(this, callNotificationId);
        DialerLogs.messageE(TAG, "KILLING YO APPLICATION.");
        sendBroadcast(new Intent("YouWillNeverKillMe"));
        if (yoCurrentCall != null) {
            CallOpParam prm = new CallOpParam();
            prm.setStatusCode(pjsip_status_code.PJSIP_SC_DECLINE);
            try {
                yoCurrentCall.hangup(prm);
                callDisconnected(CallExtras.StatusCode.OTHER, "Application killed", "May be while call is going on application got killed, so sending hangup to callee");
            } catch (Exception e) {
                DialerLogs.messageE(TAG, "Call is terminated because app got killed.");
            }
        }
    }

    protected synchronized void stopDefaultRingtone() {

        mVibrator.cancel();
        if (mRingTone != null) {
            try {
                if (mRingTone.isPlaying()) {
                    mRingTone.stop();
                }
            } catch (Exception ignored) {
            }
            try {
                mRingTone.reset();
                mRingTone.release();
            } catch (Exception ignored) {
            }
        }

    }

    private long storeCallLog(String mobileNumber, int callType, long callStarted) {
        long currentTime = System.currentTimeMillis();
        long callDuration = TimeUnit.MILLISECONDS.toSeconds(currentTime - callStarted);
        if (callStarted == 0 || callType == -1) {
            callDuration = 0;
        }
        //callStarted = 0;
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
                String countryCodeString = countryCode + "";
                String mobileTemp = mobileNumber;
                String phoneNumber = mobileTemp.substring(countryCodeString.length(), mobileTemp.length());
                contact = mContactsSyncManager.getContactPSTN(countryCode, phoneNumber);

            } catch (NumberParseException e) {
                DialerLogs.messageE(TAG, mobileNumber + " NumberParseException was thrown: " + e.toString());
            }
            if (contact != null && contact.getName() != null) {
                info.name = contact.getName();
            }
            pstnorapp = CallLog.Calls.APP_TO_PSTN_CALL;
        }

        CallLog.Calls.addCall(info, getBaseContext(), mobileNumber, callType, callStarted, callDuration, pstnorapp);
        return callDuration;
    }

    public void handleBusy(YoCall yoCall) {
        if (yoCurrentCall != null) {
            try {
                String source = DialerHelper.getInstance(YoSipService.this).getPhoneNumber(yoCall);
                sendBusyHereToIncomingCall(yoCall);
                Util.createNotification(this, source, getResources().getString(R.string.missed_call), BottomTabsActivity.class, new Intent(), false);
                storeCallLog(source, CallLog.Calls.MISSED_TYPE, 0);
            } catch (Exception e) {
                DialerLogs.messageE(TAG, e.getMessage());
            }
            // Dont remove below logic.
            yoCall.delete();
            return;
        }
    }

    public void sendBusyHereToIncomingCall(YoCall yoCall) {
        CallOpParam param = new CallOpParam(true);
        param.setStatusCode(pjsip_status_code.PJSIP_SC_BUSY_HERE);
        param.setReason(CallExtras.StatusReason.YO_BUSY_HERE);
        try {
            if (yoCall != null) {
                yoCall.hangup(param);
            }
        } catch (Exception exc) {
            DialerLogs.messageE(TAG, "Failed to send busy here");
        }
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

    public void sendMissedCallNotification() {
        Util.createNotification(this,
                parseVoxUser(phoneNumber),
                "Missed call ", BottomTabsActivity.class, new Intent(), false);
        callType = CallLog.Calls.MISSED_TYPE;
    }

    public void cancelCallNotification() {
        Util.cancelNotification(this, callNotificationId);
    }

    public void sendNoNetwork() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(YoSipService.this, getResources().getString(R.string.calls_no_network), Toast.LENGTH_LONG).show();
            }
        });
    }
}

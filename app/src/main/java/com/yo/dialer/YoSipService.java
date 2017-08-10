package com.yo.dialer;

import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.orion.android.common.preferences.PreferenceEndPoint;
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
import com.yo.android.ui.BottomTabsActivity;
import com.yo.android.util.Util;
import com.yo.dialer.ui.IncomingCallActivity;
import com.yo.dialer.ui.OutgoingCallActivity;
import com.yo.dialer.yopj.YoAccount;
import com.yo.dialer.yopj.YoCall;
import com.yo.dialer.yopj.YoSipServiceHandler;

import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.StreamStat;
import org.pjsip.pjsua2.pjsip_status_code;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;


/**
 * Created by Rajesh Babu on 11/7/17.
 */

public class YoSipService extends InjectedService implements IncomingCallListener {
    private static final String TAG = YoSipService.class.getSimpleName();
    private static final int NO_RTP_DISCONNECT_DURATION = 15000;
    private static final int INITIAL_CONNECTION_DURATION = 30000;

    private YoSipServiceHandler sipServiceHandler;
    private YoAccount yoAccount;
    private long currentRTPPackets;
    private boolean isReconnecting = false;
    private Runnable checkNetworkLossRunnable;

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;

    @Inject
    ContactsSyncManager mContactsSyncManager;
    //Media Manager to handle audio related events.
    private MediaManager mediaManager;

    //Maintain current makeCall Object
    private static YoCall yoCurrentCall;
    private Handler mHandler = new Handler();
    private long callStarted;
    private int callType = -1;
    private String phoneNumber;

    public YoSipServiceHandler getSipServiceHandler() {
        return sipServiceHandler;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new SipBinder(sipServiceHandler);
    }

    public void register() {
        mediaManager = new MediaManager(this);
        if (yoAccount == null) {
            sipServiceHandler = YoSipServiceHandler.getInstance(this, preferenceEndPoint);
            yoAccount = sipServiceHandler.addAccount(this);
        } else {
            DialerLogs.messageI(TAG, "Acccount is already registered===========");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NetworkStateListener.registerNetworkState(listener);
        parseIntentInfo(intent);
        return START_STICKY;
    }

    private void parseIntentInfo(Intent intent) {
        if (intent != null) {
            DialerLogs.messageI(TAG, "YO========Intent Action===========" + intent.getAction());
            if (CallExtras.REGISTER.equals(intent.getAction())) {
                if (sipServiceHandler == null || (sipServiceHandler != null && sipServiceHandler.getRegistersCount() == 0)) {
                    DialerLogs.messageI(TAG, "YO========Register Account===========");
                    register();
                }
            } else if (CallExtras.UN_REGISTER.equals(intent.getAction())) {
                if (sipServiceHandler != null && sipServiceHandler.getRegistersCount() > 0) {
                    sipServiceHandler.deleteAccount(yoAccount);
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
        CallHelper.rejectCall(yoCurrentCall);
    }

    public void acceptCall() {
        try {
            CallHelper.accetpCall(yoCurrentCall);
            StreamStat stats = yoCurrentCall.getStreamStat(0);
            currentRTPPackets = stats.getRtcp().getRxStat().getBytes();
            DialerLogs.messageE(TAG, "YO===Accepting call == currentRTP" + currentRTPPackets);

            checkCalleeLossNetwork();
        } catch (Exception e) {
            DialerLogs.messageE(TAG, "YO===Accepting call ==" + e.getMessage());
        }
    }

    private void makeCall(Intent intent) {
        if (yoCurrentCall == null) {
            yoCurrentCall = CallHelper.makeCall(yoAccount, intent);
            DialerLogs.messageE(TAG, "YO==makeCalling call...and YOCALL = " + yoCurrentCall);
            showOutgointCallActivity(yoCurrentCall, intent);
        } else {
            //TODO: ALREADY CALL IS GOING ON
        }
    }

    private void showOutgointCallActivity(YoCall yoCall, Intent intent) {
        showCallUI(yoCall, true, intent.getBooleanExtra(CallExtras.IS_PSTN, false));
    }

    @Override
    public void OnIncomingCall(YoCall yoCall) {
        //handleBusyCase(yoCall);
        yoCurrentCall = yoCall;
        DialerLogs.messageE(TAG, "YO====On Incoming call current call call obj==" + yoCurrentCall);
        try {
            DialerLogs.messageE(TAG, "YO====OnIncomingCall==" + yoCall.getInfo().getCallIdString());
        } catch (Exception e) {
            DialerLogs.messageE(TAG, "YO====OnIncomingCall==" + e.getMessage());
        }
        startInComingCallScreen(yoCurrentCall);
    }

    private void startInComingCallScreen(final YoCall yoCall) {
        showCallUI(yoCall, false, false);
    }

    private void showCallUI(YoCall yoCall, boolean isOutgongCall, boolean isPSTNCall) {
        Intent intent;
        DialerLogs.messageE(TAG, "YO====showCallUI== isOutgoingcall" + isOutgongCall);

        if (isOutgongCall) {
            callType = CallLog.Calls.OUTGOING_TYPE;
            intent = new Intent(YoSipService.this, OutgoingCallActivity.class);
        } else {
            callType = CallLog.Calls.INCOMING_TYPE;
            intent = new Intent(YoSipService.this, IncomingCallActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        String calleeNumber = DialerHelper.getInstance(YoSipService.this).getPhoneNumber(yoCall);
        phoneNumber = calleeNumber;
        Contact contact;
        if (isPSTNCall) {
            //TODO: NEED TO THINK FOR BETTER LOGIC
            contact = DialerHelper.getInstance(YoSipService.this).readCalleeDetailsFromDB(mContactsSyncManager, BuildConfig.RELEASE_USER_TYPE + calleeNumber + "D");
        } else {
            contact = DialerHelper.getInstance(YoSipService.this).readCalleeDetailsFromDB(mContactsSyncManager, calleeNumber);
        }
        intent.putExtra(CallExtras.CALLER_NO, calleeNumber);
        intent.putExtra(CallExtras.IMAGE, contact.getImage());
        intent.putExtra(CallExtras.PHONE_NUMBER, contact.getPhoneNo());
        intent.putExtra(CallExtras.NAME, contact.getName());
        //Wait until user profile image is loaded , it should not show blank image
        startActivity(intent);

        //Check if no response from the calle side need to disconnect the call.
        // checkCalleeLossNetwork();


    }

    public void setCurrentCallToNull() {
        yoCurrentCall = null;
    }


    //When callee loss his network there wont be any callback to caller
    //So after 10sec change to reconnecting and  30sec if there are no rtp packets need to disconnect the call.
    public void checkCalleeLossNetwork() {
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
                    DialerLogs.messageE(TAG, currentRTPPackets + "YO===Re-Inviting from Thread...");
                    reInviteToCheckCalleStatus();
                }
                mHandler.postDelayed(checkNetworkLossRunnable, NO_RTP_DISCONNECT_DURATION);
            }
        };
        checkNetworkLossRunnable = runnable;
    }

    private void reInviteToCheckCalleStatus() {
        DialerLogs.messageE(TAG, "YO===Re-Inviting for the call to check active state" + yoCurrentCall);
        try {
            CallHelper.unHoldCall(yoCurrentCall);
        } catch (Exception e) {
            getSipServiceHandler().updateWithCallStatus(CallExtras.StatusCode.YO_CALL_NETWORK_NOT_REACHABLE);
            DialerLogs.messageE(TAG, "YO===Re-Inviting failed" + e.getMessage());
        }
    }

    private void updateDisconnectStatus() {
        rejectCall();
        isReconnecting = false;
        setCurrentCallToNull();
        mHandler.removeCallbacks(checkNetworkLossRunnable);
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
            CallHelper.holdCall(yoCurrentCall);
        } else {
            try {
                CallHelper.unHoldCall(yoCurrentCall);
            } catch (Exception e) {
                DialerLogs.messageE(TAG, "YO===Re-Inviting failed" + e.getMessage());
                //Disconnect the call;
                updateDisconnectStatus();
            }
        }
    }

    public void setMic(boolean flag) {
        CallHelper.setMute(YoSipServiceHandler.getYoApp(), yoCurrentCall, flag);
    }

    public void callDisconnected() {
        setCurrentCallToNull();
        DialerLogs.messageE(TAG, "callDisconnected");
        storeCallLog(phoneNumber);
        if (sipServiceHandler != null) {
            sipServiceHandler.callDisconnected();
        } else {
            DialerLogs.messageE(TAG, "SipServiceHandler is null");
        }
    }

    public void callAccepted() {
        if (sipServiceHandler.callStatusListener != null) {
            callStarted = System.currentTimeMillis();
            sipServiceHandler.callStatusListener.callAccepted();
        }
    }

    NetworkStateChangeListener listener = new NetworkStateChangeListener() {
        public void onNetworkStateChanged(final int networkstate) {
            DialerLogs.messageI(TAG, "Network change listener and state is " + networkstate);
            if (networkstate == NetworkStateListener.NETWORK_CONNECTED) {
                // Network is connected.
                DialerLogs.messageI(TAG, "YO========Register Account===========");
                register();
            }
        }
    };

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
                DialerLogs.messageE(TAG, "NumberParseException was thrown: " + e.toString());
            }
            if (contact != null && contact.getName() != null) {
                info.name = contact.getName();
            }
            pstnorapp = CallLog.Calls.APP_TO_PSTN_CALL;
        }
        CallLog.Calls.addCall(info, getBaseContext(), mobileNumber, callType, callStarted, callDuration, pstnorapp);
    }

    private void handleBusyCase(YoCall yoCall) {
        if (yoCurrentCall != null) {
            try {
                String source = DialerHelper.getInstance(YoSipService.this).getPhoneNumber(yoCall);
                sendBusyHereToIncomingCall();
                //source = parseVoxUser(source);
                Util.createNotification(this, source, getResources().getString(R.string.missed_call), BottomTabsActivity.class, new Intent(), false);
                //Util.setBigStyleNotification(this, source, "Missed call", "Missed call", "", false, true, BottomTabsActivity.class, new Intent());
                callType = CallLog.Calls.MISSED_TYPE;
                storeCallLog(source);
            } catch (Exception e) {
                //DialerLogs.messageE(TAG, e);
            }

            // Dont remove below logic.
            //isCallDeleted = true;
            yoCall.delete();

            return;
        }
    }

    public void sendBusyHereToIncomingCall() {
        CallOpParam param = new CallOpParam();
        param.setStatusCode(pjsip_status_code.PJSIP_SC_BUSY_HERE);

        try {
            if (yoCurrentCall != null) {
                yoCurrentCall.answer(param);
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
}

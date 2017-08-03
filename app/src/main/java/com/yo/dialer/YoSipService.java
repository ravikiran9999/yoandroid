package com.yo.dialer;

import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.di.InjectedService;
import com.yo.android.model.Contact;
import com.yo.android.pjsip.MediaManager;
import com.yo.android.pjsip.SipBinder;
import com.yo.dialer.ui.IncomingCallActivity;
import com.yo.dialer.yopj.YoAccount;
import com.yo.dialer.yopj.YoCall;
import com.yo.dialer.yopj.YoSipServiceHandler;

import org.pjsip.pjsua2.StreamStat;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Rajesh Babu on 11/7/17.
 */

public class YoSipService extends InjectedService implements IncomingCallListener {
    private static final String TAG = YoSipService.class.getSimpleName();
    private static final int NO_RTP_DISCONNECT_DURATION = 15000;
    private YoSipServiceHandler sipServiceHandler;
    private YoAccount yoAccount;
    private String registrationStatus;
    private long currentRTPPackets;
    private boolean isReconnecting = false;

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


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new SipBinder(null);
    }

    public void register() {
        mediaManager = new MediaManager(this);
        sipServiceHandler = YoSipServiceHandler.getInstance(this, preferenceEndPoint);
        yoAccount = sipServiceHandler.addAccount(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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
        CallHelper.makeCall(yoAccount, intent);
    }

    @Override
    public void OnIncomingCall(YoCall yoCall) {
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
        final Intent intent = new Intent(YoSipService.this, IncomingCallActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        try {
            String calleeNumber = DialerHelper.getInstance(YoSipService.this).getPhoneNumber(yoCall);
            Contact contact = DialerHelper.getInstance(YoSipService.this).readCalleeDetailsFromDB(mContactsSyncManager, calleeNumber);
            intent.putExtra(CallExtras.CALLER_NO, calleeNumber);
            intent.putExtra(CallExtras.IMAGE, contact.getImage());
            intent.putExtra(CallExtras.PHONE_NUMBER, contact.getPhoneNo());
            intent.putExtra(CallExtras.NAME, contact.getName());
            //Wait until user profile image is loaded , it should not show blank image
            startActivity(intent);
        } catch (Exception e) {
            DialerLogs.messageE(TAG, "YO====startInComingCallScreen==" + e.getMessage());
        }
    }

    public void setCurrentCallToNull() {
        yoCurrentCall = null;
    }


    //When callee loss his network there wont be any callback to caller
    //So after 10sec change to reconnecting and  30sec if there are no rtp packets need to disconnect the call.
    public void checkCalleeLossNetwork() {
        if (yoCurrentCall != null) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    final StreamStat stats;
                    try {
                        stats = yoCurrentCall.getStreamStat(0);
                        DialerLogs.messageE(TAG, currentRTPPackets + "YO===checkCalleeLossNetwork stats.getRtcp().getRxStat().getBytes()...." + stats.getRtcp().getRxStat().getBytes());

                        if (currentRTPPackets == stats.getRtcp().getRxStat().getBytes()) {
                            //Change to reconnecting .
                            DialerLogs.messageE(TAG, "YO===checkCalleeLossNetwork....isreconnecting." + isReconnecting + "," + currentRTPPackets);

                            if (!isReconnecting) {
                                isReconnecting = true;
                                currentRTPPackets = stats.getRtcp().getRxStat().getBytes();
                                setCallStatus(getResources().getString(R.string.reconnecting_status));
                                DialerLogs.messageE(TAG, "YO===checkCalleeLossNetwork....Checking for next 15sec." + isReconnecting + "," + currentRTPPackets);
                                mHandler.postDelayed(this, NO_RTP_DISCONNECT_DURATION);
                            } else {
                                //Hangup call
                                DialerLogs.messageE(TAG, "YO===checkCalleeLossNetwork....end the call " + isReconnecting + "," + currentRTPPackets);
                                updateDisconnectStatus();
                            }
                        } else {
                            currentRTPPackets = stats.getRtcp().getRxStat().getBytes();
                            mHandler.postDelayed(this, NO_RTP_DISCONNECT_DURATION);
                        }
                    } catch (Exception e) {
                        DialerLogs.messageE(TAG, "YO===While reading RTP packets got an exception" + e.getMessage());
                        updateDisconnectStatus();
                    }
                }
            }, NO_RTP_DISCONNECT_DURATION);
        }
    }

    private void updateDisconnectStatus() {
        rejectCall();
        isReconnecting = false;
        yoCurrentCall = null;
       // setCallStatus(getResources().getString(R.string.disconnect_status));
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
    public void setCallStatus(String status) {
        registrationStatus = null;
        registrationStatus = status;
    }

    public String getCallStatus() {
        if (yoCurrentCall != null) {
            try {
                if (isReconnecting) {
                    return registrationStatus;
                } else {
                    String stateTxt = yoCurrentCall.getInfo().getStateText();
                    return stateTxt;
                }
            } catch (Exception e) {
                DialerLogs.messageE(TAG, "YO==getCallStatus===" + e.getMessage());
            }
        } else {
            return registrationStatus;
        }
        return null;
    }

    public void setHold(boolean flag) {
        if (flag) {
            CallHelper.holdCall(yoCurrentCall);
        } else {
            CallHelper.unHoldCall(yoCurrentCall);
        }
    }

    public void setMic(boolean flag) {
        CallHelper.setMute(YoSipServiceHandler.getYoApp(), yoCurrentCall, flag);
    }
}

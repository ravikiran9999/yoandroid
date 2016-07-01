package com.yo.android.voip;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.orion.android.common.logger.Log;

import de.greenrobot.event.EventBus;


public class UserAgent {

    private static final String TAG = "UserAgent";
    public static final String ACTION_CALL_END = "com.yo.android.voip.UserAgent.CALL_END";
    public static final int CALL_STATE_IDLE = 0;
    public static final int CALL_STATE_INCOMING_CALL = 1;
    public static final int CALL_STATE_OUTGOING_CALL = 2;
    public static final int CALL_STATE_INCALL = 3;
    public static final int CALL_STATE_HOLD = 4;
    public static final int CALL_STATE_BUSY = 5;
    public static final int CALL_STATE_END = 6;
    public static final int CALL_STATE_ERROR = 7;
    public static final int TIME_OUT = 30;

    private EventBus bus = EventBus.getDefault();
    private SipCallModel callModel = null;
    private int callState = CALL_STATE_IDLE;
    private SipManager manager;
    private static SipAudioCall call;
    private SipProfile profile;
    private Context context;
    private Intent intent;
    private final Log mLog;
    protected String callerName;


    public UserAgent(Log log, SipManager manager, SipAudioCall call, Context context, Intent intent, SipProfile profile) {
        this.manager = manager;
        this.call = call;
        this.context = context;
        this.intent = intent;
        this.profile = profile;
        this.callModel = new SipCallModel();
        this.mLog = log;
        if (!bus.isRegistered(this)) {
            bus.register(this);
        }
    }

    public interface UserAgentListener {
        boolean hangup();
    }

    public void setCallState(int callState) {
        this.callState = callState;
    }

    public int getCallState() {
        return callState;
    }


    protected synchronized void changeStatus(int state, String caller) {
        callState = state;
        mLog.i(TAG, "caller: %s", caller);
    }

    protected void changeStatus(int state) {
        changeStatus(state, null);
        Receiver.setCallState(state);
    }

    /**
     * Checks the call state
     */
    protected boolean statusIs(int state) {
        return callState == state;
    }


    public boolean call(String targetUrl) {
        changeStatus(CALL_STATE_OUTGOING_CALL, targetUrl);
        return false;
    }

    /**
     * Callback function called when arriving a new INVITE method (incoming
     * call)
     *
     * @param callData
     */

    public void doOutgoingCall(Bundle callData) {
        changeStatus(CALL_STATE_OUTGOING_CALL);
        try {
            String callAddress = callData.getString("callerNo");
            callAddress = String.format("%s@209.239.120.239", callAddress);
            mLog.d("SIP/CALLING_NUMBER", callAddress);
            call = manager.makeAudioCall(profile.getUriString(), callAddress, outgoingCallListener, TIME_OUT);
        } catch (SipException e) {
            mLog.w(TAG, e);
        }
    }

    public void onCallIncoming() {
        try {
            if (statusIs(CALL_STATE_IDLE)) {
                call = manager.takeAudioCall(intent, listener2);
            }
            changeStatus(CALL_STATE_INCOMING_CALL);
        } catch (SipException e) {
            mLog.w(TAG, e);
        }
        Intent i = new Intent(context, InComingCallActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            String useName = call.getPeerProfile().getDisplayName();
            if (useName == null) {
                useName = call.getPeerProfile().getUserName();
            }
            String caller = useName;
            i.putExtra("caller", caller);
            context.startActivity(i);
        } catch (Exception e) {
            mLog.w(TAG, "USERAGENT-onCallIncoming()", e);
        }
    }

    //    @Subscribe
    public void onEvent(SipCallModel model) {
        mLog.d("BUSSS CALLED", "<><> USER-AGENT BUS CALLED <><>");
        if (model.isOnCall() && callState == CALL_STATE_INCOMING_CALL) {
            accept();
            mLog.d("MODEL VALUE", "true");
        } else if (!model.isOnCall() && (callState == CALL_STATE_INCALL || callState == CALL_STATE_INCOMING_CALL)) {
            hangup();
            mLog.d("MODEL VALUE", "false");
        } else if (!model.isOnCall() && callState == CALL_STATE_OUTGOING_CALL) {
            hangup();
            mLog.d("OUTGOING_CALL - MODEL VALUE", "false");
        }
        processEvents(model);
        if (model.getEvent() != OutGoingCallActivity.CALL_ACCEPTED_START_TIMER) {
            model.setEvent(OutGoingCallActivity.NOEVENT);
        }
    }

    private void processEvents(SipCallModel model) {
        switch (model.getEvent()) {
            case OutGoingCallActivity.MUTE_ON:
                mute(true);
                break;
            case OutGoingCallActivity.MUTE_OFF:
                mute(false);
                break;
            case OutGoingCallActivity.SPEAKER_ON:
                speaker(true);
                break;
            case OutGoingCallActivity.SPEAKER_OFF:
                speaker(false);
                break;
            default:
                break;
        }
    }

    /**
     * Closes an ongoing, incoming, or pending call
     *
     * @return
     */

    public synchronized void hangup() {
        mLog.d("CALL", "|||| HANG UP ||||");
        changeStatus(CALL_STATE_IDLE);
        try {
            call.endCall();
        } catch (SipException e) {
            mLog.w(TAG, e);
        } catch (Exception e) {
            mLog.w(TAG, e);
        }
    }

    /**
     * Accepts an incoming call
     */
    public synchronized boolean accept() {
        mLog.d("CALL", "|||| ACCEPT ||||");

        changeStatus(CALL_STATE_INCALL);
        try {
            call.answerCall(TIME_OUT);
            call.startAudio();
        } catch (SipException e) {
            mLog.w(TAG, e);
        }
        return true;
    }

    // To mute mic while in call
    public synchronized void mute(boolean mute) {
        mLog.d("mute", "<<< MUTE METHOD CALLED >>>>");
        // If call is not muted and wants to mute
        if (!call.isMuted() && mute) {
            call.toggleMute();
            mLog.d("CALL MUTE", "=== CALL MUTED ==== ");
        } else if (call.isMuted() && !mute) {
            // If call is muted and wants to unmute
            call.toggleMute();
            mLog.d("CALL MUTE", "=== CALL UN-MUTED ==== ");
        }
    }

    // To set speaker on/off while in call
    public synchronized void speaker(boolean val) {
        mLog.d("speaker", "<<< SPEAKER METHOD CALLED >>>>");
        if (call.isInCall()) {
            if (val) {
                AudioManager audioManager = (AudioManager) context.getApplicationContext()
                        .getSystemService(Context.AUDIO_SERVICE);
                audioManager.setMode(AudioManager.MODE_IN_CALL);
                audioManager.setSpeakerphoneOn(true);
                mLog.d("CALL SPEAKER", "=== SPEAKER ON ==== ");
            } else {
                AudioManager audioManager = (AudioManager) context.getApplicationContext()
                        .getSystemService(Context.AUDIO_SERVICE);
                audioManager.setMode(AudioManager.MODE_IN_CALL);
                audioManager.setSpeakerphoneOn(false);
                mLog.d("CALL SPEAKER", "=== SPEAKER OFF ==== ");
            }
        }
    }

    SipAudioCall.Listener listener2 = new SipAudioCall.Listener() {

        @Override
        public void onCalling(SipAudioCall call) {
            mLog.d(TAG, "UserAgent.INCOMING_CALL %s", "ON CALLING");
            super.onCalling(call);
        }

        @Override
        public void onCallEstablished(SipAudioCall call) {
            mLog.d(TAG, "UserAgent.INCOMING_CALL - CALL ESTABLISHED");
            callerName = call.getPeerProfile().getDisplayName();
            if (callerName == null) {
                callerName = call.getPeerProfile().getUserName();
            }
            super.onCallEstablished(call);
        }

        @Override
        public void onRinging(SipAudioCall call, SipProfile caller) {
            try {
                mLog.d(TAG, "UserAgent.INCOMING_CALL - CALL RINGING");
            } catch (Exception e) {
                mLog.w(TAG, e);
            }
        }

        @Override
        public void onCallEnded(SipAudioCall call) {
            super.onCallEnded(call);
            mLog.d(TAG, "UserAgent.INCOMING_CALL - CALL ENDED");
            endCall(CALL_STATE_END);
        }

        @Override
        public void onCallBusy(SipAudioCall call) {
            super.onCallBusy(call);
            endCall(CALL_STATE_BUSY);
            mLog.d(TAG, "UserAgent.INCOMING_CALL - CALL BUSY");

        }

        /* (non-Javadoc)
         * @see android.net.sip.SipAudioCall.Listener#onReadyToCall(android.net.sip.SipAudioCall)
         */
        @Override
        public void onReadyToCall(SipAudioCall call) {
            mLog.d(TAG, "UserAgent.INCOMING_CALL - ON READY TO CALL");
            super.onReadyToCall(call);
        }

        /* (non-Javadoc)
         * @see android.net.sip.SipAudioCall.Listener#onRingingBack(android.net.sip.SipAudioCall)
         */
        @Override
        public void onRingingBack(SipAudioCall call) {
            mLog.d(TAG, "UserAgent.INCOMING_CALL - ON RINGING BACK");
            super.onRingingBack(call);
        }

        /* (non-Javadoc)
         * @see android.net.sip.SipAudioCall.Listener#onError(android.net.sip.SipAudioCall, int, java.lang.String)
         */
        @Override
        public void onError(SipAudioCall call, int errorCode, String errorMessage) {
            mLog.d(TAG, "UserAgent.INCOMING_CALL - ON ERROR");
            super.onError(call, errorCode, errorMessage);
            endCall(CALL_STATE_ERROR);
        }


    };

    private void endCall(int reason) {
        try {
            changeStatus(reason);
            callModel.setEvent(reason);
            callModel.setOnCall(false);
            UserAgent.call.endCall();
            UserAgent.call.close();
            changeStatus(CALL_STATE_IDLE);
            bus.post(callModel);
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_CALL_END));
        } catch (Exception e) {
            mLog.w(TAG, e);
        }
    }

    SipAudioCall.Listener outgoingCallListener = new SipAudioCall.Listener() {


        /* (non-Javadoc)
         * @see android.net.sip.SipAudioCall.Listener#onCallBusy(android.net.sip.SipAudioCall)
         */
        @Override
        public void onCallBusy(SipAudioCall call) {
            mLog.d(TAG, "UserAgent.outgoingCallListener - OnCallBusy");
            callModel.setOnCall(false);
            callModel.setEvent(CALL_STATE_BUSY);
            bus.post(callModel);
            endCall(CALL_STATE_BUSY);
            super.onCallBusy(call);
        }

        /* (non-Javadoc)
         * @see android.net.sip.SipAudioCall.Listener#onError(android.net.sip.SipAudioCall, int, java.lang.String)
         */
        @Override
        public void onError(SipAudioCall call, int errorCode, String errorMessage) {
            mLog.d(TAG, "UserAgent.outgoingCallListener: Error Code= %d", errorCode);
            mLog.d(TAG, "UserAgent.outgoingCallListener Error Message= %s", errorMessage);
            callModel.setEvent(CALL_STATE_ERROR);
            callModel.setOnCall(false);
            bus.post(callModel);
            endCall(CALL_STATE_ERROR);
            super.onError(call, errorCode, errorMessage);
        }

        @Override
        public void onCallEstablished(SipAudioCall call) {
            mLog.d(TAG, "UserAgent.outgoingCallListener", "OnCallEstablished");
            call.startAudio();
            speaker(false);
            changeStatus(CALL_STATE_OUTGOING_CALL);
            callModel.setOnCall(true);
            callModel.setEvent(OutGoingCallActivity.CALL_ACCEPTED_START_TIMER);
            bus.post(callModel);
        }

        /* (non-Javadoc)
         * @see android.net.sip.SipAudioCall.Listener#onCallEnded(android.net.sip.SipAudioCall)
         */
        @Override
        public void onCallEnded(SipAudioCall call) {
            changeStatus(CALL_STATE_IDLE);
            try {
                UserAgent.call.endCall();
                UserAgent.call.close();
                if (UserAgent.call.isInCall()) {
                    UserAgent.call.endCall();
                }
            } catch (SipException e) {
                mLog.e(TAG, "UserAgent/outgoingCallListener", e);
            }
            callModel.setOnCall(false);
            endCall(CALL_STATE_ERROR);
            bus.post(callModel);

        }


        @Override
        public void onRingingBack(SipAudioCall call) {
            super.onRingingBack(call);
            mLog.d(TAG, "OUTGOINGCALL/ONRINGINGBACK - %s", "onRingingBack");
        }

        @Override
        public void onChanged(SipAudioCall call) {

            int state = call.getState();
            mLog.d(TAG, "UserAgent.outgoingCallListener: onChanged state is - %d", state);
            super.onChanged(call);
        }
    };
}
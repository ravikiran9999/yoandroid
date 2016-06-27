package com.yo.android.voip;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipSession;
import android.os.Bundle;

import com.orion.android.common.logger.Log;

import org.greenrobot.eventbus.EventBus;


public class UserAgent {

    public static final int CALL_STATE_IDLE = 0;
    public static final int CALL_STATE_INCOMING_CALL = 1;
    public static final int CALL_STATE_OUTGOING_CALL = 2;
    public static final int CALL_STATE_INCALL = 3;
    public static final int CALL_STATE_HOLD = 4;

    private EventBus bus = EventBus.getDefault();
    SipCallModel callModel = null;

    int call_state = CALL_STATE_IDLE;

    SipManager manager;
    static SipAudioCall call;
    SipProfile profile;
    Context context;
    SipAudioCall.Listener listener;
    Intent intent;
    UserAgentListener callback;
    final Log mLog;

    public interface UserAgentListener {
        public boolean hangup();
    }

    protected String callerName;

    protected synchronized void changeStatus(int state, String caller) {
        call_state = state;
    }

    protected void changeStatus(int state) {
        changeStatus(state, null);
        Receiver.call_state = state;
    }

    /**
     * Checks the call state
     */
    protected boolean statusIs(int state) {
        return (call_state == state);
    }

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

    public boolean call(String target_url) {
        changeStatus(CALL_STATE_OUTGOING_CALL, target_url);
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
            callAddress = callAddress + "@209.239.120.239";
            //callAddress = "+918149169927" + "@209.239.120.239";
            mLog.d("SIP/CALLING_NUMBER", callAddress);
            call = manager.makeAudioCall(profile.getUriString(), callAddress, OutgoingCallListener, 30);
        } catch (SipException e) {
            e.printStackTrace();
        }
    }

    public void onCallIncoming() {
        try {
            if (statusIs(CALL_STATE_IDLE))
                call = manager.takeAudioCall(intent, listener2);
            changeStatus(CALL_STATE_INCOMING_CALL);
        } catch (SipException e) {
            e.printStackTrace();
        }
        Intent i = new Intent(context, IncomingCallActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            String useName = call.getPeerProfile().getDisplayName();
            if (useName == null) {
                useName = call.getPeerProfile().getUserName();
            }
            //String caller = useName + "@" + call.getPeerProfile().getSipDomain();
            String caller = useName;
            i.putExtra("caller", caller);
            context.startActivity(i);
        } catch (Exception e) {
            mLog.e("USERAGENT-onCallIncoming()", e.toString(), null);
        }
    }

    public void onEvent(SipCallModel model) {
        mLog.d("BUSSS CALLED", "<><> USER-AGENT BUS CALLED <><>");
        if (model.isOnCall() && call_state == CALL_STATE_INCOMING_CALL) {
            accept();
            mLog.d("MODEL VALUE", "true");
        } else if (!model.isOnCall() && (call_state == CALL_STATE_INCALL || call_state == CALL_STATE_INCOMING_CALL)) {
            hangup();
            mLog.d("MODEL VALUE", "false");
        } else if (!model.isOnCall() && call_state == CALL_STATE_OUTGOING_CALL) {
            hangup();
            mLog.d("OUTGOING_CALL - MODEL VALUE", "false");
        }
        switch (model.getEvent()) {
            case IncomingCallActivity.MUTE_ON:
                mute(true);
                break;
            case IncomingCallActivity.MUTE_OFF:
                mute(false);
                break;
            case IncomingCallActivity.SPEAKER_ON:
                speaker(true);
                break;
            case IncomingCallActivity.SPEAKER_OFF:
                speaker(false);
                break;
        }
        if (model.getEvent() != IncomingCallActivity.CALL_ACCEPTED_START_TIMER)
            model.setEvent(IncomingCallActivity.NOEVENT);
    }

    /**
     * Closes an ongoing, incoming, or pending call
     *
     * @return
     */

    public synchronized void hangup() {
        mLog.d("CALL", "|||| HANG UP ||||");
        // printLog("HANGUP");

        // closeMediaApplication();

		/*
         * if (call != null) { call.hangup(); }
		 */
        changeStatus(CALL_STATE_IDLE);
        try {
            call.endCall();
        } catch (SipException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Accepts an incoming call
     */
    public synchronized boolean accept() {
        mLog.d("CALL", "|||| ACCEPT ||||");

        changeStatus(CALL_STATE_INCALL);
        try {
            call.answerCall(30);
            call.startAudio();
        } catch (SipException e) {
            e.printStackTrace();
        }
        return true;
    }

    // To mute mic while in call
    public synchronized void mute(boolean mute) {
        mLog.d("mute", "<<< MUTE METHOD CALLED >>>>");
        // If call is not muted and wants to mute
        if (!call.isMuted() && mute == true) {
            call.toggleMute();
            mLog.d("CALL MUTE", "=== CALL MUTED ==== ");

        }
        // If call is muted and wants to unmute
        else if (call.isMuted() && mute == false) {
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
            mLog.d("UserAgent.INCOMING_CALL", "ON CALLING");
            super.onCalling(call);
        }

        @Override
        public void onCallEstablished(SipAudioCall call) {
            mLog.d("UserAgent.INCOMING_CALL", "CALL ESTABLISHED");
            callerName = call.getPeerProfile().getDisplayName();
            if (callerName == null) {
                callerName = call.getPeerProfile().getUserName();
            }
            //callerName += "@" + call.getPeerProfile().getSipDomain();

            super.onCallEstablished(call);
        }

        @Override
        public void onRinging(SipAudioCall call, SipProfile caller) {
            try {
                // call.answerCall(30);
                mLog.d("UserAgent.INCOMING_CALL", "CALL RINGING");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCallEnded(SipAudioCall Call) {
            mLog.d("UserAgent.INCOMING_CALL", "CALL ENDED");
            try {
                changeStatus(CALL_STATE_IDLE);
                callModel.setOnCall(false);
                bus.post(callModel);

                call.endCall();
                call.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCallBusy(SipAudioCall call) {
            mLog.d("UserAgent.INCOMING_CALL", "CALL BUSY");
            super.onCallBusy(call);
        }

        /* (non-Javadoc)
         * @see android.net.sip.SipAudioCall.Listener#onReadyToCall(android.net.sip.SipAudioCall)
         */
        @Override
        public void onReadyToCall(SipAudioCall call) {
            mLog.d("UserAgent.INCOMING_CALL", "ON READY TO CALL");
            super.onReadyToCall(call);
        }

        /* (non-Javadoc)
         * @see android.net.sip.SipAudioCall.Listener#onRingingBack(android.net.sip.SipAudioCall)
         */
        @Override
        public void onRingingBack(SipAudioCall call) {
            mLog.d("UserAgent.INCOMING_CALL", "ON RINGING BACK");
            super.onRingingBack(call);
        }

        /* (non-Javadoc)
         * @see android.net.sip.SipAudioCall.Listener#onError(android.net.sip.SipAudioCall, int, java.lang.String)
         */
        @Override
        public void onError(SipAudioCall call, int errorCode, String errorMessage) {
            mLog.d("UserAgent.INCOMING_CALL", "ON ERROR");
            super.onError(call, errorCode, errorMessage);
        }


    };

    SipAudioCall.Listener OutgoingCallListener = new SipAudioCall.Listener() {

        /* (non-Javadoc)
         * @see android.net.sip.SipAudioCall.Listener#onCalling(android.net.sip.SipAudioCall)
         */
        @Override
        public void onCalling(SipAudioCall call) {
            super.onCalling(call);
        }

				/* (non-Javadoc)
                 * @see android.net.sip.SipAudioCall.Listener#onCallEstablished(android.net.sip.SipAudioCall)
				 */

        /* (non-Javadoc)
         * @see android.net.sip.SipAudioCall.Listener#onCallBusy(android.net.sip.SipAudioCall)
         */
        @Override
        public void onCallBusy(SipAudioCall call) {
            mLog.d("UserAgent.OutgoingCallListener", "OnCallBusy");
            callModel.setOnCall(false);
            bus.post(callModel);
            super.onCallBusy(call);
        }

        /* (non-Javadoc)
         * @see android.net.sip.SipAudioCall.Listener#onError(android.net.sip.SipAudioCall, int, java.lang.String)
         */
        @Override
        public void onError(SipAudioCall call, int errorCode, String errorMessage) {
            mLog.d("UserAgent.OutgoingCallListener", "Error Code= " + errorCode);
            mLog.d("UserAgent.OutgoingCallListener", "Error Message= " + errorMessage);
            callModel.setOnCall(false);
            super.onError(call, errorCode, errorMessage);
        }

        @Override
        public void onCallEstablished(SipAudioCall call) {

            mLog.d("UserAgent.OutgoingCallListener", "OnCallEstablished");
            call.startAudio();
            speaker(false);
            changeStatus(CALL_STATE_OUTGOING_CALL);
            callModel.setOnCall(true);
            callModel.setEvent(IncomingCallActivity.CALL_ACCEPTED_START_TIMER);
            bus.post(callModel);
        }

        /* (non-Javadoc)
         * @see android.net.sip.SipAudioCall.Listener#onCallEnded(android.net.sip.SipAudioCall)
         */
        @Override
        public void onCallEnded(SipAudioCall Call) {
            changeStatus(CALL_STATE_IDLE);

            try {
                call.endCall();
                call.close();
                if (call.isInCall()) {
                    call.endCall();

                }
            } catch (SipException e) {
                mLog.e("UserAgent/OutgoingCallListener", e.toString());
            }
            callModel.setOnCall(false);
            bus.post(callModel);
        }

				/* (non-Javadoc)
                 * @see android.net.sip.SipAudioCall.Listener#onRingingBack(android.net.sip.SipAudioCall)
				 */

        @Override
        public void onRingingBack(SipAudioCall call) {

            super.onRingingBack(call);
            mLog.d("OUTGOINGCALL/ONRINGINGBACK", "onRingingBack");
        }

				/* (non-Javadoc)
                 * @see android.net.sip.SipAudioCall.Listener#onChanged(android.net.sip.SipAudioCall)
				 */

        @Override
        public void onChanged(SipAudioCall call) {

            mLog.d("UserAgent.OutgoingCallListener", "onChanged");
            int state = call.getState();
            switch (state) {
                case SipSession.State.OUTGOING_CALL_CANCELING:
                    mLog.d("SESSION-STATE", "OUTGOING_CALL_CANCELING");
                    break;
                case SipSession.State.INCOMING_CALL_ANSWERING:
                    mLog.d("SESSION-STATE", "INCOMING_CALL_ANSWERING");
                    break;
                case SipSession.State.INCOMING_CALL:
                    mLog.d("SESSION-STATE", "INCOMING_CALL");
                    break;
                case SipSession.State.OUTGOING_CALL_RING_BACK:
                    mLog.d("SESSION-STATE", "OUTGOING_CALL_RING_BACK");
                    break;
                case SipSession.State.PINGING:
                    mLog.d("SESSION-STATE", "PINGING");
                    break;
                case SipSession.State.IN_CALL:
                    mLog.d("SESSION-STATE", "IN_CALL");
                    break;
                case SipSession.State.DEREGISTERING:
                    mLog.d("SESSION-STATE", "DEREGISTERING");
                    break;
                case SipSession.State.REGISTERING:
                    mLog.d("SESSION-STATE", "DEREGISTERING");
                    break;
                case SipSession.State.READY_TO_CALL:
                    mLog.d("SESSION-STATE", "DEREGISTERING");
                    break;
            }
            super.onChanged(call);
        }
    };
}
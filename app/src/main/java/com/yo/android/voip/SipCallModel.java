package com.yo.android.voip;

public class SipCallModel {

    private boolean onCall;
    private boolean mute;
    private boolean speaker;
    private int event;
    public static final int IDLE = 0;
    public static final int BUSY = 1;
    public static final int IN_CALL = 2;
    public static final int RECONNECTING = 3;
    private int network_availability;


    public SipCallModel() {

    }

    public boolean isOnCall() {
        return onCall;
    }

    public void setOnCall(boolean onCall) {
        this.onCall = onCall;
    }

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public boolean isSpeaker() {
        return speaker;
    }

    public void setSpeaker(boolean speaker) {
        this.speaker = speaker;
    }

    /**
     * @return the event
     */
    public int getEvent() {
        return event;
    }

    /**
     * @param event the event to set
     */
    public void setEvent(int event) {
        this.event = event;
    }

    public int getNetwork_availability() {
        return network_availability;
    }

    public void setNetwork_availability(int network_availability) {
        this.network_availability = network_availability;
    }
}

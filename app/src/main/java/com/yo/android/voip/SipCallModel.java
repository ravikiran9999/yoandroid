package com.yo.android.voip;

public class SipCallModel {

    private boolean onCall;
    private boolean mute = false;
    private boolean speaker = false;
    private int event = 0;

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


}

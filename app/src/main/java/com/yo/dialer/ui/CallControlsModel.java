package com.yo.dialer.ui;

/**
 * Created by root on 17/8/17.
 */

public class CallControlsModel {
    private boolean isSpeakerOn;
    private boolean isHoldOn;
    private boolean isMicOn;
    private boolean isChatOpened;
    private boolean isCallAccepted;

    public boolean isSpeakerOn() {
        return isSpeakerOn;
    }

    public void setSpeakerOn(boolean speakerOn) {
        isSpeakerOn = speakerOn;
    }

    public boolean isHoldOn() {
        return isHoldOn;
    }

    public void setHoldOn(boolean holdOn) {
        isHoldOn = holdOn;
    }

    public boolean isMicOn() {
        return isMicOn;
    }

    public void setMicOn(boolean micOn) {
        isMicOn = micOn;
    }

    public boolean isChatOpened() {
        return isChatOpened;
    }

    public void setChatOpened(boolean chatOpened) {
        isChatOpened = chatOpened;
    }

    public boolean isCallAccepted() {
        return isCallAccepted;
    }

    public void setCallAccepted(boolean callAccepted) {
        isCallAccepted = callAccepted;
    }
}

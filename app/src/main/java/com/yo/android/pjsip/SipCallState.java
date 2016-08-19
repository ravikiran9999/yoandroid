package com.yo.android.pjsip;

/**
 * Created by Ramesh on 14/8/16.
 */
public class SipCallState {
    //Call directions
    public static final int NONE = 0;
    public static final int INCOMING = 1;
    public static final int OUTGOING = 2;
    //Call states
    public static final int CALL_RINGING = 3;
    public static final int IN_CALL = 4;
    public static final int CALL_FINISHED = 5;

    private long startTime;

    private int callState = NONE;

    private int callDirection;

    private String mobileNumber;


    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getCallState() {
        return callState;
    }

    public void setCallState(int callState) {
        this.callState = callState;
    }

    public int getCallDir() {
        return callDirection;
    }

    public void setCallDir(int callDir) {
        this.callDirection = callDir;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }
}

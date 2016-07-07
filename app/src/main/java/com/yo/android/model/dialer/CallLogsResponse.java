package com.yo.android.model.dialer;

public class CallLogsResponse {

    private CallLogsData DATA;

    private String STATUS;

    public CallLogsData getDATA() {
        return DATA;
    }

    public void setDATA(CallLogsData DATA) {
        this.DATA = DATA;
    }

    public String getSTATUS() {
        return STATUS;
    }

    public void setSTATUS(String STATUS) {
        this.STATUS = STATUS;
    }

    @Override
    public String toString() {
        return "ClassPojo [DATA = " + DATA + ", STATUS = " + STATUS + "]";
    }
}
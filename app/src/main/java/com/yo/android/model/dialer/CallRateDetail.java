package com.yo.android.model.dialer;

public class CallRateDetail {

    private String PREFIX;

    private String RATE;

    private String DESTINATION;

    private String PULSE;

    private String PACKAGEID;

    public String getPrefix() {
        return PREFIX;
    }

    public void setPREFIX(String PREFIX) {
        this.PREFIX = PREFIX;
    }

    public String getRate() {
        return RATE;
    }

    public void setRATE(String RATE) {
        this.RATE = RATE;
    }

    public String getDestination() {
        return DESTINATION;
    }

    public void setDESTINATION(String DESTINATION) {
        this.DESTINATION = DESTINATION;
    }

    public String getPulse() {
        return PULSE;
    }

    public void setPULSE(String PULSE) {
        this.PULSE = PULSE;
    }

    public String getPACKAGEID() {
        return PACKAGEID;
    }

    public void setPACKAGEID(String PACKAGEID) {
        this.PACKAGEID = PACKAGEID;
    }

    @Override
    public String toString() {
        return "ClassPojo [PREFIX = " + PREFIX + ", RATE = " + RATE + ", DESTINATION = " + DESTINATION + ", PULSE = " + PULSE + ", PACKAGEID = " + PACKAGEID + "]";
    }
}
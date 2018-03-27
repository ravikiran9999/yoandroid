package com.yo.android.model.dialer;

import com.google.gson.annotations.SerializedName;

public class CallRateDetail {

    private String id;
    private boolean recentSelected;
    private String updated;

    @SerializedName("prefix")
    private String PREFIX;

    @SerializedName("rate")
    private String RATE;

    @SerializedName("destination")
    private String DESTINATION;

    @SerializedName("pulse")
    private String PULSE;

    @SerializedName("package_id")
    private String PACKAGEID;

    @SerializedName("us_code")
    private String US_CODE;

    @SerializedName("us_symbol")
    private String US_SYMBOL;

    @SerializedName("us_rate")
    private String US_RATE;

    @SerializedName("currency_code")
    private String localCurrencyCode;

    @SerializedName("currency_symbol")
    private String localCurrencySymbol;


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

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isRecentSelected() {
        return recentSelected;
    }

    public void setRecentSelected(boolean recentSelected) {
        this.recentSelected = recentSelected;
    }

    public String getPREFIX() {
        return PREFIX;
    }

    public String getRATE() {
        return RATE;
    }

    public String getDESTINATION() {
        return DESTINATION;
    }

    public String getPULSE() {
        return PULSE;
    }

    public String getUS_CODE() {
        return US_CODE;
    }

    public void setUS_CODE(String US_CODE) {
        this.US_CODE = US_CODE;
    }

    public String getUS_SYMBOL() {
        return US_SYMBOL;
    }

    public void setUS_SYMBOL(String US_SYMBOL) {
        this.US_SYMBOL = US_SYMBOL;
    }

    public String getUS_RATE() {
        return US_RATE;
    }

    public void setUS_RATE(String US_RATE) {
        this.US_RATE = US_RATE;
    }

    public String getLocalCurrencyCode() {
        return localCurrencyCode;
    }

    public void setLocalCurrencyCode(String localCurrencyCode) {
        this.localCurrencyCode = localCurrencyCode;
    }

    public String getLocalCurrencySymbol() {
        return localCurrencySymbol;
    }

    public void setLocalCurrencySymbol(String localCurrencySymbol) {
        this.localCurrencySymbol = localCurrencySymbol;
    }

    @Override
    public boolean equals(Object v) {
       /* boolean retVal = false;

        if (v instanceof CallRateDetail) {
            CallRateDetail ptr = (CallRateDetail) v;
            //retVal = ptr.getId() == this.id;
            retVal = ptr.getDestination() == this.getDestination();
        }

        return retVal;*/

        if (v instanceof CallRateDetail) {
            CallRateDetail articles = (CallRateDetail) v;
            return (articles.getDestination().equals(this.getDestination()));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        /*int hash = 7;
        //hash = 17 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 17 * hash + (this.getDestination() != null ? this.getDestination().hashCode() : 0);
        return hash;*/

        int hash = getDestination().hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "ClassPojo [id = " + id + ", updated = " + updated + ", recentSelected = " + recentSelected + ", PREFIX = " + PREFIX + ", RATE = " + RATE + ", DESTINATION = " + DESTINATION + ", PULSE = " + PULSE + ", PACKAGEID = " + PACKAGEID + "]";
    }
}
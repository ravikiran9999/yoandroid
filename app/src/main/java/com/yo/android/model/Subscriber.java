package com.yo.android.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by rajesh on 24/8/16.
 */
public class Subscriber {

    @SerializedName("STATUS")
    private String STATUS;

    @SerializedName("DATA")
    private DATA DATA;

    public String getSTATUS() {
        return STATUS;
    }

    public void setSTATUS(String STATUS) {
        this.STATUS = STATUS;
    }

    public DATA getDATA() {
        return DATA;
    }

    public void setDATA(DATA DATA) {
        this.DATA = DATA;
    }

    public static class DATA {
        @SerializedName("SUBSCRIBERID")
        private String SUBSCRIBERID;
        @SerializedName("USERNAME")
        private String USERNAME;
        @SerializedName("PASSWORD")
        private String PASSWORD;
        @SerializedName("CREDIT")
        private String CREDIT;
        @SerializedName("CALLINGCARDNUMBER")
        private String CALLINGCARDNUMBER;
        @SerializedName("MESSAGE")
        private String MESSAGE;

        public String getSUBSCRIBERID() {
            return SUBSCRIBERID;
        }

        public void setSUBSCRIBERID(String SUBSCRIBERID) {
            this.SUBSCRIBERID = SUBSCRIBERID;
        }

        public String getUSERNAME() {
            return USERNAME;
        }

        public void setUSERNAME(String USERNAME) {
            this.USERNAME = USERNAME;
        }

        public String getPASSWORD() {
            return PASSWORD;
        }

        public void setPASSWORD(String PASSWORD) {
            this.PASSWORD = PASSWORD;
        }

        public String getCREDIT() {
            return CREDIT;
        }

        public void setCREDIT(String CREDIT) {
            this.CREDIT = CREDIT;
        }

        public String getCALLINGCARDNUMBER() {
            return CALLINGCARDNUMBER;
        }

        public void setCALLINGCARDNUMBER(String CALLINGCARDNUMBER) {
            this.CALLINGCARDNUMBER = CALLINGCARDNUMBER;
        }

        public String getMESSAGE() {
            return MESSAGE;
        }

        public void setMESSAGE(String MESSAGE) {
            this.MESSAGE = MESSAGE;
        }
    }
}

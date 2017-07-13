package com.yo.android.model;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by rdoddapaneni on 7/6/2017.
 */

public class Lock {

    @SerializedName("code")
    @Expose
    private long code;
    @SerializedName("response")
    @Expose
    private String response;
    @SerializedName("data")
    @Expose
    private Data data;

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }


    public class Data {

        @SerializedName("isAutorenewalDone")
        @Expose
        private boolean isAutorenewalDone;
        @SerializedName("TrailPeriodFlag")
        @Expose
        private boolean trailPeriodFlag;
        @SerializedName("AutorenewalDate")
        @Expose
        private String autorenewalDate;
        @SerializedName("AutorenwalSubscription")
        @Expose
        private boolean autorenwalSubscription;
        @SerializedName("isMagzineLocked")
        @Expose
        private boolean isMagzineLocked;
        @SerializedName("isDailerLocked")
        @Expose
        private boolean isDailerLocked;
        @SerializedName("isAppLocked")
        @Expose
        private boolean isAppLocked;

        public boolean isIsAutorenewalDone() {
            return isAutorenewalDone;
        }

        public void setIsAutorenewalDone(boolean isAutorenewalDone) {
            this.isAutorenewalDone = isAutorenewalDone;
        }

        public boolean isTrailPeriodFlag() {
            return trailPeriodFlag;
        }

        public void setTrailPeriodFlag(boolean trailPeriodFlag) {
            this.trailPeriodFlag = trailPeriodFlag;
        }

        public String getAutorenewalDate() {
            return autorenewalDate;
        }

        public void setAutorenewalDate(String autorenewalDate) {
            this.autorenewalDate = autorenewalDate;
        }

        public boolean isAutorenwalSubscription() {
            return autorenwalSubscription;
        }

        public void setAutorenwalSubscription(boolean autorenwalSubscription) {
            this.autorenwalSubscription = autorenwalSubscription;
        }

        public boolean isIsMagzineLocked() {
            return isMagzineLocked;
        }

        public void setIsMagzineLocked(boolean isMagzineLocked) {
            this.isMagzineLocked = isMagzineLocked;
        }

        public boolean isIsDailerLocked() {
            return isDailerLocked;
        }

        public void setIsDailerLocked(boolean isDailerLocked) {
            this.isDailerLocked = isDailerLocked;
        }

        public boolean isIsAppLocked() {
            return isAppLocked;
        }

        public void setIsAppLocked(boolean isAppLocked) {
            this.isAppLocked = isAppLocked;
        }

    }
}





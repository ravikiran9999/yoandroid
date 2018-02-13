package com.yo.android.model.dialer;

import com.yo.android.model.Contact;

/**
 * Created by rajesh on 15/11/16.
 */
public class OpponentDetails {
    private String voxUserName;
    private Contact contact;
    private int statusCode;
    private boolean selfReject;

    public boolean isSelfReject() {
        return selfReject;
    }

    public void setSelfReject(boolean selfReject) {
        this.selfReject = selfReject;
    }


    public String getVoxUserName() {
        return voxUserName;
    }

    public Contact getContact() {
        return contact;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public OpponentDetails(String voxUserName, Contact contact, int statusCode) {
        this.voxUserName = voxUserName;
        this.contact = contact;
        this.statusCode = statusCode;
    }
}

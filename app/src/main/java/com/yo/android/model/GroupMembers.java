package com.yo.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by rdoddapaneni on 8/31/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
//public class GroupMembers implements Parcelable {
public class GroupMembers {
    private String fullName;
    private String mobileNumber;
    private String admin;
    private String userId;


    public GroupMembers() {
        // empty default constructor, necessary for Firebase to be able to deserialize
    }

    public GroupMembers(String fullName, String mobileNumber, String admin, String userId) {
        this.fullName = fullName;
        this.mobileNumber = mobileNumber;
        this.admin = admin;
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

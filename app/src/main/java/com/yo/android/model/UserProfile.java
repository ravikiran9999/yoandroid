package com.yo.android.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by rdoddapaneni on 9/1/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserProfile {

    private String fullName;
    private String mobileNumber;

    public UserProfile() {
        // empty default constructor, necessary for Firebase to be able to deserialize
    }

    public UserProfile(String fullName, String mobileNumber) {
        this.fullName = fullName;
        this.mobileNumber = mobileNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }
}

package com.yo.android.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by rdoddapaneni on 9/1/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserProfile {

    private String fullName;
    private String mobileNumber;
    private String countryCode;
    private String phoneNumber;
    private String image;
    private String firebaseRoomId;
    private String nexgeUserName;

    public UserProfile() {
        // empty default constructor, necessary for Firebase to be able to deserialize
    }

    public UserProfile(String fullName, String mobileNumber, String countryCode, String phoneNumber, String image, String firebaseRoomId, String nexgeUserName) {
        this.fullName = fullName;
        this.mobileNumber = mobileNumber;
        this.countryCode = countryCode;
        this.phoneNumber = phoneNumber;
        this.image = image;
        this.firebaseRoomId = firebaseRoomId;
        this.nexgeUserName = nexgeUserName;
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

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getImage() {
        return image;
    }

    public String getFirebaseRoomId() {
        return firebaseRoomId;
    }

    public String getNexgeUserName() {
        return nexgeUserName;
    }
}

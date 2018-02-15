package com.yo.android.database.model;

import io.realm.RealmObject;

/**
 * Created by rdoddapaneni on 15-02-2018.
 */

public class DBUserProfile extends RealmObject {

    private String fullName;
    private String mobileNumber;
    private String countryCode;
    private String phoneNumber;
    private String image;
    private String firebaseRoomId;
    private String nexgeUserName;

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

    public void setImage(String image) {
        this.image = image;
    }

    public String getFirebaseRoomId() {
        return firebaseRoomId;
    }

    public void setFirebaseRoomId(String firebaseRoomId) {
        this.firebaseRoomId = firebaseRoomId;
    }

    public String getNexgeUserName() {
        return nexgeUserName;
    }

    public void setNexgeUserName(String nexgeUserName) {
        this.nexgeUserName = nexgeUserName;
    }
}

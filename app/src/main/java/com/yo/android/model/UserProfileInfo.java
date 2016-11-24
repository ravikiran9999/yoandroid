package com.yo.android.model;

import com.google.gson.annotations.SerializedName;
import com.yo.android.util.Constants;

/**
 * Created by Ramesh on 17/7/16.
 */
public class UserProfileInfo {

    @SerializedName(Constants.ID)
    private String id;

    @SerializedName(Constants.PHONE_NO)
    private String phoneNumber;

    @SerializedName(Constants.DESCRIPTION)
    private String description;

    private String firebaseUserId;

    @SerializedName(Constants.FIRST_NAME)
    private String first_name;

    @SerializedName(Constants.LAST_NAME)
    private String last_name;

    @SerializedName(Constants.AVATAR)
    private String avatar;

    @SerializedName(Constants.GENDER)
    private String gender;

    @SerializedName(Constants.DOB)
    private String dob;

    @SerializedName(Constants.EMAIL)
    private String email;

    @SerializedName("contacts_sync")
    private boolean syncContacts;

    @SerializedName("notification_alert")
    private boolean notificationAlert;

    private boolean representative;

    public boolean isSyncContacts() {
        return syncContacts;
    }

    public void setSyncContacts(boolean syncContacts) {
        this.syncContacts = syncContacts;
    }

    public boolean isNotificationAlert() {
        return notificationAlert;
    }

    public void setNotificationAlert(boolean notificationAlert) {
        this.notificationAlert = notificationAlert;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDescription() {
        return description;
    }

    public String getFirstName() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getFirebaseUserId() {
        return firebaseUserId;
    }

    public void setFirebaseUserId(String firebaseUserId) {
        this.firebaseUserId = firebaseUserId;
    }

    public boolean isRepresentative() {
        return representative;
    }

    public void setRepresentative(boolean representative) {
        this.representative = representative;
    }

    public String getFirst_name() {
        return first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public String getGender() {
        return gender;
    }

    public String getDob() {
        return dob;
    }

    public String getEmail() {
        return email;
    }
}

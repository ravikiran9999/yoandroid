package com.yo.android.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Ramesh on 17/7/16.
 */
public class UserProfileInfo {

    private String id;
    private String phoneNumber;
    private String description;

    private String first_name;
    private String avatar;
    @SerializedName("contacts_sync")
    private boolean syncContacts;


    @SerializedName("notification_alert")
    private boolean notificationAlert;

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
}

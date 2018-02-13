package com.yo.android.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by rajesh on 24/8/16.
 */
public class Subscriber {

    @SerializedName("nexge_subscriber_id")
    private String nexge_subscriber_id;
    @SerializedName("nexge_subscriber_username")
    private String nexge_subscriber_username;
    @SerializedName("nexge_subscriber_password")
    private String nexge_subscriber_password;
    @SerializedName("nexge_subscriber_telID")
    private String nexge_subscriber_telID;

    public String getNexge_subscriber_id() {
        return nexge_subscriber_id;
    }

    public void setNexge_subscriber_id(String nexge_subscriber_id) {
        this.nexge_subscriber_id = nexge_subscriber_id;
    }

    public String getNexge_subscriber_username() {
        return nexge_subscriber_username;
    }

    public void setNexge_subscriber_username(String nexge_subscriber_username) {
        this.nexge_subscriber_username = nexge_subscriber_username;
    }

    public String getNexge_subscriber_password() {
        return nexge_subscriber_password;
    }

    public void setNexge_subscriber_password(String nexge_subscriber_password) {
        this.nexge_subscriber_password = nexge_subscriber_password;
    }

    public String getNexge_subscriber_telID() {
        return nexge_subscriber_telID;
    }

    public void setNexge_subscriber_telID(String nexge_subscriber_telID) {
        this.nexge_subscriber_telID = nexge_subscriber_telID;
    }
}

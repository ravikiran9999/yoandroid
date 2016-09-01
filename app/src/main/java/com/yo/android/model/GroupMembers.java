package com.yo.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by rdoddapaneni on 8/31/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GroupMembers {

    private String admin;
    private String userId;
    private UserProfile userProfile;


    public GroupMembers() {
        // empty default constructor, necessary for Firebase to be able to deserialize
    }

    public GroupMembers(String admin, String userId) {
        this.admin = admin;
        this.userId = userId;
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

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }
}

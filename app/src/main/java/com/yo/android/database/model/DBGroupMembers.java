package com.yo.android.database.model;

import com.yo.android.model.UserProfile;

import io.realm.RealmObject;

/**
 * Created by rdoddapaneni on 15-02-2018.
 */

public class DBGroupMembers extends RealmObject {

    private String admin;
    private String userId;
    private DBUserProfile userProfile;

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

    public DBUserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(DBUserProfile userProfile) {
        this.userProfile = userProfile;
    }
}

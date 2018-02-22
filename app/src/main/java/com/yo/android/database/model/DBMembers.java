package com.yo.android.database.model;

//import io.realm.RealmObject;

import io.realm.RealmObject;

/**
 * Created by Chaatz on 15-02-2018.
 */

public class DBMembers extends RealmObject {

    private String id;
    private String mobileNumber;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }
}

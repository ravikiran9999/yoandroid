package com.yo.android.model;

/**
 * Created by rdoddapaneni on 6/29/2016.
 */

public class Registration {

    //private String emailId;
    private String password;
    private String phoneNumber;

    public Registration() {
        // empty default constructor, necessary for Firebase to be able to deserialize
    }

    public Registration(String password, String phoneNumber) {
        //this.emailId = emailId;
        this.password = password;
        this.phoneNumber = phoneNumber;
    }

    /*public String getEmailId() {
        return emailId;
    }*/

    public String getPassword() {
        return password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}

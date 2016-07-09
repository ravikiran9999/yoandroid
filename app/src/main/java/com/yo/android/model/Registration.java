package com.yo.android.model;

import java.util.ArrayList;

/**
 * Created by rdoddapaneni on 6/29/2016.
 */

public class Registration {

    private ArrayList<Contacts> mPhoneContacts;

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

    public String getPassword() {
        return password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public ArrayList<Contacts> getmPhoneContacts() {
        return mPhoneContacts;
    }

    public void setmPhoneContacts(ArrayList<Contacts> mPhoneContacts) {
        this.mPhoneContacts = mPhoneContacts;
    }
}

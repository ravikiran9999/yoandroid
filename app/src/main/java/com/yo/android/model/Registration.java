package com.yo.android.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rdoddapaneni on 6/29/2016.
 */

public class Registration {

    private List<Contacts> mPhoneContacts;

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

    public List<Contacts> getmPhoneContacts() {
        return mPhoneContacts;
    }

    public void setmPhoneContacts(ArrayList<Contacts> mPhoneContacts) {
        this.mPhoneContacts = mPhoneContacts;
    }

}

package com.yo.android.vox;

import java.util.ArrayList;

public class AddSubscriberBodyData extends AbstractData {


    private String PACKAGEID;
    private String USERNAME;
    private String PASSWORD;
    private String FULLNAME;
    private String ADDRESS;
    private String EXPIRYDATE;
    private String EMAILID;
    private String PHONENUMBER;
    private String STATUS;
    private String COUNTRYCODE;
    private String MAXCALL;
    private String CREDIT;
    private ArrayList<String> CALLERIDARRAY;


    public AddSubscriberBodyData(String USERNAME, final String strMobileNo) {
        this.PACKAGEID = "1";
        this.USERNAME = USERNAME;
        this.PASSWORD = "123456";
        this.FULLNAME = "John Dev";
        this.ADDRESS = "22-2-11,xyz";
        this.EXPIRYDATE = "2019-08-27";
        this.EMAILID = "test@gmail.com";
        this.PHONENUMBER = strMobileNo;
        this.STATUS = "1";
        this.COUNTRYCODE = "91";
        this.MAXCALL = "1";
        this.CREDIT = "2";
        this.CALLERIDARRAY = new ArrayList<String>();
        CALLERIDARRAY.add(strMobileNo);
    }


}

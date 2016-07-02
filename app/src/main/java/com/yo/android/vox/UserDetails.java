package com.yo.android.vox;

/**
 * Created by Ramesh on 2/7/16.
 */
public class UserDetails {
    protected String LOGINUSER = "droid";
    protected String LOGINSECRET = "30aa498c5be84f703add8e0b1ff69fc9620e71a7";
    protected String SECTION = "OTP";
    protected String ACTION = "OTPREQUEST";
    protected AbstractData DATA;

    public UserDetails(String user, String secret, String section, String action) {
        LOGINUSER = user;
        LOGINSECRET = secret;
        SECTION = section;
        ACTION = action;
    }

    public void setData(AbstractData data) {
        this.DATA = data;
    }

}

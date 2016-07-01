package com.yo.android.vox;

/**
 * Created by Ramesh on 1/7/16.
 */
public class OTPBody {
    private String LOGINUSER = "droid";
    private String LOGINSECRET = "30aa498c5be84f703add8e0b1ff69fc9620e71a7";
    private String SECTION = "OTP";
    private String ACTION = "OTPREQUEST";

    public OTPBody() {
        DATA = new Data();
    }

    public OTPBody(String LOGINUSER, String LOGINSECRET) {
        DATA = new Data();
    }

    public OTPBody addNumber(String number) {
        DATA.PIN = number;
        return this;
    }

    private Data DATA;

    private class Data {
        //        String PIN = "8341569102";
//        String TYPE = "3";
//        String PACKAGEID = "1";
        String PIN = "919573535345";
        String TYPE = "1";
        String PACKAGEID = "1";

    }
}

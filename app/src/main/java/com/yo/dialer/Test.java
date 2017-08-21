package com.yo.dialer;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.yo.android.model.Contact;
import com.yo.dialer.ui.IncomingCallActivity;
import com.yo.dialer.yopj.YoCall;

/**
 * Created by root on 27/7/17.
 */

public class Test {
    private static final String TAG = Test.class.getSimpleName();

    public static void main(String[] args) {
        getPhoneNumber();


    }

    public static void startInComingCallScreen(Context context) {
        final Intent intent = new Intent(context, IncomingCallActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        try {
            // String calleeNumber = DialerHelper.getInstance(YoSipService.this).getPhoneNumber(yoCall);
            // Contact contact = DialerHelper.getInstance(YoSipService.this).readCalleeDetailsFromDB(mContactsSyncManager, calleeNumber);
            intent.putExtra(CallExtras.CALLER_NO, "youser919490570720");
            intent.putExtra(CallExtras.IMAGE, "http://");
            intent.putExtra(CallExtras.PHONE_NUMBER, "919490570720");
            intent.putExtra(CallExtras.NAME, "Rajesh Babu");
            //Wait until user profile image is loaded , it should not show blank image
            context.startActivity(intent);
        } catch (Exception e) {
            DialerLogs.messageE(TAG, "YO====startInComingCallScreen==" + e.getMessage());
        }
    }

    public static void getPhoneNumber() {
        // String mobileNumber = "919490570791";
        String mobileNumber = "97477498669";

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        try {
            // phone must begin with '+'
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse("+" + mobileNumber, "");
            int countryCode = numberProto.getCountryCode();
            String country = countryCode + "";
            System.out.println(country.length() + " Country code " + countryCode);
            String mobileTemp = mobileNumber;
            String phoneNumber = mobileTemp.substring(country.length(), mobileTemp.length());
            System.out.println("Phone number " + phoneNumber);
        } catch (NumberParseException e) {
            Log.e(TAG, "NumberParseException was thrown: " + e.toString());
        }
    }

}
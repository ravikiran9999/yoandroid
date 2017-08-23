package com.yo.dialer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.yo.android.BuildConfig;
import com.yo.android.app.BaseApp;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.di.Injector;
import com.yo.android.model.Contact;
import com.yo.dialer.yopj.YoCall;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

/**
 * Created by root on 11/7/17.
 */

public class DialerHelper {

    private static DialerHelper dialerHelper;
    private static final String TAG = DialerHelper.class.getSimpleName();
    private static Context mContext;

    public static DialerHelper getInstance(Context context) {
        mContext = context;
        if (dialerHelper == null) {
            dialerHelper = new DialerHelper();
        }
        return dialerHelper;
    }

    public String parsePhoneNumber(String username) {
        try {
            DialerLogs.messageE(TAG, "Username for parse Phone number" + username);
            if (username != null) {
                return username.substring(username.indexOf(BuildConfig.RELEASE_USER_TYPE) + 6, username.length() - 1);
            }
        } catch (StringIndexOutOfBoundsException ex) {
            DialerLogs.messageE(TAG, "Parsing YO USER EXception " + ex.getMessage());
        }
        return username;
    }


    public String getURI(String displayname, String username, String domain) {
        return String.format("\"%s\"<sip:%s@%s>", displayname, username, domain);
    }

    public String getRegister(String domain) {
        return String.format("sip:%s", domain);
    }


    public String getPhoneNumber(YoCall call) {
        if (call == null) {
            DialerLogs.messageI(TAG, "YO=======getPhoneNumber Call object is null===");
            return "";
        }
        String remoteUriStr = null;
        try {
            remoteUriStr = call.getInfo().getRemoteUri();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (remoteUriStr == null) {
            return null;
        }

        DialerLogs.messageI(TAG, "YO=======getPhoneNumber Remote URI===" + remoteUriStr);
        String title = null;
        String part2 = "";
        String ip = null;
        //EX:"8341569102" <sip:8341569102@209.239.120.239>
        String regex = "\"(.+?)\" \\<sip:(.+?)@(.+?)\\>";
        if (regex == null) {
            return null;
        }
        Pattern p = Pattern.compile(regex);
        Matcher matcher = p.matcher(remoteUriStr);
        if (matcher.matches()) {
            title = matcher.group(1);
            part2 = matcher.group(2);
            ip = matcher.group(3);
        }
        DialerLogs.messageI(TAG, "YO=======getPhoneNumber TITLE===" + title + ",IP=" + ip + ",Part2=" + part2);

        return part2;
    }

    public Contact readCalleeDetailsFromDB(ContactsSyncManager mContactsSyncManager, String calleeNumber) {
        DialerLogs.messageI(TAG, "YO=======readCalleeDetailsFromDB===" + calleeNumber);
        if (TextUtils.isEmpty(calleeNumber)) {
            DialerLogs.messageI(TAG, "YO=======aDDING TEMP,readCalleeDetailsFromDB===" + calleeNumber);
        }
        Contact contact = mContactsSyncManager.getContactByVoxUserName(calleeNumber);
        if (contact != null) {
            DialerLogs.messageI(TAG, "YO=======Image===" + contact.getImage());
            DialerLogs.messageI(TAG, "YO=======Name===" + contact.getName());
            DialerLogs.messageI(TAG, "YO=======YOUSER Name===" + contact.getNexgieUserName());
            DialerLogs.messageI(TAG, "YO=======PhoneNumber===" + contact.getPhoneNo());
            return contact;
        } else {
            //if callee Name is not found in the YO Contacts, search for PhoneBook with the number
            // To get the number from the yo username need to parse.
            String calleePhoneNumber = parsePhoneNumber(calleeNumber);
            //Callee Phone Number with country code.
            DialerLogs.messageI(TAG, "YO=======CallePhoneNumber===" + calleePhoneNumber);

            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            try {
                // phone must begin with '+'
                Phonenumber.PhoneNumber numberProto = phoneUtil.parse("+" + calleePhoneNumber, "");
                int countryCode = numberProto.getCountryCode();
                String countryCodeString = countryCode + "";
                String mobileTemp = calleePhoneNumber;
                String phoneNumber = mobileTemp.substring(countryCodeString.length(), mobileTemp.length());
                contact = mContactsSyncManager.getContactPSTN(countryCode, phoneNumber);
            } catch (NumberParseException e) {
                DialerLogs.messageE(TAG, "NumberParseException was thrown: " + e.toString());
            }

            if (contact != null) {
                return contact;
            } else {
                return new Contact();
            }
        }

    }

    public void loadImageIntoCache(String imagePath, ImageLoadedCallback imageLoadedCallback) throws ExecutionException, InterruptedException {
        FutureTarget<File> future = Glide.with(mContext)
                .load(imagePath)
                .downloadOnly(500, 500);
        imageLoadedCallback.onImageLoadComplete(future.get());
    }

    public static String getCountryCodeFromNexgeUsername(final String calleePhoneNumber) {

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            // phone must begin with '+'
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse("+" + calleePhoneNumber, "");
            int countryCode = numberProto.getCountryCode();
            String countryCodeString = countryCode + "";
            return countryCodeString;
        } catch (NumberParseException e) {
            DialerLogs.messageE(TAG, "NumberParseException was thrown: " + e.toString());
        }
        return null;
    }
}

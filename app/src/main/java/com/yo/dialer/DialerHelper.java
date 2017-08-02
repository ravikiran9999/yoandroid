package com.yo.dialer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
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
        if (username != null) {
            return username.substring(username.indexOf(BuildConfig.RELEASE_USER_TYPE) + 6, username.length() - 1);
        }
        return username;
    }

    public String getURI(String displayname, String username, String domain) {
        return String.format("\"%s\"<sip:%s@%s>", displayname, username, domain);
    }

    public String getRegister(String domain) {
        return String.format("sip:%s", domain);
    }


    public String getPhoneNumber(YoCall call) throws Exception {
        String remoteUriStr = call.getInfo().getRemoteUri();
        String part2 = "";
        String regex = "\"(.+?)\" \\<sip:(.+?)@(.+?)\\>";
        Pattern p = Pattern.compile(regex);
        Matcher matcher = p.matcher(remoteUriStr);
        if (matcher.matches()) {
            part2 = matcher.group(2);
        }
        return part2;
    }

    public Contact readCalleeDetailsFromDB(ContactsSyncManager mContactsSyncManager, String calleeNumber) {
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
            return new Contact();
        }

    }

    public void loadImageIntoCache(String imagePath, ImageLoadedCallback imageLoadedCallback) throws ExecutionException, InterruptedException {
        FutureTarget<File> future = Glide.with(mContext)
                .load(imagePath)
                .downloadOnly(500, 500);
        imageLoadedCallback.onImageLoadComplete(future.get());
    }
}

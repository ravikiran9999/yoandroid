package com.yo.dialer;

import android.content.Context;
import android.content.Intent;

import com.yo.android.model.Contact;
import com.yo.dialer.ui.IncomingCallActivity;
import com.yo.dialer.yopj.YoCall;

/**
 * Created by root on 27/7/17.
 */

public class Test {
    private static final String TAG = Test.class.getSimpleName();

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

}

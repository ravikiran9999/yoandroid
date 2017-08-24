package com.yo.feedback;

import com.yo.dialer.CallHelper;
import com.yo.dialer.DialerLogs;

/**
 * Created by root on 24/8/17.
 */

public class AppFailureReport {
    private static final String TAG = AppFailureReport.class.getSimpleName();

    public static void sendDetails(String s) {
        DialerLogs.messageE(TAG, s);
        CallHelper.appendLog(s);
    }

    public static void sendSuccessDetails(String s) {
        sendDetails(s);
        CallHelper.appendLog(s);
    }
}

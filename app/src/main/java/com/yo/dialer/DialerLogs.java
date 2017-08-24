package com.yo.dialer;

import android.util.Log;

/**
 * Created by root on 11/7/17.
 */

public class DialerLogs {
    private static final String COMMON_STRING = "YO======";

    public static void messageW(final String TAG, final String message) {
        if (DialerConfig.ENABLE_LOGS) {
            Log.w(TAG, COMMON_STRING + message);
            write(message);
        }
    }

    private static void write(String message) {
        CallHelper.appendLog(message);
    }

    public static void messageE(final String TAG, final String message) {
        if (DialerConfig.ENABLE_LOGS) {
            Log.e(TAG, COMMON_STRING + message);
            write(message);
        }
    }

    public static void messageI(final String TAG, final String message) {
        if (DialerConfig.ENABLE_LOGS) {
            Log.i(TAG, COMMON_STRING + message);
            write(message);
        }
    }

}

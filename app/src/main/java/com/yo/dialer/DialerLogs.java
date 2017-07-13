package com.yo.dialer;

import android.util.Log;

/**
 * Created by root on 11/7/17.
 */

public class DialerLogs {
    public static void messageW(final String TAG, final String message) {
        if (DialerConfig.ENABLE_LOGS) {
            Log.w(TAG, message);
        }
    }

    public static void messageE(final String TAG, final String message) {
        if (DialerConfig.ENABLE_LOGS) {
            Log.e(TAG, message);
        }
    }

    public static void messageI(final String TAG, final String message) {
        if (DialerConfig.ENABLE_LOGS) {
            Log.i(TAG, message);
        }
    }

}
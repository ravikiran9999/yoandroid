package com.yo.android.networkmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.yo.dialer.CallExtras;
import com.yo.dialer.DialerLogs;

/**
 * Created by root on 23/8/17.
 */

public class StartServiceAtBootReceiver extends BroadcastReceiver {
    private static final String TAG = StartServiceAtBootReceiver.class.getSimpleName();

    public void onReceive(Context context, Intent intent) {
        DialerLogs.messageE(TAG, "Service loaded at BOOT_COMPLETED");

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent service = new Intent(context, com.yo.dialer.YoSipService.class);
            service.setAction(CallExtras.REGISTER);
            context.startService(service);
            Log.v(TAG, "Service loaded at start");
        }
    }
}

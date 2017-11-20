package com.yo.android.networkmanager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.yo.android.util.FetchNewArticlesService;
import com.yo.dialer.CallExtras;
import com.yo.dialer.DialerLogs;
import com.yo.services.BackgroundServices;

import java.util.Calendar;

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

            service = new Intent(context, BackgroundServices.class);
            service.setAction(BackgroundServices.FETCH_CALL_RATES);
            context.startService(service);

            service = new Intent(context, BackgroundServices.class);
            service.setAction(BackgroundServices.SYNC_OFFLINE_CONTACTS);
            context.startService(service);
            Log.v(TAG, "Service loaded at start");

            int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY); //Current hour
            int currentMin = Calendar.getInstance().get(Calendar.MINUTE); //Current hour
            int currentSec = Calendar.getInstance().get(Calendar.SECOND); //Current hour
            int currentTimeInSec = currentHour * 60 * 60 + currentMin * 60 + currentSec;
            // Start service using AlarmManager
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Intent fetchArticlesintent = new Intent(context, FetchNewArticlesService.class);
            PendingIntent pintent = PendingIntent.getService(context, 1014, fetchArticlesintent,
                    0);
            AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, (((24 * 60 * 60) - currentTimeInSec) * 1000),
                    AlarmManager.INTERVAL_DAY, pintent);
        }
    }
}

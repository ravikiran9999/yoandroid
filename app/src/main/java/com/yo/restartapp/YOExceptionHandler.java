package com.yo.restartapp;

import android.app.Activity;
import android.content.Intent;

import com.yo.android.ui.BottomTabsActivity;

/**
 * Created by Rajesh Babu on 1/8/17.
 */

public class YOExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Activity activity;

    public YOExceptionHandler(Activity a) {
        activity = a;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Intent intent = new Intent(activity, BottomTabsActivity.class);
        intent.putExtra("crash", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);

        //PendingIntent pendingIntent = PendingIntent.getActivity(BaseApp.get().getBaseContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);

        //AlarmManager mgr = (AlarmManager) BaseApp.get().getBaseContext().getSystemService(Context.ALARM_SERVICE);
       // mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);

        activity.finish();
        System.exit(2);
    }
}
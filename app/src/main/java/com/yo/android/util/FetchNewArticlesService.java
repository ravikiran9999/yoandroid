package com.yo.android.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.di.Injector;

import java.util.Calendar;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * This service is used to fetch the articles from the server once a day
 */
public class FetchNewArticlesService extends Service {
    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;
    public FetchNewArticlesService() {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("FetchNewArticlesService", "FetchNewArticlesService Started");
        preferenceEndPoint.saveBooleanPreference(Constants.IS_SERVICE_RUNNING, false);
        preferenceEndPoint.saveBooleanPreference(Constants.IS_ARTICLES_POSTED, false);
        preferenceEndPoint.saveBooleanPreference(Constants.STARTING_SERVICE, true);
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY); //Current hour
        int currentMin = Calendar.getInstance().get(Calendar.MINUTE); //Current hour
        int currentSec = Calendar.getInstance().get(Calendar.SECOND); //Current hour
        if(currentHour == 0 && currentMin == 0 && currentSec == 0) {
            preferenceEndPoint.saveBooleanPreference(Constants.IS_SERVICE_RUNNING, true);
            de.greenrobot.event.EventBus.getDefault().post(Constants.START_FETCHING_ARTICLES_ACTION);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Injector.obtain(getApplication()).inject(this);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        Log.d("FetchNewArticlesService", "onTaskRemoved()");

        /*Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Intent intent = new Intent(getApplicationContext(), FetchNewArticlesService.class);
        PendingIntent pintent = PendingIntent.getService(this, 1014, intent,
                0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                Constants.FETCHING_NEW_ARTICLES_FREQUENCY, pintent);*/

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
        Intent intent = new Intent(getApplicationContext(), FetchNewArticlesService.class);
        PendingIntent pintent = PendingIntent.getService(this, 1014, intent,
                0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, (((24 * 60 * 60) - currentTimeInSec) * 1000),
                AlarmManager.INTERVAL_DAY, pintent);
    }

}

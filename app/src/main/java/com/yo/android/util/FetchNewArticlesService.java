package com.yo.android.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
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
    public static PendingIntent pintent;

    public FetchNewArticlesService() {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("FetchNewArticlesService", "FetchNewArticlesService Started");
        if(TextUtils.isEmpty(preferenceEndPoint.getStringPreference(Constants.VOX_USER_NAME))) {
                 stopSelf();
        }
        preferenceEndPoint.saveBooleanPreference(Constants.IS_SERVICE_RUNNING, false);
        preferenceEndPoint.saveBooleanPreference(Constants.IS_ARTICLES_POSTED, false);
        preferenceEndPoint.saveBooleanPreference(Constants.STARTING_SERVICE, true);
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY); //Current hour
        if (currentHour == 1) { // 1 am
            //showTrayNotification();
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

        Calendar cal = Calendar.getInstance();
        long currenttime =  cal.getTimeInMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 1);
        calendar.set(Calendar.MINUTE, 0);
        long settime = calendar.getTimeInMillis();

        long differencetime = settime -  currenttime;
        int dif=(int)differencetime/1000;

        cal.set(Calendar.SECOND, calendar.get(Calendar.SECOND) + dif);

        Intent intent = new Intent(getApplicationContext(), FetchNewArticlesService.class);
        pintent = PendingIntent.getService(this, 1014, intent,
                0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pintent);
    }
}

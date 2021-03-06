package com.yo.android.util;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.text.TextUtilsCompat;
import android.text.TextUtils;
import android.util.Log;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.di.Injector;
import com.yo.android.ui.BottomTabsActivity;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;

import static com.yo.android.chat.notification.localnotificationsbuilder.Notifications.GROUP_NOTIFICATION_ID;

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

        // Start service using AlarmManager
        Calendar cal = Calendar.getInstance();
        /*cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);*/
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
        /*alarm.setRepeating(AlarmManager.RTC_WAKEUP, (((24 * 60 * 60) - currentTimeInSec) * 1000),
                AlarmManager.INTERVAL_DAY, pintent);*/
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pintent);
    }

}

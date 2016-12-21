package com.yo.android.util;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.di.Injector;

import javax.inject.Inject;
import javax.inject.Named;

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
        preferenceEndPoint.saveBooleanPreference(Constants.IS_SERVICE_RUNNING, true);
        de.greenrobot.event.EventBus.getDefault().post(Constants.START_FETCHING_ARTICLES_ACTION);
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

}

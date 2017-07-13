package com.yo.services;

import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.api.YoApi;
import com.yo.android.di.InjectedService;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by root on 11/7/17.
 */

public class BackgroundServices extends InjectedService {
    public final static String FETCH_CALL_RATES = "action_fetch_call_rates";
    public final static boolean ENABLE_LOGS = true;
    private static final String TAG = BackgroundServices.class.getSimpleName();

    @Inject
    protected YoApi.YoService yoService;

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ENABLE_LOGS) {
            Log.w(TAG, "Background service  is called");
        }
        performAction(intent);
        return START_STICKY;
    }


    private void performAction(Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            if (action.equalsIgnoreCase(FETCH_CALL_RATES)) {
                fetchCallRates();
            }
        }
    }

    private void fetchCallRates() {
        CallRates.fetch(yoService, preferenceEndPoint);
    }
}

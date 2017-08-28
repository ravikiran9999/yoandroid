package com.yo.android.util;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.util.Log;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.orion.android.common.preferences.PreferenceEndPointImpl;
import com.yo.android.di.Injector;
import com.yo.android.model.Contact;
import com.yo.android.pjsip.SipBinder;
import com.yo.android.pjsip.SipProfile;
import com.yo.android.pjsip.YoSipService;

import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.pjsip_inv_state;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Rajesh Babu on 27/8/16.
 */
public class ReCreateService extends Service {
    protected static PreferenceEndPoint preferenceEndPoint;
    private static ReCreateService reCreateServiceIns;

    public static ReCreateService getInstance(Context context) {
        if (reCreateServiceIns == null) {
            reCreateServiceIns = new ReCreateService();
            preferenceEndPoint = new PreferenceEndPointImpl(context, "login");
        }
        return reCreateServiceIns;
    }


    private static SipBinder sipBinder;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof SipBinder) {
                sipBinder = (SipBinder) service;
                sipBinder.getYOHandler().addAccount(ReCreateService.this);
            }
            Log.w("ReCreateService", "Created account addAccount onServiceConnected...." + sipBinder);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            sipBinder = null;
            Log.w("ReCreateService", "Created account addAccount onServiceDisconnected ");

        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            bindService(new Intent(this, com.yo.dialer.YoSipService.class), connection, Context.BIND_AUTO_CREATE);
        }
        Log.w("ReCreateService", "Created account  onStartCommand ");
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

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
 * Created by rajesh on 27/8/16.
 */
public class ReCreateService extends Service {
    protected static PreferenceEndPoint preferenceEndPoint;
    private static ReCreateService reCreateServiceIns;
    private Context context;
    String username;
    String password;

    public static ReCreateService getInstance(Context context) {
        if (reCreateServiceIns == null) {
            reCreateServiceIns = new ReCreateService();
            preferenceEndPoint = new PreferenceEndPointImpl(context, "login");
            reCreateServiceIns.context = context;
        }
        return reCreateServiceIns;
    }


    private static SipBinder sipBinder;
    private Handler mHandler = new Handler();

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            sipBinder = (SipBinder) service;
            Log.w("ReCreateService", "Created account addAccount onServiceConnected...." + sipBinder);

            addAccount();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            sipBinder = null;
            Log.w("ReCreateService", "Created account addAccount onServiceDisconnected ");

        }
    };

    public void start(Context context) {
    }

    private void addAccount() {
        Log.e("YOService-Recreate", preferenceEndPoint + "");

        SipProfile sipProfile = new SipProfile.Builder()
                .withUserName(username)
                //.withUserName("64728474")
                //.withUserName("7032427")
                //.withUserName("64724865")
                //.withUserName("603703")
                .withPassword(password)
                //     .withPassword("534653")
                //.withPassword("@pa1ra2di3gm")
                //.withPassword("823859")
                //.withPassword("@pa1ra2di3gm")
                //       .withServer("209.239.120.239")
                .withServer("173.82.147.172")
                .build();
        sipBinder.getHandler().createSipService(sipProfile);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            username = intent.getStringExtra(Constants.VOX_USER_NAME);
            password = intent.getStringExtra(Constants.PASSWORD);
            bindService(new Intent(this, YoSipService.class), connection, Context.BIND_AUTO_CREATE);
        }
        Log.w("ReCreateService", "Created account  onStartCommand " + username + "......" + password);

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}

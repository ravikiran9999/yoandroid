package com.yo.android.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
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
public class ReCreateService {
    protected static PreferenceEndPoint preferenceEndPoint;
    private static ReCreateService reCreateServiceIns;
    private Context context;

    public static ReCreateService getInstance(Context context){
        if(reCreateServiceIns == null){
            reCreateServiceIns = new ReCreateService();
            preferenceEndPoint =new PreferenceEndPointImpl(context, "login");
           reCreateServiceIns.context = context;
        }
        return reCreateServiceIns;
    }


    private static SipBinder sipBinder;
    private Handler mHandler = new Handler();

    private static ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            sipBinder = (SipBinder) service;
            addAccount(reCreateServiceIns.context);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            sipBinder = null;
        }
    };
    public void start(Context context){
        context.bindService(new Intent(context, YoSipService.class), connection, context.BIND_AUTO_CREATE);
    }
    private static void addAccount(Context context) {
        Log.e("YOService-Recreate",preferenceEndPoint+"");
        if(preferenceEndPoint!=null) {
            String username = preferenceEndPoint.getStringPreference(Constants.VOX_USER_NAME, null);
            String password = preferenceEndPoint.getStringPreference(Constants.PASSWORD, null);
            SipProfile sipProfile = new SipProfile.Builder()
                    //.withUserName(username)
                    .withUserName("64728474")
                    //.withUserName("7032427")
                    //.withUserName("64724865")
                    //.withUserName("603703")
                     //       .withPassword(password)
                            .withPassword("534653")
                    //.withPassword("@pa1ra2di3gm")
                    //.withPassword("823859")
                    //.withPassword("@pa1ra2di3gm")
                     //       .withServer("209.239.120.239")
                    .withServer("173.82.147.172")
                    .build();
            sipBinder.getHandler().addAccount(sipProfile);
        }
    }
}

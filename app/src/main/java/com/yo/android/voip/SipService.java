package com.yo.android.voip;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.orion.android.common.logger.Log;
import com.yo.android.di.InjectedService;

import javax.inject.Inject;

public class SipService extends InjectedService {

    Receiver receiver;
    @Inject
    Log mLog;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        mLog.d("SipService DESTORYED", "SipService DESTORYED");
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    @Override
    public void onCreate() {
        mLog.d("YO.SipService", "SipService CREATED");
        if (receiver == null) {
            IntentFilter intentfilter = new IntentFilter();
            intentfilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            intentfilter.addAction("android.SipPrac.INCOMING_CALL");
            intentfilter.addAction("android.IncomingCall.CALL_ACCEPTED");
            intentfilter.addAction("android.yo.OUTGOING_CALL");
            intentfilter.addAction("com.yo.NewAccountSipRegistration");
            receiver = new Receiver();
            registerReceiver(receiver, intentfilter);
        }
    }

    /* (non-Javadoc)
     * @see android.app.Service#onTaskRemoved(android.content.Intent)
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        mLog.d("SERVICE TASK", "<<<< SERVICE TASK REMOVED >>>");
        super.onTaskRemoved(rootIntent);
    }
}

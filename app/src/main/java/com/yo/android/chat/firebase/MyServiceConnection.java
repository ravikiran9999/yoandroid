package com.yo.android.chat.firebase;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by rdoddapaneni on 7/19/2016.
 */
@Singleton
public class MyServiceConnection implements ServiceConnection {

    private boolean serviceConnection;
    private FirebaseService firebaseService;

    @Inject
    public MyServiceConnection() {
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        /*FirebaseService.MyBinder myBinder = (FirebaseService.MyBinder) service;
        firebaseService = myBinder.getService();*/

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

        setServiceConnection(false);
    }

    public boolean isServiceConnection() {
        return serviceConnection;
    }

    public void setServiceConnection(boolean serviceConnection) {
        this.serviceConnection = serviceConnection;
    }
}

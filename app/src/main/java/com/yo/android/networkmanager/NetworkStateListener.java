package com.yo.android.networkmanager;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.yo.services.BackgroundServices;

/**
 * Created by Rajesh Babu Polamarasetti on 11/6/15.
 */
public class NetworkStateListener extends BroadcastReceiver {

    public static final int NETWORK_CONNECTED = 1;
    public static final String YO_NETWORK_STATE_CONNECTED = "yo.action.network_state_connected";

    public static final int NO_NETWORK_CONNECTIVITY = 2;

    private static final List<NetworkStateChangeListener> LISTENERS = new CopyOnWriteArrayList<NetworkStateChangeListener>();

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle networkStateInfo = intent.getExtras();
        if (networkStateInfo != null) {
            NetworkInfo networkInfo = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED
                    && !LISTENERS.isEmpty()) {
                setNetworkState(NETWORK_CONNECTED);
                startContactSync(context);
            } else if (networkInfo != null && networkInfo.getState() == NetworkInfo.State.DISCONNECTED && !LISTENERS.isEmpty()) {
                setNetworkState(NO_NETWORK_CONNECTIVITY);
            }
        }


    }

    private void startContactSync(Context context) {
        Intent service = new Intent(context, BackgroundServices.class);
        service.setAction(BackgroundServices.SYNC_OFFLINE_CONTACTS);
        context.startService(service);
    }

    private void setNetworkState(int networkConnected) {
        for (NetworkStateChangeListener mlistener : LISTENERS) {
            if (mlistener != null) {
                mlistener.onNetworkStateChanged(networkConnected);
            }
        }

    }

    public static void registerNetworkState(NetworkStateChangeListener listener) {
        synchronized (LISTENERS) {
            if (!LISTENERS.contains(listener)) {
                LISTENERS.add(listener);
            }
        }
    }

    public static void unregisterNetworkState(NetworkStateChangeListener listener) {
        LISTENERS.remove(listener);

    }
}

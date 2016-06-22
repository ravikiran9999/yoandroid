package com.yo.android.notification;

import android.support.annotation.NonNull;


import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.orion.android.common.logger.Log;

import javax.inject.Inject;

/**
 * Created by rdoddapaneni on 6/22/2016.
 */

public class MyInstanceIDListenerService extends FirebaseInstanceIdService {
    private static final String TAG = "MyInstanceIDListener";
    private static final String FCM_TOPIC = "FCMTopic";

    @Inject
    protected Log mLog;

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        mLog.d(TAG, "Refreshed token: " + refreshedToken);
        // Subscribe to topic.
        FirebaseMessaging.getInstance().subscribeToTopic(FCM_TOPIC);

        sendRegistrationToServer(refreshedToken);

    }

    private void sendRegistrationToServer(@NonNull String refreshedToken) {

        if( refreshedToken != null) {
           // send fcm registration token to server
        }
    }
}

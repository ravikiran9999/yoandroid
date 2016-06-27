package com.yo.android.notification;

import android.support.annotation.NonNull;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.orion.android.common.logger.Log;
import com.yo.android.di.Injector;

import javax.inject.Inject;

/**
 * Created by rdoddapaneni on 6/22/2016.
 */

public class MyInstanceIDListenerService extends FirebaseInstanceIdService {
    private static final String TAG = "MyInstanceIDListenerService";

    @Inject
    protected Log mLog;

    /**
     * Constructor
     */
    public MyInstanceIDListenerService() {
        Injector.obtain(getApplication()).inject(this);
    }


    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        mLog.d(TAG, "onTokenRefresh: Refreshed token: %s", refreshedToken);
        sendRegistrationToServer(refreshedToken);

    }

    private void sendRegistrationToServer(@NonNull String refreshedToken) {

        if (refreshedToken != null) {
            // send fcm registration token to server
        }
    }
}

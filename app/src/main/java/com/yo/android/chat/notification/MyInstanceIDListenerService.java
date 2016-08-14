package com.yo.android.chat.notification;

import android.support.annotation.NonNull;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.orion.android.common.logger.Log;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.api.YoApi;
import com.yo.android.di.Injector;
import com.yo.android.util.Constants;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by rdoddapaneni on 6/22/2016.
 */

public class MyInstanceIDListenerService extends FirebaseInstanceIdService {
    private static final String TAG = "MyInstanceIDListenerService";

    @Inject
    protected Log mLog;
    @Inject
    @Named("login")
    PreferenceEndPoint preferenceEndPoint;
    @Inject
    YoApi.YoService yoService;

    @Override
    public void onCreate() {
        super.onCreate();
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
            preferenceEndPoint.saveStringPreference(Constants.FCM_REFRESH_TOKEN, refreshedToken);
        }
    }
}

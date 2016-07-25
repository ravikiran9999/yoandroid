package com.yo.android.chat.notification;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.orion.android.common.logger.Log;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.api.YoApi;
import com.yo.android.di.Injector;
import com.yo.android.ui.BottomTabsActivity;
import com.yo.android.util.Constants;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
            // send fcm registration token to server
            /*if(!TextUtils.isEmpty(preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER))){
                String accessToken = preferenceEndPoint.getStringPreference("access_token");
                yoService.updateDeviceTokenAPI(accessToken, refreshedToken).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });
            }*/
        }
    }
}

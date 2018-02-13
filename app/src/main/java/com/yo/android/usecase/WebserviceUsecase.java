package com.yo.android.usecase;

import android.util.Log;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.api.ApiCallback;
import com.yo.android.api.YoApi;
import com.yo.android.model.Lock;
import com.yo.android.util.Constants;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by rdoddapaneni on 7/6/2017.
 */

public class WebserviceUsecase {

    @Inject
    YoApi.YoService yoService;
    @Inject
    @Named("login")
    PreferenceEndPoint loginPrefs;

    public void appStatus(final ApiCallback<Lock> lockApiCallback) {
        String accessToken = loginPrefs.getStringPreference("access_token");
        Call<Lock> call = yoService.lockAPI(accessToken);
        call.enqueue(new Callback<Lock>() {
            @Override
            public void onResponse(Call<Lock> call, Response<Lock> response) {
                try {
                    if (response.body() != null) {
                        loginPrefs.saveBooleanPreference(Constants.MAGAZINE_LOCK, response.body().getData().isIsMagzineLocked());
                        loginPrefs.saveBooleanPreference(Constants.DIALER_LOCK, response.body().getData().isIsDailerLocked());
                        loginPrefs.saveBooleanPreference(Constants.APP_LOCK, response.body().getData().isIsAppLocked());
                        loginPrefs.saveBooleanPreference(Constants.RENEWAL, response.body().getData().isIsAutorenewalDone());
                        if (lockApiCallback != null) {
                            lockApiCallback.onResult(response.body());
                        }
                    }
                } finally {
                    if (response != null && response.body() != null) {
                        try {
                            response = null;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Lock> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}

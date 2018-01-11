package com.yo.android.usecase;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.api.ApiCallback;
import com.yo.android.api.YoApi;
import com.yo.android.model.Notification;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationUsecase {

    @Inject
    YoApi.YoService yoService;

    @Inject
    @Named("login")
    PreferenceEndPoint loginPrefs;

    public void getNotifications(String notificationItems, String type, final ApiCallback<List<Notification>> notificationCallback) {
        String accessToken = loginPrefs.getStringPreference("access_token");
        Call<List<Notification>> call = yoService.getNotifications(accessToken, type, notificationItems);
        call.enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                try {
                    notificationCallback.onResult(response.body());
                }finally {
                    if(response != null && response.body() != null) {
                        try {
                            response.body().clear();
                            response = null;
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Notification>> call, Throwable t) {

            }
        });
    }
}

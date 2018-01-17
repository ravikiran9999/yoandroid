package com.yo.android.usecase;

import android.util.Log;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.api.YoApi;
import com.yo.android.model.Alerts;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppLogglyUsecase {

    public static final String TAG = ChatNotificationUsecase.class.getSimpleName();
    @Inject
    YoApi.YoService yoService;
    @Inject
    @Named("login")
    PreferenceEndPoint loginPrefs;

    public void sendAlertsToLoggly(final String title, final String message, final String status, final int code) {
        String accessToken = loginPrefs.getStringPreference("access_token");
        Call<Alerts> call = yoService.sendAlerts(accessToken, title, message, status, code);
        call.enqueue(new Callback<Alerts>() {
            @Override
            public void onResponse(Call<Alerts> call, Response<Alerts> response) {
                try {
                    if (response.body() != null && response.code() == 200) {
                        // request success
                    } else {
                        errorMessage(response);
                    }
                } finally {
                    if(response != null && response.body() != null) {
                        try {
                            response = null;
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Alerts> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void errorMessage(Response<Alerts> response) {
        try {
            JSONObject jsonObjectError = new JSONObject(response.errorBody().string());
            int code = jsonObjectError.getInt("code");
            if (code == 400) {
                Log.e(TAG, "LogglyAlertErrorMessage : " + jsonObjectError.getString("data"));
            } else if(code == 500) {
                Log.e(TAG, "LogglyAlertErrorMessage : " + jsonObjectError.getString("data"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

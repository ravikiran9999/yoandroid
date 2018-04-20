package com.yo.android.usecase;

import android.util.Log;
import com.google.gson.JsonElement;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.api.ApiCallback;
import com.yo.android.api.YoApi;
import org.json.JSONObject;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Administrator on 4/9/2018.
 */

public class ContactsSyncWithNameUsecase {

    private static final String TAG = ContactsSyncWithNameUsecase.class.getSimpleName();

    @Inject
    YoApi.YoService yoService;
    @Inject
    @Named("login")
    PreferenceEndPoint loginPrefs;


    public void contactsSyncWithName(List<JSONObject> nameAndNumber, final ApiCallback<JsonElement> apiCallback) {
        String accessToken = loginPrefs.getStringPreference(YoApi.ACCESS_TOKEN);
        yoService.syncContactsWithNameAPI(accessToken, nameAndNumber).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                apiCallback.onResult(response.body());
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                try {
                    Log.d(TAG, " " + t.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //if its failed to add try later to add failed contacts.
            }
        });
    }
}

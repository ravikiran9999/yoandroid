package com.yo.android.usecase;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.api.ApiCallback;
import com.yo.android.api.YoApi;
import com.yo.android.model.Articles;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Administrator on 5/4/2018.
 */

public class StoryUsecase {
    @Inject
    protected YoApi.YoService yoService;
    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;

    public void magazineStory(String url, String magazineTitle, String magazineDesc, String magazinePrivacy, String magazineId, String tag, final ApiCallback<Articles> callback) {
        String accessToken = preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
        Call<Articles> call = yoService.postStoryMagazineAPI(accessToken, url, magazineTitle, magazineDesc, magazinePrivacy, magazineId, tag);
        call.enqueue(new Callback<Articles>() {
            @Override
            public void onResponse(Call<Articles> call, Response<Articles> response) {
                if(response != null && response.body() != null) {
                    callback.onResult(response.body());
                } else {
                    callback.onFailure("");
                }
            }

            @Override
            public void onFailure(Call<Articles> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

}

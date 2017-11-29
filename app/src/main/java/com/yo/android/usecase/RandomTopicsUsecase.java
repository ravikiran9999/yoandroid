package com.yo.android.usecase;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.api.ApiCallback;
import com.yo.android.api.YoApi;
import com.yo.android.model.Categories;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RandomTopicsUsecase {

    public static final String TAG = AddTopicsUsecase.class.getSimpleName();
    @Inject
    YoApi.YoService yoService;

    @Inject
    @Named("login")
    PreferenceEndPoint loginPrefs;

    public void getRandomTopics(final ApiCallback<List<Categories>> randomCategories) {
        String accessToken = loginPrefs.getStringPreference("access_token");
        yoService.randomTagsAPI(accessToken).enqueue(new Callback<List<Categories>>() {
            @Override
            public void onResponse(Call<List<Categories>> call, Response<List<Categories>> response) {
                //dismissProgressDialog();
                if (response == null || response.body() == null) {
                    return;
                }
                randomCategories.onResult(response.body());
                //categoriesList.clear();
                //categoriesList.addAll(response.body());
            }

            @Override
            public void onFailure(Call<List<Categories>> call, Throwable t) {

            }
        });
    }
}

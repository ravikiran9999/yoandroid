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
    private static final String NO_TOPICS_AVAILABLE = "No topics are available for selection";

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
                try {
                    if (response == null || response.body() == null) {
                        return;
                    }
                    if (response.body().size() > 0) {
                        randomCategories.onResult(response.body());
                    } else {
                        randomCategories.onFailure(NO_TOPICS_AVAILABLE);
                    }
                } catch (Exception e) {

                } finally {
                    if (response != null && response.raw() != null) {
                        try {
                            response.body().clear();
                            response = null;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Categories>> call, Throwable t) {
                randomCategories.onFailure(NO_TOPICS_AVAILABLE);
            }
        });
    }
}

package com.yo.android.usecase;

import android.util.Log;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.api.ApiCallback;
import com.yo.android.api.YoApi;
import com.yo.android.model.Categories;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTopicsUsecase {

    public static final String TAG = AddTopicsUsecase.class.getSimpleName();
    @Inject
    YoApi.YoService yoService;

    @Inject
    @Named("login")
    PreferenceEndPoint loginPrefs;

    @Inject
    RandomTopicsUsecase randomTopicsUsecase;

    public void addTopics(List<String> followedTopicsIdsList, final ApiCallback<List<Categories>> categoriesCallback) {
        String accessToken = loginPrefs.getStringPreference("access_token");
        Call<ResponseBody> call = yoService.addTopicsAPI(accessToken, followedTopicsIdsList);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.body() != null && response.code() == 200) {
                    Log.i(TAG, "added topics" + response.body());
                    randomTopicsUsecase.getRandomTopics(new ApiCallback<List<Categories>>() {

                        @Override
                        public void onResult(List<Categories> result) {
                            categoriesCallback.onResult(result);
                        }

                        @Override
                        public void onFailure(String message) {

                        }
                    });
                } else {
                    errorMessage(response);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void errorMessage(Response<ResponseBody> response) {
        try {
            JSONObject jsonObjectError = new JSONObject(response.errorBody().string());
            int code = jsonObjectError.getInt("code");
            if (code == 400) {
                Log.e(TAG, "chatErrorMessage : " + jsonObjectError.getString("data"));
            } else if (code == 500) {
                Log.e(TAG, "chatErrorMessage : " + jsonObjectError.getString("data"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

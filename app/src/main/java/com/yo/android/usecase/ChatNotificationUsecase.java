package com.yo.android.usecase;

import android.util.Log;

import com.google.gson.Gson;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.api.ApiCallback;
import com.yo.android.api.YoApi;
import com.yo.android.model.ChatMessage;
import com.yo.android.model.ChatNotificationResponse;
import com.yo.android.model.PackageDenomination;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by rdoddapaneni on 11/14/2017.
 */

public class ChatNotificationUsecase {

    public static final String TAG = ChatNotificationUsecase.class.getSimpleName();
    @Inject
    YoApi.YoService yoService;
    @Inject
    @Named("login")
    PreferenceEndPoint loginPrefs;

    public void pushChatMessage(final ChatMessage chatMessage) {
        String mChatMessage = new Gson().toJson(chatMessage);
        String accessToken = loginPrefs.getStringPreference(YoApi.ACCESS_TOKEN);
        Call<ChatNotificationResponse> call = yoService.chatNotificationApi(accessToken, mChatMessage);
        call.enqueue(new Callback<ChatNotificationResponse>() {
            @Override
            public void onResponse(Call<ChatNotificationResponse> call, Response<ChatNotificationResponse> response) {
                if (response.body() != null && response.code() == 200) {
                    Log.i(TAG, "chat push successful" + response.body());
                } else {
                    errorMessage(response);
                }
            }

            @Override
            public void onFailure(Call<ChatNotificationResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void errorMessage(Response<ChatNotificationResponse> response) {
        try {
            JSONObject jsonObjectError = new JSONObject(response.errorBody().string());
            int code = jsonObjectError.getInt("code");
            if (code == 400) {
              Log.e(TAG, "chatErrorMessage : " + jsonObjectError.getString("data"));
            } else if(code == 500) {
                Log.e(TAG, "chatErrorMessage : " + jsonObjectError.getString("data"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

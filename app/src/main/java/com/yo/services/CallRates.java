package com.yo.services;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.api.YoApi;
import com.yo.android.model.dialer.CallRateDetail;
import com.yo.android.util.Constants;

import java.io.InputStreamReader;
import java.util.List;

import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Rajesh on 11/7/17.
 */

public class CallRates {
    private static final String TAG = CallRates.class.getSimpleName();

    public static void fetch(final YoApi.YoService yoService, final PreferenceEndPoint preferenceEndPoint) {
        yoService.getCallsRatesListAPI(preferenceEndPoint.getStringPreference("access_token")).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (BackgroundServices.ENABLE_LOGS) {
                    Log.w(TAG, "Received call rates response ");
                }
                try {
                    List<CallRateDetail> callRateDetailList = new Gson().fromJson(new InputStreamReader(response.body().byteStream()), new TypeToken<List<CallRateDetail>>() {
                    }.getType());
                    if (callRateDetailList != null && !callRateDetailList.isEmpty()) {
                        String json = new Gson().toJson(callRateDetailList);
                        preferenceEndPoint.saveStringPreference(Constants.COUNTRY_LIST, json);
                        EventBus.getDefault().post(Constants.CALL_RATE_DETAILS_ACTION);
                    }
                } catch (Exception e) {
                    if (BackgroundServices.ENABLE_LOGS) {
                        Log.w(TAG, e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> response, Throwable t) {
                if (BackgroundServices.ENABLE_LOGS && t != null) {
                    Log.w(TAG, "Failed to get call rates " + t.getMessage());
                }
            }
        });
    }
}

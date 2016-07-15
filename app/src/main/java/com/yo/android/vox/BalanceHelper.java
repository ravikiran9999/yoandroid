package com.yo.android.vox;

import android.text.TextUtils;

import com.orion.android.common.logger.Log;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ramesh on 9/7/16.
 */
@Singleton
public class BalanceHelper {
    private static final String TAG = "BalanceHelper";
    VoxFactory voxFactory;
    VoxApi.VoxService voxService;
    PreferenceEndPoint prefs;
    Log mLog;

    @Inject
    public BalanceHelper(Log log, VoxFactory voxFactory, VoxApi.VoxService voxService, @Named("login") PreferenceEndPoint preferenceEndPoint) {
        this.mLog = log;
        this.voxFactory = voxFactory;
        this.voxService = voxService;
        this.prefs = preferenceEndPoint;
    }


    public void checkBalance() {
        if (TextUtils.isEmpty(prefs.getStringPreference(Constants.SUBSCRIBER_ID))) {
            loadSubscriberId();
        } else {
            loadBalance();
        }

    }

    private void loadSubscriberId() {
        voxService.executeAction(voxFactory.getSubscriberIdBody(prefs.getStringPreference(Constants.PHONE_NUMBER))).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String str = Util.toString(response.body().byteStream());
                    JSONObject jsonObject = new JSONObject(str);
                    String subscriberId = jsonObject.getJSONObject("DATA").getString("SUBSCRIBERID");
                    prefs.saveStringPreference(Constants.SUBSCRIBER_ID, subscriberId);
                    loadBalance();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }


    private void loadBalance() {
        voxService.executeAction(voxFactory.getBalanceBody(prefs.getStringPreference(Constants.SUBSCRIBER_ID))).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String str = Util.toString(response.body().byteStream());
                        JSONObject jsonObject = new JSONObject(str);
                        String balance = jsonObject.getJSONObject("DATA").getString("CREDIT");
                        prefs.saveStringPreference(Constants.CURRENT_BALANCE, balance);
                        mLog.i(TAG, "loadBalance: balance -  %s", balance);
                    } catch (IOException e) {
                        mLog.w(TAG, "loadBalance", e);
                    } catch (JSONException e) {
                        mLog.w(TAG, "loadBalance", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }
}

package com.yo.android.chat.firebase;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import com.orion.android.common.logger.Log;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.api.YoApi;
import com.yo.android.di.InjectedService;
import com.yo.android.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FirebaseService extends InjectedService {

    private static String LOG_TAG = "BoundService";
    private IBinder mBinder = new MyBinder();

    @Inject
    @Named("login")
    PreferenceEndPoint loginPrefs;
    @Inject
    YoApi.YoService yoService;

    public FirebaseService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getFirebaseAuth();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    public void getFirebaseAuth() {
        String access = loginPrefs.getStringPreference(YoApi.ACCESS_TOKEN);
        yoService.firebaseAuthToken(access).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response.body().string());
                    String name = jsonObject.getString("firebase_token");
                    loginPrefs.saveStringPreference(Constants.FIREBASE_TOKEN, name);

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }

    public class MyBinder extends Binder {

        public FirebaseService getService() {
            return FirebaseService.this;
        }
    }
}

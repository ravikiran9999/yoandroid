package com.yo.android.chat.firebase;

import android.content.Context;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.api.YoApi;
import com.yo.android.di.Injector;
import com.yo.android.util.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by rajesh on 13/9/16.
 */
public class FireBaseAuthToken {

    @Inject
    @Named("login")
    PreferenceEndPoint loginPrefs;

    @Inject
    YoApi.YoService yoService;

    private static FireBaseAuthToken fireBaseAuthToken;

    public interface FireBaseAuthListener {
        public void onSuccess();
        public void onFailed();
    }

    public static FireBaseAuthToken getInstance(Context context) {
        if (fireBaseAuthToken == null) {
            Injector.obtain(context).inject(context);
            fireBaseAuthToken = new FireBaseAuthToken();
        }
        return fireBaseAuthToken;
    }

    public void getFirebaseAuth(final FireBaseAuthListener listener) {
        String firebackToken = loginPrefs.getStringPreference(Constants.FIREBASE_TOKEN);
        if (firebackToken == null) {
            String access = loginPrefs.getStringPreference(YoApi.ACCESS_TOKEN);
            yoService.firebaseAuthToken(access).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    JSONObject jsonObject = null;
                    try {
                        if (response.body() != null) {
                            jsonObject = new JSONObject(response.body().string());
                            String firebaseToken = jsonObject.getString("firebase_token");
                            loginPrefs.saveStringPreference(Constants.FIREBASE_TOKEN, firebaseToken);
                            listener.onSuccess();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        listener.onFailed();
                    } catch (IOException e) {
                        e.printStackTrace();
                        listener.onFailed();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    listener.onFailed();
                }
            });
        }
    }
}

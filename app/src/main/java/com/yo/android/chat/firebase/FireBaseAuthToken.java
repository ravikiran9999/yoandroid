package com.yo.android.chat.firebase;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

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

    private static boolean waitingForReply = false;

    private static FireBaseAuthToken fireBaseAuthToken;

    public interface FireBaseAuthListener {
        public void onSuccess();

        public void onFailed();

    }

    public FireBaseAuthToken(Context context) {
        Injector.obtain(context.getApplicationContext()).inject(this);
    }

    public static FireBaseAuthToken getInstance(Context context) {
        if (fireBaseAuthToken == null) {
            fireBaseAuthToken = new FireBaseAuthToken(context);
        }
        return fireBaseAuthToken;
    }

    public void getFirebaseAuth(final FireBaseAuthListener listener) {
        //if(!loginPrefs.getStringPreference(Constants.FIREBASE_TOKEN).isEmpty()) {
        String firebackToken = loginPrefs.getStringPreference(Constants.FIREBASE_TOKEN);
        Log.i("FireBaseAuthToken", "FireBaseAuthToken : " + firebackToken);
        if (TextUtils.isEmpty(firebackToken) & !waitingForReply) {
            waitingForReply = true;
            String access = loginPrefs.getStringPreference(YoApi.ACCESS_TOKEN);
            Log.i("FireBaseAuthToken", "YoApiACCESS_TOKEN : " + access);
            yoService.firebaseAuthToken(access).enqueue(new Callback<ResponseBody>() {
                public final String TAG = FireBaseAuthToken.class.getSimpleName();

                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    JSONObject jsonObject = null;

                    try {
                        if (response.body() != null) {
                            jsonObject = new JSONObject(response.body().string());
                            String firebaseToken = jsonObject.getString("firebase_token");
                            Log.i(TAG, "New token generated " + firebaseToken);
                            loginPrefs.saveStringPreference(Constants.FIREBASE_TOKEN, firebaseToken);
                            listener.onSuccess();
                            waitingForReply = false;
                            Log.i(TAG, "Login Succeeded! " + firebaseToken);

                        }
                    } catch (JSONException e) {
                        Log.i(TAG, "Login Failed!");
                        e.printStackTrace();
                        waitingForReply = false;
                        listener.onFailed();
                    } catch (IOException e) {
                        Log.i(TAG, "Login Failed!");
                        e.printStackTrace();
                        waitingForReply = false;
                        listener.onFailed();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.i(TAG, "Login Failed!");
                    waitingForReply = false;
                    listener.onFailed();
                }
            });
        } else {
            listener.onSuccess();
        }
    }
}

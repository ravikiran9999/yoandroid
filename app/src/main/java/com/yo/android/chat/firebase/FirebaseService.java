package com.yo.android.chat.firebase;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.api.YoApi;
import com.yo.android.di.InjectedService;
import com.yo.android.model.ChatMessage;
import com.yo.android.util.Constants;
import com.yo.android.util.FireBaseHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FirebaseService extends InjectedService {

    private static String LOG_TAG = "BoundService";
    private IBinder mBinder = new MyBinder();
    private Firebase authReference;
    private ArrayList<ChatMessage> chatMessageArray;

    @Inject
    @Named("login")
    PreferenceEndPoint loginPrefs;
    @Inject
    YoApi.YoService yoService;

    @Inject
    FireBaseHelper fireBaseHelper;

    public FirebaseService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getFirebaseAuth();
        chatMessageArray = new ArrayList<>();

        authReference = fireBaseHelper.authWithCustomToken(loginPrefs.getStringPreference(Constants.FIREBASE_TOKEN));
        authReference.keepSynced(true);
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
                    if (response.body() != null) {
                        jsonObject = new JSONObject(response.body().string());
                        String name = jsonObject.getString("firebase_token");
                        loginPrefs.saveStringPreference(Constants.FIREBASE_TOKEN, name);
                    }
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

    public ArrayList<ChatMessage> getChatMessageList() {
        try {
            authReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    try {

                        ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                        chatMessageArray.add(chatMessage);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }

            });
        } catch (Exception e) {
        }
        return chatMessageArray;
    }

    public class MyBinder extends Binder {

        public FirebaseService getService() {
            return FirebaseService.this;
        }
    }

}

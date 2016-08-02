package com.yo.android.chat.firebase;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.annotations.NotNull;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.BuildConfig;
import com.yo.android.adapters.UserChatAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.chat.ui.fragments.UserChatFragment;
import com.yo.android.di.InjectedService;
import com.yo.android.model.ChatMessage;
import com.yo.android.util.Constants;
import com.yo.android.util.FireBaseHelper;

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

    private ChildEventListener childEventListener;

    public FirebaseService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        getFirebaseAuth();


        //getChatMessageList(null);

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

    public void getChatMessageList(String childRoom, @NonNull final UserChatAdapter userChatAdapter) {
        try {
            chatMessageArray = new ArrayList<>();
            //authReference = fireBaseHelper.authWithCustomToken(loginPrefs.getStringPreference(Constants.FIREBASE_TOKEN));
            authReference = new Firebase(BuildConfig.FIREBASE_URL);

            if (childRoom != null) {
                Firebase roomRefer = authReference.child(childRoom).child(Constants.CHATS);
                childEventListener = new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        try {

                            //if(( instanceof UserChatFragment) {

                            //getActivityManager();

                            ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                            chatMessageArray.add(chatMessage);
                            userChatAdapter.addItems(chatMessageArray);

                            //listView.smoothScrollToPosition(userChatAdapter.getCount());

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
                        firebaseError.getMessage();
                    }
                };
                roomRefer.addChildEventListener(childEventListener);
                //roomRefer.keepSynced(true);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class MyBinder extends Binder {

        public FirebaseService getService() {
            return FirebaseService.this;
        }
    }


}

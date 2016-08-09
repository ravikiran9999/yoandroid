package com.yo.android.chat.firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.adapters.UserChatAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.di.InjectedService;
import com.yo.android.model.ChatMessage;
import com.yo.android.model.Room;
import com.yo.android.ui.BaseActivity;
import com.yo.android.util.Constants;
import com.yo.android.util.FireBaseHelper;
import com.yo.android.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.ContentValues.TAG;

public class FirebaseService extends InjectedService implements ValueEventListener {

    private static String LOG_TAG = "BoundService";
    private IBinder mBinder = new MyBinder();
    private Firebase authReference;
    private Firebase roomReference;
    private Firebase allRoomsReference;

    @Inject
    @Named("login")
    PreferenceEndPoint loginPrefs;
    @Inject
    YoApi.YoService yoService;

    @Inject
    FireBaseHelper fireBaseHelper;

    private ChildEventListener childEventListener;
    private ValueEventListener valueEventListener;

    private Context context;

    private boolean isRunning = false;


    @Override
    public void onCreate() {
        super.onCreate();

        authReference = new Firebase(BuildConfig.FIREBASE_URL);
        //allRoomsReference = authReference.child()
        isRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (isRunning) {
            Log.i(TAG, "Service running");
            getFirebaseAuth();
            //getChatMessageList();

        }

        return START_STICKY;
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
                        getAllRooms();
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

    private void getAllRooms() {

        String access = loginPrefs.getStringPreference(YoApi.ACCESS_TOKEN);
        yoService.getAllRoomsAPI(access).enqueue(new Callback<List<Room>>() {
            @Override
            public void onResponse(Call<List<Room>> call, Response<List<Room>> response) {
                if (response.body() != null) {
                    for (int i = 0; i < response.body().size(); i++) {
                        final Room room = response.body().get(i);
                        getChatMessageList(room.getFirebaseRoomId());
                    }
                }

            }

            @Override
            public void onFailure(Call<List<Room>> call, Throwable t) {
            }
        });

    }


    public void getChatMessageList(String roomId) {
        try {

            roomReference = authReference.child(roomId).child(Constants.CHATS);

            childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    try {

                        ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                        if (chatMessage.getDelivered() == 0) {
                            postNotif(chatMessage.getRoomId(), chatMessage);
                        }

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
            roomReference.limitToLast(1).addChildEventListener(childEventListener);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class MyBinder extends Binder {

        public FirebaseService getService() {
            return FirebaseService.this;
        }
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {
        firebaseError.getMessage();
    }

    private void postNotif(String roomId, ChatMessage chatMessage) {
        try {

            String body = chatMessage.getMessage();
            String title = chatMessage.getSenderID();
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            int notificationId = chatMessage.getMessage().hashCode();

            NotificationCompat.BigTextStyle notificationStyle = new NotificationCompat.BigTextStyle();
            notificationStyle.bigText(body);
            Intent notificationIntent = new Intent(this, ChatActivity.class);
            notificationIntent.putExtra(Constants.CHAT_ROOM_ID, roomId);
            notificationIntent.putExtra(Constants.OPPONENT_PHONE_NUMBER, title);
            notificationIntent.putExtra(Constants.TYPE, Constants.YO_NOTIFICATION);

            PendingIntent contentIntent = PendingIntent.getActivity(this, notificationId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            android.app.Notification notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_yo_notification)
                    .setContentTitle(title == null ? "Yo App" : title)
                    .setContentText(body)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .setStyle(notificationStyle)
                    .build();

            mNotificationManager.notify(notificationId, notification);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "FirebaseService killed", Toast.LENGTH_LONG).show();
    }


}

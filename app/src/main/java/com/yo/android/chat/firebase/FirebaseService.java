package com.yo.android.chat.firebase;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.di.InjectedService;
import com.yo.android.model.ChatMessage;
import com.yo.android.util.Constants;
import com.yo.android.util.FireBaseHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.ContentValues.TAG;

public class FirebaseService extends InjectedService {

    private static String TAG = "BoundService";
    private IBinder mBinder = new MyBinder();
    private Firebase authReference;
    private Firebase roomReference;
    private int messageCount;
    private Context context;


    @Inject
    FireBaseHelper fireBaseHelper;

    private boolean isRunning = false;

    @Inject
    @Named("login")
    PreferenceEndPoint loginPrefs;


    @Override
    public void onCreate() {
        super.onCreate();

        context = this;
        authReference = new Firebase(BuildConfig.FIREBASE_URL);
        //roomReference = authReference.child()
        isRunning = true;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (isRunning) {
            Log.i(TAG, "Service running");
            FireBaseAuthToken.getInstance(this).getFirebaseAuth(new FireBaseAuthToken.FireBaseAuthListener() {
                @Override
                public void onSuccess() {
                    getAllRooms();
                }

                @Override
                public void onFailed() {

                }
            });
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


    private void getAllRooms() {
        authReference = fireBaseHelper.authWithCustomToken(this,loginPrefs.getStringPreference(Constants.FIREBASE_TOKEN));

        ChildEventListener mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                getChatMessageList(dataSnapshot.getKey());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //getChatMessageList(dataSnapshot.getKey());
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
        };
        String firebaseUserId = loginPrefs.getStringPreference(Constants.FIREBASE_USER_ID);
        if (!firebaseUserId.isEmpty()) {
            authReference.child(Constants.USERS).child(firebaseUserId).child(Constants.MY_ROOMS).addChildEventListener(mChildEventListener);
        }
    }

    public void getChatMessageList(String roomId) {
        try {
            roomReference = authReference.child(Constants.ROOMS).child(roomId).child(Constants.CHATS);

            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    try {

                        ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                        String userId = loginPrefs.getStringPreference(Constants.PHONE_NUMBER);

                        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
                        String[] strings = cn.getShortClassName().split(Pattern.quote("."));
                        int i = strings.length - 1;

                        if (!userId.equalsIgnoreCase(chatMessage.getSenderID()) && chatMessage.getDelivered() == 0 && loginPrefs.getBooleanPreference(Constants.NOTIFICATION_ALERTS) && (!strings[i].equalsIgnoreCase("ChatActivity"))) {
                            postNotification(chatMessage.getRoomId(), chatMessage);
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

    private void postNotification(String roomId, ChatMessage chatMessage) {
        try {

            String body = chatMessage.getMessage();
            String title = chatMessage.getSenderID();
            String voxUsername = chatMessage.getVoxUserName();


            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            long num = Long.parseLong(title);
            int notificationId = (int) num;


            NotificationCompat.BigTextStyle notificationStyle = new NotificationCompat.BigTextStyle();
            notificationStyle.bigText(body);
            Intent notificationIntent = new Intent(this, ChatActivity.class);
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            notificationIntent.putExtra(Constants.CHAT_ROOM_ID, roomId);
            notificationIntent.putExtra(Constants.OPPONENT_PHONE_NUMBER, title);
            notificationIntent.putExtra(Constants.VOX_USER_NAME, voxUsername);
            notificationIntent.putExtra(Constants.TYPE, Constants.YO_NOTIFICATION);
            notificationIntent.putExtra(Constants.OPPONENT_ID, chatMessage.getYouserId());

            PendingIntent contentIntent = PendingIntent.getActivity(this, notificationId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            android.app.Notification notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_yo_notification)
                    .setContentTitle(title == null ? "Yo App" : title)

                    .setContentText(body)
                    .setNumber(++messageCount)
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

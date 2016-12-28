package com.yo.android.chat.firebase;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import com.firebase.client.Query;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.chat.ImageLoader;
import com.yo.android.chat.notification.localnotificationsbuilder.GeneratePictureStyleNotification;
import com.yo.android.chat.notification.localnotificationsbuilder.Notifications;
import com.yo.android.chat.notification.pojo.NotificationAction;
import com.yo.android.chat.notification.pojo.NotificationBuilderObject;
import com.yo.android.chat.notification.pojo.UserData;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.di.InjectedService;
import com.yo.android.model.ChatMessage;
import com.yo.android.model.NotificationCountReset;
import com.yo.android.util.Constants;
import com.yo.android.util.FireBaseHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

import de.greenrobot.event.EventBus;

public class FirebaseService extends InjectedService {

    private static String TAG = "BoundService";
    private Firebase authReference;
    private Firebase roomReference;
    private ChildEventListener mChildEventListener;
    private ChildEventListener childEventListener;
    private Query chatMessageQuery;
    private int messageCount;
    private Context context;
    private List<UserData> notificationList = new ArrayList<>();
    private static final int SIX = 6;

    private static final int STYLE_TEXT = 1;
    private static final int STYLE_INBOX = 2;
    private static final int STYLE_PICTURE = 3;
    private static final int STYLE_TEXT_WITH_ACTION = 4;


    @Inject
    FireBaseHelper fireBaseHelper;

    private boolean isRunning = false;
    private int initRoomCount;
    private int count;

    @Inject
    @Named("login")
    PreferenceEndPoint loginPrefs;


    @Override
    public void onCreate() {
        super.onCreate();

        context = this;
        authReference = new Firebase(BuildConfig.FIREBASE_URL);
        isRunning = true;
        EventBus.getDefault().register(this);
        count = 0;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //if (isRunning && count == 0) {
        if (isRunning ) {
            //count = 1;
            Log.i(TAG, "Service running");
            FireBaseAuthToken.getInstance(this).getFirebaseAuth(new FireBaseAuthToken.FireBaseAuthListener() {
                @Override
                public void onSuccess() {
                    getAllRooms();
                }

                @Override
                public void onFailed() {
                    Log.i(TAG, "Failed FirebaseAuthToken");
                    Toast.makeText(context, "Failed FirebaseAuthToken", Toast.LENGTH_SHORT).show();
                }
            });
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void getAllRooms() {
        authReference = fireBaseHelper.authWithCustomToken(this, loginPrefs.getStringPreference(Constants.FIREBASE_TOKEN));

        mChildEventListener = new ChildEventListener() {
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
                //Toast.makeText(getApplicationContext(), firebaseError.getMessage(), Toast.LENGTH_LONG).show();
                initRoomCount = 0;
            }
        };
        String firebaseUserId = loginPrefs.getStringPreference(Constants.FIREBASE_USER_ID);
        if (!firebaseUserId.isEmpty()) {
            roomReference = authReference.child(Constants.USERS).child(firebaseUserId).child(Constants.MY_ROOMS);
            registerRoomListener();
        }
    }


    public void getChatMessageList(String roomId) {
        try {
            authReference = fireBaseHelper.authWithCustomToken(context, loginPrefs.getStringPreference(Constants.FIREBASE_TOKEN));
            Firebase chatRoomReference = authReference.child(Constants.ROOMS).child(roomId).child(Constants.CHATS);

            childEventListener = new ChildEventListener() {
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
                            newPushNotification(chatMessage.getRoomId(), chatMessage);
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
            //chatMessageQuery = chatRoomReference.limitToLast(100);
            chatMessageQuery = chatRoomReference;
            registerChatMessageListener();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void newPushNotification(final String roomId, final ChatMessage chatMessage) {
        if (notificationList != null && notificationList.size() == 0 && chatMessage.getImagePath() != null) {
            ImageLoader.updateImage(context, chatMessage, new ImageLoader.ImageDownloadListener() {

                @Override
                public void onDownlaoded(File file) {
                    if (file != null) {
                        chatMessage.setImagePath(file.getAbsolutePath());
                        sendTrayNotifications(STYLE_PICTURE, roomId, chatMessage);
                    }
                }
            });
        } else {
            sendTrayNotifications(STYLE_INBOX, roomId, chatMessage);
        }
    }

    private int getNotificationIcon() {
        boolean useWhiteIcon = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
        return useWhiteIcon ? R.drawable.ic_yo_notification_white : R.drawable.ic_notification;
    }

    private void sendTrayNotifications(int mode, String roomId, ChatMessage chatMessage) {
        Notifications notification = new Notifications();
        String title = chatMessage.getSenderID();
        String voxUsername = chatMessage.getVoxUserName();

        Intent notificationIntent = new Intent(this, ChatActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.putExtra(Constants.CHAT_ROOM_ID, roomId);
        notificationIntent.putExtra(Constants.VOX_USER_NAME, voxUsername);
        notificationIntent.putExtra(Constants.TYPE, Constants.YO_NOTIFICATION);
        notificationIntent.putExtra(Constants.OPPONENT_ID, chatMessage.getYouserId());
        if (!TextUtils.isEmpty(chatMessage.getRoomName())) {
            notificationIntent.putExtra(Constants.OPPONENT_PHONE_NUMBER, title);
        }
        switch (mode) {
            case STYLE_TEXT:
                NotificationBuilderObject notificationTextData = prepareNotificationData(chatMessage);
                notification.buildBigTextStyleNotifications(this, notificationTextData, notificationIntent);
                break;
            case STYLE_INBOX:
                NotificationBuilderObject notificationsInboxData = prepareNotificationData(chatMessage);
                UserData data = new UserData();
                data.setMessageId(chatMessage.getMsgID());
                if (chatMessage.getType().equalsIgnoreCase(Constants.TEXT)) {
                    data.setDescription(chatMessage.getMessage());
                } else if (chatMessage.getType().equalsIgnoreCase(Constants.IMAGE)) {
                    data.setDescription(Constants.PHOTO);
                }
                data.setSenderName(chatMessage.getSenderID());

                if (!notificationList.contains(data)) {
                    notificationList.add(0, data);//always insert new notification on top
                }

                notification.buildInboxStyleNotifications(this, notificationIntent, notificationsInboxData, notificationList, SIX, false, true);
                break;
            case STYLE_PICTURE:
                NotificationBuilderObject notificationPictureInfo = prepareNotificationData(chatMessage);
                UserData pictureData = new UserData();
                pictureData.setMessageId(chatMessage.getMsgID());
                pictureData.setDescription(Constants.PHOTO);
                pictureData.setSenderName(chatMessage.getSenderID());
                if (!notificationList.contains(pictureData)) {
                    notificationList.add(0, pictureData);//always insert new notification on top
                }
                new GeneratePictureStyleNotification(this, notificationIntent, notificationPictureInfo, notificationList).execute();
                break;
            case STYLE_TEXT_WITH_ACTION:
                NotificationBuilderObject notificationTextActionData = prepareNotificationData(chatMessage);
                addingActionToNotification(notificationTextActionData);
                notification.buildBigTextStyleNotifications(this, notificationTextActionData, notificationIntent);
                break;
            default:
                break;
        }
    }

    @NonNull
    private NotificationBuilderObject prepareNotificationData(ChatMessage chatMessage) {
        NotificationBuilderObject notificationData = new NotificationBuilderObject();
        notificationData.setNotificationTitle(chatMessage.getSenderID());
        notificationData.setNotificationSmallIcon(getNotificationIcon());
        if (chatMessage.getType().equalsIgnoreCase(Constants.IMAGE)) {
            notificationData.setNotificationText(Constants.PHOTO);
        } else {
            notificationData.setNotificationText(chatMessage.getMessage());
        }
        notificationData.setNotificationLargeIconDrawable(R.mipmap.ic_launcher);
        notificationData.setNotificationInfo("3");
        notificationData.setNotificationLargeiconUrl(chatMessage.getImagePath());
        notificationData.setNotificationLargeText("Hello Every one ....Welcome to Notifications Demo..we are very glade to meet you here.Android Developers ");
        return notificationData;
    }

    private void addingActionToNotification(NotificationBuilderObject notificationData) {
        List<NotificationAction> actions = new ArrayList<NotificationAction>();
        NotificationAction action = new NotificationAction();
        action.setActionIcon(android.R.drawable.sym_action_call);
        action.setActionTitle("Yes");
        action.setActionType(com.yo.android.chat.notification.helper.Constants.YES_ACTION);
        NotificationAction actionNo = new NotificationAction();
        actionNo.setActionIcon(android.R.drawable.sym_action_email);
        actionNo.setActionTitle("No");
        actionNo.setActionType(com.yo.android.chat.notification.helper.Constants.NO_ACTION);
        NotificationAction actionMaybe = new NotificationAction();
        actionMaybe.setActionIcon(android.R.drawable.sym_action_chat);
        actionMaybe.setActionTitle("May Be");
        actionMaybe.setActionType(com.yo.android.chat.notification.helper.Constants.MAYBE_ACTION);
        actions.add(action);
        actions.add(actionNo);
        actions.add(actionMaybe);
        notificationData.setActions(actions);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        Toast.makeText(context, "FirebaseService killed", Toast.LENGTH_LONG).show();
    }

    public void onEventMainThread(NotificationCountReset count) {
        if (notificationList != null && count.getCount() == 0) {
            notificationList.clear();
            initRoomCount = 0;
            //init();
        }
    }

    private void registerRoomListener() {
        if (roomReference != null) {
            roomReference.addChildEventListener(mChildEventListener);
        }
    }

    private void registerChatMessageListener() {
        if (chatMessageQuery != null) {
            chatMessageQuery.addChildEventListener(childEventListener);
        }
    }
}

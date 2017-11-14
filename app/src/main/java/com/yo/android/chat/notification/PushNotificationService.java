package com.yo.android.chat.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.orion.android.common.logger.Log;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.chat.ChatRefreshBackground;
import com.yo.android.chat.ImageLoader;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.chat.notification.helper.NotificationCache;
import com.yo.android.chat.notification.localnotificationsbuilder.GeneratePictureStyleNotification;
import com.yo.android.chat.notification.localnotificationsbuilder.Notifications;
import com.yo.android.chat.notification.pojo.NotificationAction;
import com.yo.android.chat.notification.pojo.NotificationBuilderObject;
import com.yo.android.chat.notification.pojo.UserData;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.di.Injector;
import com.yo.android.helpers.PopupHelper;
import com.yo.android.model.ChatMessage;
import com.yo.android.model.NotificationCount;
import com.yo.android.ui.BottomTabsActivity;
import com.yo.android.ui.NotificationsActivity;
import com.yo.android.util.Constants;
import com.yo.dialer.DialerConfig;
import com.yo.dialer.YoSipService;
import com.yo.dialer.googlesheet.UploadCallDetails;
import com.yo.dialer.googlesheet.UploadModel;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import de.greenrobot.event.EventBus;

import static com.yo.android.chat.notification.localnotificationsbuilder.Notifications.GROUP_NOTIFICATION_ID;

/**
 * Created by rdoddapaneni on 6/22/2016.
 */

public class PushNotificationService extends FirebaseMessagingService {
    private static final String TAG = "PushNotificationService";
    private Handler handler = new Handler();

    @Inject
    protected Log mLog;
    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;
    @Inject
    ContactsSyncManager mContactsSyncManager;

    private static final int SIX = 6;

    private static final int STYLE_TEXT = 1;
    private static final int STYLE_INBOX = 2;
    private static final int STYLE_PICTURE = 3;
    private static final int STYLE_TEXT_WITH_ACTION = 4;

    private List<UserData> notificationList = new ArrayList<>();


    @Override
    public void onCreate() {
        super.onCreate();
        Injector.obtain(getApplication()).inject(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        final Map data = remoteMessage.getData();


        mLog.i(TAG, "From: %s", remoteMessage.getFrom());
        mLog.i(TAG, "onMessageReceived: title- %s and data- %s", data.get("title"), data.get("message"));
        //title- Group zxzxzxzx has been created. and data- Group zxzxzxzx has been created with users 917207535681,918897524475,919490570720,918008407207

        EventBus.getDefault().post(Constants.UPDATE_NOTIFICATIONS);

        if (data.get("tag").equals("Topic")) {
            EventBus.getDefault().post(Constants.TOPIC_NOTIFICATION_ACTION);
        } else if (data.get("tag").equals("BalanceTransferred")) {
            EventBus.getDefault().post(Constants.BALANCE_TRANSFER_NOTIFICATION_ACTION);
        } else if (data.get("tag").equals("POPUP")) {
            PopupHelper.handlePop(preferenceEndPoint, data);
        } else if(data.get("tag").equals("Chat") && data.get("title").equals("Chat message stored")) {
            String chatMessageString = data.get("chat_message").toString();
            ChatMessage chatMessage = new Gson().fromJson(chatMessageString, ChatMessage.class);
            newPushNotification(chatMessage.getRoomId(), chatMessage);
        }else if (data.get("tag").equals("Chat")) {
            //update UI , if chat is opened.
            ChatRefreshBackground.getInstance().doRefresh(getApplicationContext(), data.get("firebase_room_id").toString());
            String voxUser = BuildConfig.RELEASE_USER_TYPE + data.get("admin_number") + BuildConfig.RELEASE_USER_TYPE_END;
            String currentVoxUser = preferenceEndPoint.getStringPreference(Constants.VOX_USER_NAME);
            final String message;
            if (currentVoxUser != null && currentVoxUser.equalsIgnoreCase(voxUser)) {
                message = "You created " + data.get("group_name").toString() + " group.";
            } else {
                message = data.get("admin_name").toString() + " added you";
            }
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    sendChatGroupCreatedNotification(data, message);
                }
            }, 2000);
        }

        if (DialerConfig.UPLOAD_REPORTS_GOOGLE_SHEET) {
            UploadModel model = new UploadModel(preferenceEndPoint);
            model.setCaller(preferenceEndPoint.getStringPreference(Constants.VOX_USER_NAME));
            model.setNotificationType(data.get("tag"));
            model.setNotificationDetails(data.get("message"));
            Calendar c = Calendar.getInstance();
            String formattedDate = YoSipService.df.format(c.getTime());
            model.setDate(formattedDate);
            Date d = new Date();
            String currentDateTimeString = YoSipService.sdf.format(d);
            model.setTime(currentDateTimeString);
            String regId = preferenceEndPoint.getStringPreference(Constants.FCM_REFRESH_TOKEN);
            model.setRegId(regId);
            try {
                UploadCallDetails.postDataFromApi(model, "Notifications");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if (preferenceEndPoint.getBooleanPreference(Constants.IS_IN_APP)) {
            if (!data.get("tag").equals("POPUP")) {
                int i = preferenceEndPoint.getIntPreference(Constants.NOTIFICATION_COUNT);
                i = ++i;
                preferenceEndPoint.saveIntPreference(Constants.NOTIFICATION_COUNT, i);
                EventBus.getDefault().post(new NotificationCount(i));
            }
        }
        setBigStyleNotification(data.get("title").toString(), data.get("message").toString(), data.get("tag").toString(), data.get("id").toString());
    }

    private void sendChatGroupCreatedNotification(Map data, String message) {

        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.CHAT_ROOM_ID, data.get("firebase_room_id").toString());
        intent.putExtra(Constants.OPPONENT_PHONE_NUMBER, data.get("group_name").toString());
        intent.putExtra(Constants.TYPE, Constants.YO_NOTIFICATION);

        PendingIntent notificationPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_yo_notification)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_yo_notification))
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setContentTitle(data.get("group_name").toString())
                .setContentIntent(notificationPendingIntent)
                .setContentText(message)
                .setDefaults(Notification.DEFAULT_ALL)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(Notification.PRIORITY_HIGH | Notification.PRIORITY_MAX);
        mNotificationManager.notify(GROUP_NOTIFICATION_ID, builder.build());
        playNotificationSound();
    }

    private void playNotificationSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private int getNotificationIcon() {
        boolean useWhiteIcon = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
        return useWhiteIcon ? R.drawable.ic_yo_notification_white : R.drawable.ic_yo_notification;
    }

    private void setBigStyleNotification(String title, String message, String tag, String id) {
        Notifications notification = new Notifications();

        Intent notificationIntent = new Intent(this, BottomTabsActivity.class);
        //notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.putExtra(Constants.TYPE, Constants.YO_NOTIFICATION);
        notificationIntent.putExtra("title", title);
        notificationIntent.putExtra("message", message);
        notificationIntent.putExtra("tag", tag);
        notificationIntent.putExtra("id", id);
        notificationIntent.putExtra("fromLowBalNotification", false);

        NotificationBuilderObject notificationsInboxData = prepareNotificationData(title, message);
        UserData data = new UserData();
        data.setDescription(message);
        List<UserData> notificationList = new ArrayList<>();
        notificationList.add(data);

        notification.buildInboxStyleNotifications(this, notificationIntent, notificationsInboxData, notificationList, SIX, false, false);
    }

    @NonNull
    private NotificationBuilderObject prepareNotificationData(String title, String message) {
        NotificationBuilderObject notificationData = new NotificationBuilderObject();
        notificationData.setNotificationTitle(title);
        notificationData.setNotificationSmallIcon(getNotificationIcon());
        notificationData.setNotificationText(message);
        notificationData.setNotificationLargeIconDrawable(R.mipmap.ic_launcher);
        notificationData.setNotificationInfo("3");
        return notificationData;
    }

    private void newPushNotification(final String roomId, final ChatMessage chatMessage) {
        if (notificationList != null && notificationList.size() == 0 && chatMessage.getImagePath() != null) {
            ImageLoader.updateImage(getApplicationContext(), chatMessage, Constants.YOIMAGES, new ImageLoader.ImageDownloadListener() {

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

    /*private void playNotificationSound() {
        try {
            if (notification == null) {
                notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
            }
            if (ringtone != null && !ringtone.isPlaying()) {
                ringtone.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    private void sendTrayNotifications(int mode, String roomId, ChatMessage chatMessage) {
        playNotificationSound();
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
            notificationIntent.putExtra(Constants.OPPONENT_PHONE_NUMBER, chatMessage.getRoomName());
            notificationIntent.putExtra(Constants.OPPONENT_CONTACT_IMAGE, chatMessage.getRoomImage());
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
                if (chatMessage.getRoomName() != null) {
                    data.setSenderName(chatMessage.getRoomName());
                } else {
                    String nameFromNumber = mContactsSyncManager.getContactNameByPhoneNumber(chatMessage.getSenderID());
                    data.setSenderName(nameFromNumber);
                }

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
}

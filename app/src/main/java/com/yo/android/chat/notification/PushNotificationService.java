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
import com.orion.android.common.logger.Log;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.chat.ChatRefreshBackground;
import com.yo.android.chat.notification.helper.NotificationCache;
import com.yo.android.chat.notification.localnotificationsbuilder.Notifications;
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
    private static final int SIX = 6;

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
        } else if (data.get("tag").equals("Chat")) {
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
}

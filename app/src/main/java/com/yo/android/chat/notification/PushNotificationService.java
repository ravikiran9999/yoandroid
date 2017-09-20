package com.yo.android.chat.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.orion.android.common.logger.Log;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
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

/**
 * Created by rdoddapaneni on 6/22/2016.
 */

public class PushNotificationService extends FirebaseMessagingService {
    private static final String TAG = "PushNotificationService";

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

        Map data = remoteMessage.getData();

        mLog.i(TAG, "From: %s", remoteMessage.getFrom());
        mLog.i(TAG, "onMessageReceived: title- %s and data- %s", data.get("title"), data.get("message"));
        EventBus.getDefault().post(Constants.UPDATE_NOTIFICATIONS);

        if (data.get("tag").equals("Topic")) {
            EventBus.getDefault().post(Constants.TOPIC_NOTIFICATION_ACTION);
        } else if (data.get("tag").equals("BalanceTransferred")) {
            EventBus.getDefault().post(Constants.BALANCE_TRANSFER_NOTIFICATION_ACTION);
        } else if (data.get("tag").equals("POPUP")) {
            PopupHelper.handlePop(preferenceEndPoint, data);
        }

        UploadModel model = new UploadModel(preferenceEndPoint);
        model.setCaller(preferenceEndPoint.getStringPreference(Constants.VOX_USER_NAME));
        model.setNotificationType(data.get("tag"));
        model.setNotificationDetails(data.get("message"));
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = df.format(c.getTime());
        model.setDate(formattedDate);
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        String currentDateTimeString = sdf.format(d);
        model.setTime(currentDateTimeString);
        String regId = preferenceEndPoint.getStringPreference(Constants.FCM_REFRESH_TOKEN);
        model.setRegId(regId);
        try {
            UploadCallDetails.postDataFromApi(model, "Notifications");
        } catch (IOException e) {
            e.printStackTrace();
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


    private int getNotificationIcon() {
        boolean useWhiteIcon = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
        return useWhiteIcon ? R.drawable.ic_yo_notification_white : R.drawable.ic_yo_notification;
    }

    private void setBigStyleNotification(String title, String message, String tag, String id) {
        Notifications notification = new Notifications();

        Intent notificationIntent = new Intent(this, BottomTabsActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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

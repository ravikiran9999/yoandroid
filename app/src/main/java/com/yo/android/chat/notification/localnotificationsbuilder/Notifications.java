package com.yo.android.chat.notification.localnotificationsbuilder;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;


import com.yo.android.R;
import com.yo.android.chat.notification.helper.AppRunningState;
import com.yo.android.chat.notification.helper.Constants;
import com.yo.android.chat.notification.helper.NotificationCache;
import com.yo.android.chat.notification.pojo.NotificationBuilderObject;
import com.yo.android.chat.notification.pojo.UserData;
import com.yo.android.model.Notification;

import java.util.List;

/**
 * Created by Anitha on 7/12/15.
 */
public class Notifications {
    private static final int ONE = 1;
    private static final int ZERO = 0;
    public static final int NOTIFICATION_ID = 2;
    private NotificationManager mNotificationManager;
    private static Intent intent;

    /**
     * Building Inbox style Notification
     *
     * @param mContext
     * @param destinationClass
     * @param notificationBuilderObject
     * @param notificationList
     * @param maxNotifications
     */
    public void buildInboxStyleNotifications(Context mContext, Intent destinationClass, NotificationBuilderObject notificationBuilderObject, List<UserData> notificationList, int maxNotifications, boolean onGoing, boolean isDialer) {
        String newMessage;

        NotificationCache.get().setCacheNotifications(notificationList);
        //List<UserData> pushNotificationList = NotificationCache.get().getCacheNotifications();
        List<UserData> pushNotificationList = notificationList;
        UserData userData = pushNotificationList.get(0);
        newMessage = pushNotificationList.size() == 1 ? mContext.getResources().getString(R.string.notification_new_message) : mContext.getResources().getString(R.string.notification_new_messages);


        PendingIntent contentIntent = getPendingIntent(mContext, destinationClass);
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext)
                .setContentTitle(pushNotificationList.size() + " " + newMessage)
                .setSmallIcon(R.drawable.ic_yo_notification);
        if (userData.getSenderName() != null && !TextUtils.isEmpty(userData.getSenderName())) {
            mBuilder.setContentText(userData.getSenderName() + " : " + userData.getDescription());
        } else {
            mBuilder.setContentText(userData.getDescription());
        }
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), notificationBuilderObject.getNotificationLargeIconDrawable()));

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(pushNotificationList.size() + " " + newMessage);
        setSummaryText(mContext, maxNotifications, pushNotificationList, inboxStyle);
        mBuilder.setStyle(inboxStyle);
        mBuilder.setNumber(pushNotificationList.size());
        mBuilder.setContentIntent(contentIntent);
        if (onGoing) {
            mBuilder.setOngoing(true);
        } else {
            mBuilder.setAutoCancel(true);
        }
        if (!isDialer) {
            if (!AppRunningState.isRunning(mContext)) {
                mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            }
        } else {
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }
    }


    /**
     * getting Pending Intent
     *
     * @param mContext
     * @return
     */

    private PendingIntent getPendingIntent(Context mContext, final Intent intent) {
        if (intent != null) {
            intent.putExtra(Constants.NOTIFICATIONS_LIST, "list");
        }

        return PendingIntent.getActivity(mContext, ZERO, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * setting summary of Inbox style notifications
     *
     * @param mContext
     * @param maxNotifications
     * @param pushNotificationList
     * @param inboxStyle
     */
    private void setSummaryText(Context mContext, int maxNotifications, List<UserData> pushNotificationList, NotificationCompat.InboxStyle inboxStyle) {
        String moreMessage;
        int unreadedNotification = pushNotificationList.size();
        if (unreadedNotification > maxNotifications) {
            unreadedNotification = maxNotifications;
        }
        for (int i = ZERO; i < unreadedNotification; i++) {
            inboxStyle.addLine(pushNotificationList.get(i).getDescription());
        }
        if (pushNotificationList.size() > maxNotifications) {
            if (pushNotificationList.size() - unreadedNotification == ONE) {
                moreMessage = mContext.getResources().getString(R.string.more_notification);
            } else {
                moreMessage = mContext.getResources().getString(R.string.more_notifications);
            }
            inboxStyle.setSummaryText("+" + (pushNotificationList.size() - unreadedNotification) + " " + moreMessage);
        } else {
            inboxStyle.setSummaryText("");
        }
    }


    public void buildBigTextStyleNotifications(Context mContext, NotificationBuilderObject notificationBuilderObject, Intent destinationClass) {
        PendingIntent contentIntent = getPendingIntent(mContext, destinationClass);

        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(notificationBuilderObject.getNotificationSmallIcon()).setContentTitle(notificationBuilderObject.getNotificationTitle())
                .setContentText(notificationBuilderObject.getNotificationText()).setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), notificationBuilderObject.getNotificationLargeIconDrawable()))
                .setStyle(new NotificationCompat.BigTextStyle()

                        .bigText(notificationBuilderObject.getNotificationLargeText())).setSound(setNotificationSound());
        if (notificationBuilderObject.getActions() != null) {
            int actionsSize = notificationBuilderObject.getActions().size();
            if (actionsSize > 0) {
                int i;
                for (i = 0; i < actionsSize; i++) {
                    mBuilder = addAction(mContext, notificationBuilderObject.getActions().get(i).getActionType(), mBuilder, notificationBuilderObject.getActions().get(i).getActionIcon(), notificationBuilderObject.getActions().get(i).getActionTitle());
                }
            }
        }
        mBuilder.setContentIntent(contentIntent);
        //  if (!AppRunningState.isRunning(mContext) || AppRunningState.isLocked(mContext)) {
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        //  }
    }


    /**
     * Adding actions to the notification
     *
     * @param mContext
     * @param action
     * @param builder
     * @param icon
     * @param actionTitle
     * @return
     */

    private NotificationCompat.Builder addAction(Context mContext, String action, NotificationCompat.Builder builder, int icon, String actionTitle) {
        Intent receiverAction = new Intent();
        receiverAction.setAction(action);
        PendingIntent pendingIntentNo = PendingIntent.getBroadcast(mContext, Constants.ACTION_REQUEST_CODE, receiverAction, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(icon, actionTitle, pendingIntentNo);
        return builder;
    }


    /**
     * setting ring tone to the notifications
     *
     * @return soundUri
     */
    private Uri setNotificationSound() {
        // define sound URI, the sound to be played when there's a notification
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    }
}

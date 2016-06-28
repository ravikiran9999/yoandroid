package com.yo.android.chat.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.orion.android.common.logger.Log;
import com.yo.android.R;
import com.yo.android.di.Injector;
import com.yo.android.ui.MainActivity;

import java.util.Map;

import javax.inject.Inject;

/**
 * Created by rdoddapaneni on 6/22/2016.
 */

public class PushNotificationService extends FirebaseMessagingService {
    private static final String TAG = "PushNotificationService";

    @Inject
    protected Log mLog;

    /**
     * Constructor
     */
    public PushNotificationService() {
        Injector.obtain(getApplication()).inject(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String body = remoteMessage.getNotification().getBody();
        String title = remoteMessage.getNotification().getTitle();
        Map data = remoteMessage.getData();

        mLog.i(TAG, "From: %s", remoteMessage.getFrom());
        mLog.i(TAG, "onMessageReceived: title- %s and data- %s", title, data.toString());
        createNotification(body);
    }

    private void createNotification(String body) {

        Intent destinationIntent = new Intent(this, MainActivity.class);

        int notificationId = body.hashCode();
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), notificationId, destinationIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.BigTextStyle notificationStyle = new NotificationCompat.BigTextStyle();
        notificationStyle.bigText(body);

        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("FCM ChatMessage")
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setStyle(notificationStyle)
                .build();

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notification);


    }
}

package com.yo.android.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.yo.android.R;
import com.yo.android.voip.InComingCallActivity;

/**
 * Created by Ramesh on 1/7/16.
 */
public class Util {

    private void createNotification(Context context, String title, String body) {
        //
        Intent destinationIntent = new Intent(context, InComingCallActivity.class);
        destinationIntent.putExtra("from_notification", true);
        int notificationId = body.hashCode();
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), notificationId, destinationIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.BigTextStyle notificationStyle = new NotificationCompat.BigTextStyle();
        notificationStyle.bigText(body);

        Notification notification = new NotificationCompat.Builder(context.getApplicationContext())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title == null ? "Yo App" : title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setStyle(notificationStyle)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notification);

    }

}

package com.yo.android.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.yo.android.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by Ramesh on 1/7/16.
 */
public class Util {
    public static final int DEFAULT_BUFFER_SIZE = 1024;

    public static <T> int createNotification(Context context, String title, String body, Class<T> clzz, Intent intent) {
        //
        Intent destinationIntent = new Intent(context, clzz);
        destinationIntent.putExtra("from_notification", true);
        destinationIntent.putExtras(intent);
        int notificationId = body.hashCode();
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), notificationId, destinationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.BigTextStyle notificationStyle = new NotificationCompat.BigTextStyle();
        notificationStyle.bigText(body);

        Notification notification = new NotificationCompat.Builder(context.getApplicationContext())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title == null ? "Yo App" : title)
                .setContentText(body)
                .setOngoing(true)
//                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setStyle(notificationStyle)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notification);
        return notificationId;
    }

    public static void cancelNotification(Context context, int notificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
    }

    public static String toString(InputStream inputstream) throws IOException {
        StringWriter stringwriter = new StringWriter();
        copy(inputstream, stringwriter);
        return stringwriter.toString();
    }

    /**
     * Copy.
     *
     * @param input  the input
     * @param output the output
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @SuppressWarnings("resource")
    private static void copy(final InputStream input, final Writer output) throws IOException {
        InputStreamReader in = null;
        try {
            in = new InputStreamReader(input);
            final char[] buffer = new char[DEFAULT_BUFFER_SIZE];
            long count = 0;
            int n = 0;
            while (-1 != (n = in.read(buffer))) {
                output.write(buffer, 0, n);
                count += n;
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (final IOException ex) {
                    //
                }
            }
        }
    }


}

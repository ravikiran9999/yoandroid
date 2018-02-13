package com.yo.android.chat.notification.localnotificationsbuilder;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


import com.yo.android.chat.notification.helper.Constants;
import com.yo.android.chat.notification.pojo.NotificationBuilderObject;
import com.yo.android.chat.notification.pojo.UserData;
import com.yo.android.model.Notification;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;


/**
 * Created by Anitha on 10/12/15.
 * Building Big Picture Style Notifications from URL
 */
public class GeneratePictureStyleNotification extends AsyncTask<String, Void, Bitmap> {

    private static final String TAG = GeneratePictureStyleNotification.class.getSimpleName();
    private Context mContext;
    private String imageUrl;
    private Intent destination;
    private NotificationBuilderObject notificationBuilderObject;
    List<UserData> notificationList;
    private static final int SIX = 6;
    /**
     * Constructor
     *
     * @param context
     * @param destinationAction
     * @param notificationBuilderObject
     */
    public GeneratePictureStyleNotification(Context context, Intent destinationAction, NotificationBuilderObject notificationBuilderObject, List<UserData> notificationList) {
        super();
        this.destination = destinationAction;
        this.mContext = context;
        this.notificationBuilderObject = notificationBuilderObject;
        this.imageUrl = notificationBuilderObject.getNotificationLargeiconUrl();
        this.notificationList = notificationList;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        File file = new File(this.imageUrl);
        if (file.exists()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(this.imageUrl, options);
            return bitmap;
        }

        InputStream in;
        try {
            URL url = new URL(this.imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            in = connection.getInputStream();
            return BitmapFactory.decodeStream(in);
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException" + e);
        } catch (IOException e) {
            Log.e(TAG, "IOException" + e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        if (mContext != null && notificationBuilderObject != null) {
            Notifications notification = new Notifications();
            destination.putExtra(Constants.NOTIFICATIONS_LIST, "value");
            //PendingIntent pendingIntent = PendingIntent.getActivity(mContext, Constants.NOTIFICATION_REQUEST_CODE, destination, PendingIntent.FLAG_UPDATE_CURRENT);
            for (UserData userData : notificationList) {
                userData.setBitmap(result);
            }
            notification.buildInboxStyleNotifications(mContext, destination, notificationBuilderObject, notificationList, SIX, false, true);
        }
    }
}


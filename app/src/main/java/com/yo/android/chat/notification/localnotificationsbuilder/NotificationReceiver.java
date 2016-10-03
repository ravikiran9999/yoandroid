package com.yo.android.chat.notification.localnotificationsbuilder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.yo.android.chat.notification.helper.Constants;


/**
 * Created by Anitha on 11/12/15.
 * Receiver for Notification Actions
 */
public class NotificationReceiver extends BroadcastReceiver {

    public static final String TAG = NotificationReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (Constants.YES_ACTION.equals(action)) {
            Log.v(TAG, "Pressed YES");
            Toast.makeText(context,"Pressed Yes",Toast.LENGTH_LONG).show();
        } else if (Constants.NO_ACTION.equals(action)) {
            Log.v(TAG, "Pressed NO");
            Toast.makeText(context,"Pressed No",Toast.LENGTH_LONG).show();
        } else if (Constants.MAYBE_ACTION.equals(action)) {
            Log.v(TAG, "Pressed MAYBE");
            Toast.makeText(context,"Pressed MAYBE",Toast.LENGTH_LONG).show();

        }
    }
}

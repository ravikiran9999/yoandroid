package com.yo.android.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.MenuItem;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.Job.Builder;
import com.firebase.jobdispatcher.Trigger;
import com.orion.android.common.util.ResourcesHelper;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.chat.firebase.MyServiceConnection;
import com.yo.android.chat.ui.ParentActivity;
import com.yo.android.di.AwsLogsCallBack;
import com.yo.android.di.JobsModule;
import com.yo.android.vox.VoxFactory;

import javax.inject.Inject;

/**
 * Created by ramesh on 12/3/16.
 */
public class BaseActivity extends ParentActivity {

    @Inject
    protected ResourcesHelper mResourcesHelper;

    @Inject
    AwsLogsCallBack mAwsLogsCallBack;

    @Inject
    protected VoxFactory voxFactory;
    @Inject
    protected YoApi.YoService yoService;

    @Inject
    MyServiceConnection myServiceConnection;

    @Inject
    FirebaseJobDispatcher firebaseJobDispatcher;
    private boolean enableBack;

    private boolean isDestroyed;
    //private FirebaseJobDispatcher firebaseJobDispatcher;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAwsLogsCallBack.onCalled(getBaseContext(), getIntent());
        /*Intent intent = new Intent(this, FirebaseService.class);
        startService(intent);*/
        //firebaseJobDispatcher();
    }

    protected void enableBack() {
        enableBack = true;
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroyed = true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean hasDestroyed() {
        return isDestroyed;
    }

    public void createNotification(String title, String message) {

        Intent destinationIntent = new Intent(BaseActivity.this, NotificationsActivity.class);

        int notificationId = title.hashCode();
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), notificationId, destinationIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.BigTextStyle notificationStyle = new NotificationCompat.BigTextStyle();
        notificationStyle.bigText(title);

        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(getNotificationIcon())
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setStyle(notificationStyle)
                .build();

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notification);


    }

    private int getNotificationIcon() {
        boolean useWhiteIcon = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
        return useWhiteIcon ? R.drawable.ic_yo_notification_white : R.drawable.ic_yo_notification;
    }

}

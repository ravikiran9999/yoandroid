package com.yo.android.ui;

import android.app.Activity;
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
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.orion.android.common.util.ResourcesHelper;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.chat.firebase.MyServiceConnection;
import com.yo.android.chat.ui.ParentActivity;
import com.yo.android.di.AwsLogsCallBack;
import com.yo.android.di.JobsModule;
import com.yo.android.vox.VoxFactory;
import com.yo.dialer.CallExtras;
import com.yo.dialer.DialerLogs;
import com.yo.dialer.googlesheet.UploadCallDetails;
import com.yo.dialer.googlesheet.UploadModel;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
    private static final String TAG = BaseActivity.class.getSimpleName();
    //private FirebaseJobDispatcher firebaseJobDispatcher;

    private static Activity activity;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = this;
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

    /**
     * Fetch a list of names and majors of students in a sample spreadsheet:
     * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
     *
     * @return List of names and majors
     * @throws IOException
     */
    public static void getDataFromApi(Sheets sheets, UploadModel model) throws IOException {
        final String spreadsheetId = "1OIkQq2O3en-x0ChsIAy2DX0YPS6n9sMzFQr_-xVy1Qs";
        final String range = "Report!A:H";
        DialerLogs.messageI(TAG, "Uploading to google sheet " + model.getName());
        //Name	Caller	Callee	StatusCode	Reason	Duration	CallType	Comment
        List<List<Object>> values = Arrays.asList(
                Arrays.asList(model.getName(), (Object) model.getCaller(), model.getCallee(), model.getStatusCode(), model.getStatusReason(), model.getDuration(), model.getCallType(), model.getDateTime(), model.getComments()
                )
        );
        ValueRange valueRange = new ValueRange()
                .setValues(values);
        try {
            if (sheets != null && valueRange != null) {
                DialerLogs.messageE(TAG, "Google upload START " + valueRange.get(UploadCallDetails.COMMENT));

                sheets.spreadsheets().values()
                        .append(spreadsheetId,
                                range,
                                valueRange
                        ).setValueInputOption("USER_ENTERED").execute();
            } else {
                DialerLogs.messageE(TAG, "Updated Columns Sheets object is null");
            }
        } catch (UserRecoverableAuthIOException e) {
            e.printStackTrace();
            DialerLogs.messageE(TAG, "Updated Columns Sheets object is null" + e.getMessage());

            Intent intent = e.getIntent();
            intent.putExtra(CallExtras.GOOGLE_DATA, model);
            activity.startActivityForResult(intent, UploadCallDetails.COMPLETE_AUTHORIZATION_REQUEST_CODE);
        }
    }
}

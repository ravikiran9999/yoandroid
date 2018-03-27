package com.yo.android.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.FirebaseException;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.orion.android.common.util.ResourcesHelper;
import com.yo.android.R;
import com.yo.android.api.ApiCallback;
import com.yo.android.api.YoApi;
import com.yo.android.chat.firebase.MyServiceConnection;
import com.yo.android.chat.ui.ParentActivity;
import com.yo.android.di.AwsLogsCallBack;
import com.yo.android.typeface.TypefacePath;
import com.yo.android.usecase.AppLogglyUsecase;
import com.yo.android.util.Constants;
import com.yo.android.util.FireBaseHelper;
import com.yo.android.vox.BalanceHelper;
import com.yo.android.vox.VoxFactory;
import com.yo.dialer.CallExtras;
import com.yo.dialer.DialerLogs;
import com.yo.dialer.googlesheet.UploadCallDetails;
import com.yo.dialer.googlesheet.UploadModel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Callback;

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
    FireBaseHelper fireBaseHelper;

    @Inject
    protected AppLogglyUsecase appLogglyUsecase;

    @Inject
    BalanceHelper mBalanceHelper;

    private boolean enableBack;

    private boolean isDestroyed;
    private static final String TAG = BaseActivity.class.getSimpleName();

    private static Activity activity;
    private Typeface alexBrushRegular;
    Timer myTimer;
    String firebaseToken;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = this;
        mAwsLogsCallBack.onCalled(getBaseContext(), getIntent());

        firebaseToken = preferenceEndPoint.getStringPreference(Constants.FIREBASE_TOKEN);
        if(!firebaseToken.equalsIgnoreCase("")) {
            myTimer = new Timer();
            myTimer.schedule(new TimerTask() {
                @Override
                public void run() {

                    firebaseUserStatus(firebaseToken);
                }

            }, 0, 1000);
        }
    }

    protected void enableBack() {
        enableBack = true;
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(isDeviceRooted(this)) {
            showRootedDeviceMessage();
        }

    }

    // Display rooted message dialog
    public void showRootedDeviceMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.alert);

        builder.setMessage(R.string.rooted_device_msg);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }

                finish();

            }
        });


        AlertDialog dialog = builder.create();

        if (dialog != null) {
            dialog.show();
        }
    }

    // Check whether phone is rooted or not
    public static boolean isDeviceRooted(Context mcon)
    {
        try
        {
            String buildTags = android.os.Build.TAGS;
            if (buildTags != null && buildTags.contains("test-keys"))
            {
                if (!areTestKeysFalse(mcon.getApplicationContext(), Build.MODEL))
                {
                    return true;
                }
            }
            String[] rootApks = new String[]{"superuser.apk", "z4root.apk", "superoneclick.apk", "androot.apk", "z4mod-1.apk", "cwmmanager.apk"};
            HashSet<String> hs = new HashSet<>();
            hs.addAll(Arrays.asList(rootApks));
            try
            {
                File appsFolder = new File("/system/app/");
                File[] fileList = appsFolder.listFiles();
                for (File aFileList : fileList)
                {
                    if (aFileList.isFile())
                    {
                        String name = aFileList.getName().toLowerCase();
                        if (name.endsWith(".apk") && hs.contains(name))
                        {
                            return true;
                        }
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            try
            {
                String knoxbit = getKnoxBit(mcon, Build.MANUFACTURER);
                if ("1".equalsIgnoreCase(knoxbit))
                {
                    return true;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            try
            {
                Process proc = Runtime.getRuntime().exec("su");
                if (proc != null)
                {
                    return true;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    private static String getKnoxBit(Context ctx, String make)
    {
        if ("samsung".equalsIgnoreCase(make))
        {
            String knox = getProp(ctx, "ro.boot.warranty_bit");
            if (knox == null || knox.length() == 0)
            {
                knox = getProp(ctx, "ro.warranty_bit");
            }
            return knox;
        }
        return "";
    }

    public static String getProp(Context ctx, String key)
    {
        String ret;
        try
        {
            ClassLoader cl = ctx.getClassLoader();
            Class SystemProperties = cl.loadClass("android.os.SystemProperties");
            Class[] paramTypes = new Class[1];
            paramTypes[0] = String.class;
            Method get = SystemProperties.getMethod("get", paramTypes);
            Object[] params = new Object[1];
            params[0] = key;
            ret = (String) get.invoke(SystemProperties, params);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            ret = "";
        }
        return ret;
    }

    public static boolean areTestKeysFalse(Context ctx, String model)
    {
        String[] modelIgnoresList = new String[]{"HW-01E", "Elite Power"};
        for (String aModelIgnoresList : modelIgnoresList)
        {
            if (aModelIgnoresList.equalsIgnoreCase(model))
            {
                return true;
            }
        }
        String buildDescription = getProp(ctx, "ro.build.description");
        return !buildDescription.contains("test-keys");
    }

    @Override
    protected void onDestroy() {
        activity = null;
        super.onDestroy();
        isDestroyed = true;
    }

    @Override
    public void finish() {
        stopTimer();
        super.finish();
    }

    public void stopTimer() {
        if (myTimer != null) {
            myTimer.cancel();
        }
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

    /**
     * Fetch a list of names and majors of students in a sample spreadsheet:
     * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
     *
     * @return List of names and majors
     * @throws IOException
     */

    public static void getDataFromApi(Sheets sheets, UploadModel model, String type) throws IOException {
        // Stage
        //final String spreadsheetId = "1OIkQq2O3en-x0ChsIAy2DX0YPS6n9sMzFQr_-xVy1Qs";

        //Production
        final String spreadsheetId = "1qgUuEqHNiKbIHq-yTlLNnHpBx9sCb7YLueJoxVEnLiA";

        List<List<Object>> values = new ArrayList<>();
        String range = " ";
        if (type.equals("Calls")) {
            range = "1.0.4.6 Call Logs!A:L";
            //range = "17.4.5.0!A:L";
            DialerLogs.messageI(TAG, "Uploading to google sheet " + model.getName());
            if (TextUtils.isEmpty(model.getCallee().trim())) {
                model.setCallee("Unknow..");
            }
            values = Arrays.asList(
                    Arrays.asList((Object) model.getName(), (Object) model.getCaller(), model.getCallee(), model.getCallMode(), model.getStatusCode(), model.getStatusReason(), model.getDuration(), model.getCallType(), model.getDate(), model.getTime(), model.getComments(), model.getCurrentBalance(), model.getNotificationDetails()
                    )
            );
        } else if (type.equals("Notifications")) {
            range = "Notifications!A:G";
            DialerLogs.messageI(TAG, "Uploading to google sheet " + model.getName());
            values = Arrays.asList(
                    Arrays.asList((Object) model.getCaller(), (Object) model.getName(), model.getNotificationType(), model.getNotificationDetails(), model.getDate(), model.getTime(), model.getRegId()
                    )
            );
        } else if (type.equals("Magazines")) {
            range = "Magazines!A:F";
            DialerLogs.messageI(TAG, "Uploading to google sheet " + model.getName());
            //Caller Name Topic Name Article Title Date Time
            values = Arrays.asList(
                    Arrays.asList((Object) model.getCaller(), (Object) model.getName(), model.getNotificationType(), model.getNotificationDetails(), model.getDate(), model.getTime()
                    )
            );
        } else if (type.equals("Hold")) {
            range = "Hold!A:G";
            DialerLogs.messageI(TAG, "Uploading to google sheet " + model.getName());
            if (TextUtils.isEmpty(model.getCallee().trim())) {
                model.setCallee("Unknow..");
            }
            values = Arrays.asList(
                    Arrays.asList((Object) model.getName(), (Object) model.getCaller(), model.getCallee(), model.getCallMode(), model.getDate(), model.getTime(), model.getComments()
                    )
            );
        } else if (type.equals("BalanceFailures")) {
            range = "BalanceFailures!A:G";
            DialerLogs.messageI(TAG, "Uploading to google sheet " + model.getName());
            if (TextUtils.isEmpty(model.getCallee().trim())) {
                model.setCallee("Unknown");
            }

            if (TextUtils.isEmpty(model.getToName().trim())) {
                model.setToName("Unknown");
            }
            values = Arrays.asList(
                    Arrays.asList((Object) model.getName(), (Object) model.getCaller(), model.getToName(), model.getCallee(), model.getDate(), model.getTime(), model.getComments()
                    )
            );
        } else if(type.equals("Chat")) {
            range = "Chat!A:G";
            DialerLogs.messageI(TAG, "Uploading to google sheet " + model.getName());
            if (TextUtils.isEmpty(model.getCallee().trim())) {
                model.setCallee("Unknown");
            }

            if (TextUtils.isEmpty(model.getToName().trim())) {
                model.setToName("Unknown");
            }
            values = Arrays.asList(
                    Arrays.asList((Object) model.getName(), (Object) model.getCaller(), model.getToName(), model.getCallee(), model.getDate(), model.getTime(), model.getComments()
                    )
            );
        }
        sendToGoogleDrive(sheets, model, spreadsheetId, range, values);
    }

    private static void sendToGoogleDrive(Sheets sheets, UploadModel model, String spreadsheetId, String range, List<List<Object>> values) {
        ValueRange valueRange = new ValueRange()
                .setValues(values);
        try {
            if (sheets != null && valueRange != null) {
                DialerLogs.messageE(TAG, "Google upload START NAME " + valueRange.getValues().get(0).get(0));
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
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    // Alexbrush regular font
    protected Typeface getAlexBrushRegular() {
        if (alexBrushRegular == null) {
            alexBrushRegular = Typeface.createFromAsset(getAssets(), TypefacePath.ALEX_BRUSH_REGULAR);
        }
        return alexBrushRegular;
    }

    private void firebaseUserStatus(String firebaseToken) {
        fireBaseHelper.authWithCustomToken(getApplicationContext(), firebaseToken, new ApiCallback<Firebase>() {
            @Override
            public void onResult(Firebase result) {
                String firebaseUserId = preferenceEndPoint.getStringPreference(Constants.FIREBASE_USER_ID);
                if(!TextUtils.isEmpty(firebaseUserId)) {
                    initialiseOnlinePresence(result, firebaseUserId);
                }
            }

            @Override
            public void onFailure(String message) {
                android.util.Log.d(TAG, message);
            }
        });

    }

    private void initialiseOnlinePresence(Firebase databaseReference, String userId) {
        try {
            final Firebase onlineRef = databaseReference.child(".info/connected");
            final Firebase currentUserRef = databaseReference.child(Constants.USERS + "/" + userId + "/" + Constants.PROFILE).child("presence");
            onlineRef.addValueEventListener(new com.firebase.client.ValueEventListener() {
                @Override
                public void onDataChange(final com.firebase.client.DataSnapshot dataSnapshot) {
                    //android.util.Log.d(TAG, "DataSnapshot value :" + dataSnapshot.getValue(Boolean.class));
                    if (dataSnapshot.getValue(Boolean.class)) {
                        currentUserRef.onDisconnect().removeValue();
                        currentUserRef.setValue(dataSnapshot.getValue(), 1, new Firebase.CompletionListener() {
                            @Override
                            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                if (firebaseError != null) {
                                    android.util.Log.d(TAG, firebaseError.getDetails());
                                }
                            }
                        });
                    } else {
                        currentUserRef.setValue(false);
                    }
                }

                @Override
                public void onCancelled(final FirebaseError databaseError) {
                    android.util.Log.d(TAG, "DatabaseError:" + databaseError);
                }
            });
        } catch (FirebaseException e) {
            Log.e(TAG, "Firebase error :" + e.getMessage());
        }
    }

    // clear glide memory
    public void clearGlideMemory(Context context) {
        Glide glide = Glide.get(context);
        glide.clearMemory();
    }

    public void showMessageDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        final View view = layoutInflater.inflate(R.layout.custom_dialog, null);
        builder.setView(view);

        TextView textView = (TextView) view.findViewById(R.id.dialog_content);


        textView.setText(R.string.enable_camera_permission_settings);


        Button yesBtn = (Button) view.findViewById(R.id.yes_btn);
        yesBtn.setText(getResources().getString(R.string.yes));
        Button noBtn = (Button) view.findViewById(R.id.no_btn);
        noBtn.setText(getResources().getString(R.string.cancel));
        noBtn.setVisibility(View.VISIBLE);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.getWindow().setBackgroundDrawable(new BitmapDrawable());
        alertDialog.show();


        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                //startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                startActivity(intent);
            }

        });

        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();

            }
        });
    }
}

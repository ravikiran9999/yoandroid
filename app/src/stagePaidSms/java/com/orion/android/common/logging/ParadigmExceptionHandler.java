package com.orion.android.common.logging;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.yo.android.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;


/**
 * Handles all the un-handled exception occurring anywhere in the application.
 * It also sends a report to the server. After the report is sent, it restarts
 * the application.<br>
 * <br>
 * <p/>
 * <b>Usage</b><br>
 * In your main activity's <code>onCreate</code> method write<br>
 * <code>
 * intent = PendingIntent.getActivity(getApplication().getBaseContext(), 0, new Intent(getIntent()), getIntent().getFlags());
 * </code> <br>
 * <br>
 * Pass the <code>intent</code> above to the constructor of
 * <code>ParadigmExceptionHandler</code> along with the <code>Context</code> of
 * the application.<br>
 * <br>
 * Finally set the
 * <code>Thread.setDefaultUncaughtExceptionHandler(ParadigmExceptionHandler instance)</code>
 * with the instance of <code>ParadigmExceptionHandler</code> created earlier.
 *
 * @author
 */

public class ParadigmExceptionHandler implements
        Thread.UncaughtExceptionHandler {

    private static final String TAG = ParadigmExceptionHandler.class.getSimpleName();
    private static final String INFO = "Info ";
    private static final String MODEL = "Model ";


    private Context mContext;
    private PendingIntent intent;
    private String versionName = "", packageName = "", filePath = "", phoneModel = "",
            androidVersion = "", board = "", brand = "", device = "", display = "", fingerPrint = "", host = "";
    private String id = "", model = "", product = "", tags = "", time = "", type = "", user = "",
            availableInternalMemory = "", totalInternalMemory = "";
    private String cause = "";


    public ParadigmExceptionHandler(Context context, PendingIntent intent) {
        super();
        this.mContext = context;
        this.intent = intent;
    }

    public ParadigmExceptionHandler() {
        super();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (thread != null) {
            //
        }
        Log.e("Exception ", "Crashed");
        // Getting the cause of exception and its stack trace
        if (ex.getMessage() != null) {

            cause = ex.getMessage() + "\n";
        }

        if (ex.getCause() != null) {
            cause += ex.toString() + "\n";
        }

        StackTraceElement[] ele = ex.getStackTrace();
        if (ele != null) {
            for (StackTraceElement el : ele) {
                cause += "   File: " + el.getFileName() + "   Class:"
                        + el.getClassName() + "   Method:" + el.getMethodName()
                        + "   Line:" + el.getLineNumber() + "\n";
            }
        }

        // Getting the data of phone
        List<NameValuePair> data = collectLocalInformation(mContext);

        // Adding the cause to data
        BasicNameValuePair bnvp = new BasicNameValuePair("exception", cause);
        data.add(bnvp);

        printLocalInformation();
        Logger.info("Crash Happened :: " + cause);

		/*
         * submits the log to amazon S3 server
		 */
        submitLogToAmazonServer(mContext);

        if (mContext != null) {
            //Exit the application
            android.os.Process.killProcess(android.os.Process.myPid());
        }

    }

    /**
     * Gets the available internal memory
     *
     * @return
     */
    public long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    private void restartApp() {
        if (mContext != null && intent != null) {
            try {
                AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, intent);
            } catch (Exception e) {
                if (e.getMessage() != null) {
                    Log.e(TAG, e + "");
                }
            }
        }
    }


    /**
     * Gets the total internal memory
     *
     * @return
     */
    public long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    /**
     * Collects phone and application specific data and creates an
     * <code>ArrayList</code> in the form required by the
     * <code>WebServiceWrapper</code>. This data is sent to the server.
     *
     * @param context Application's context
     * @return
     */
    public List<NameValuePair> collectLocalInformation(Context context) {
        List<NameValuePair> data = new ArrayList<NameValuePair>();

        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi;
            // Version
            pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            // Package name
            packageName = pi.packageName;
            // Files dir for storing the stack traces
            filePath = context.getFilesDir().getAbsolutePath();
            // Device model
            phoneModel = android.os.Build.MODEL;
            // Android version
            androidVersion = android.os.Build.VERSION.RELEASE;

            board = android.os.Build.BOARD;
            brand = android.os.Build.BRAND;
            // CPU_ABI = android.os.Build.
            device = android.os.Build.DEVICE;
            display = android.os.Build.DISPLAY;
            fingerPrint = android.os.Build.FINGERPRINT;
            host = android.os.Build.HOST;
            id = android.os.Build.ID;
            // Manufacturer = android.os.Build.
            model = android.os.Build.MODEL;
            product = android.os.Build.PRODUCT;
            tags = android.os.Build.TAGS;
            time = android.os.Build.TIME + "";
            type = android.os.Build.TYPE;
            user = android.os.Build.USER;

            // Internal Memory
            totalInternalMemory = getTotalInternalMemorySize() + "";
            availableInternalMemory = getAvailableInternalMemorySize() + "";

        } catch (NameNotFoundException e) {
            Logger.logStackTrace(e);
            Log.e("Exception ", "ParadigmExceptionHandler.RecoltInformations()"
                    + e);
        } catch (Exception e) {
            Logger.logStackTrace(e);
        }

        // Adding the values
        BasicNameValuePair bnvp = new BasicNameValuePair("versionName",
                versionName);
        data.add(bnvp);

        bnvp = new BasicNameValuePair("versionName", versionName);
        data.add(bnvp);

        bnvp = new BasicNameValuePair("packageName", packageName);
        data.add(bnvp);

        bnvp = new BasicNameValuePair("phoneModel", phoneModel);
        data.add(bnvp);

        bnvp = new BasicNameValuePair("brand", brand);
        data.add(bnvp);

        bnvp = new BasicNameValuePair("board", board);
        data.add(bnvp);

        bnvp = new BasicNameValuePair("device", device);
        data.add(bnvp);

        bnvp = new BasicNameValuePair("display", display);
        data.add(bnvp);

        bnvp = new BasicNameValuePair("fingerPrint", fingerPrint);
        data.add(bnvp);

        bnvp = new BasicNameValuePair("host", host);
        data.add(bnvp);

        bnvp = new BasicNameValuePair("product", product);
        data.add(bnvp);

        bnvp = new BasicNameValuePair(MODEL, model);
        data.add(bnvp);

        bnvp = new BasicNameValuePair("id", id);
        data.add(bnvp);

        bnvp = new BasicNameValuePair("user", user);
        data.add(bnvp);

        bnvp = new BasicNameValuePair("type", type);
        data.add(bnvp);

        bnvp = new BasicNameValuePair("tags", tags);
        data.add(bnvp);

        bnvp = new BasicNameValuePair("totalInternalMemory",
                totalInternalMemory);
        data.add(bnvp);

        bnvp = new BasicNameValuePair("availableInternalMemory",
                availableInternalMemory);
        data.add(bnvp);

        return data;
    }

    public void printLocalInformation() {
        String localDeviceInformation = "";

        Log.i(INFO, "Version: " + versionName);
        localDeviceInformation = localDeviceInformation + "Version: " + versionName + "\n";
        Log.i(INFO, "PKG: " + packageName);
        localDeviceInformation = localDeviceInformation + "PKG: " + packageName + "\n";
        Log.i(INFO, "File: " + filePath);
        localDeviceInformation = localDeviceInformation + "File: " + filePath + "\n";
        Log.i(INFO, MODEL + ":" + phoneModel);
        localDeviceInformation = localDeviceInformation + MODEL + ":" + phoneModel + "\n";

        Log.i(INFO, "Android Version: " + androidVersion);
        localDeviceInformation = localDeviceInformation + "Android Version: " + androidVersion + "\n";
        Log.i(INFO, "Board: " + board);
        localDeviceInformation = localDeviceInformation + "Board: " + board + "\n";
        Log.i(INFO, "Brand: " + brand);
        localDeviceInformation = localDeviceInformation + "Brand: " + brand + "\n";
        Log.i(INFO, "Device: " + device);
        localDeviceInformation = localDeviceInformation + "Device: " + device + "\n";

        Log.i(INFO, "Display: " + display);
        localDeviceInformation = localDeviceInformation + "Display: " + display + "\n";
        Log.i(INFO, "Fngr: " + fingerPrint);
        localDeviceInformation = localDeviceInformation + "Fngr: " + fingerPrint + "\n";
        Log.i(INFO, "Host: " + host);
        localDeviceInformation = localDeviceInformation + "Host: " + host + "\n";
        Log.i(INFO, "ID: " + id);
        localDeviceInformation = localDeviceInformation + "ID: " + id + "\n";

        Log.i(INFO, MODEL + ":" + model);
        localDeviceInformation = localDeviceInformation + MODEL + ":" + model + "\n";
        Log.i(INFO, "Product: " + product);
        localDeviceInformation = localDeviceInformation + "Product: " + product + "\n";
        Log.i(INFO, "Tags: " + tags);
        localDeviceInformation = localDeviceInformation + "Tags: " + tags + "\n";
        Log.i(INFO, "Time: " + time);
        localDeviceInformation = localDeviceInformation + "Time: " + time + "\n";

        Log.i(INFO, "User: " + user);
        localDeviceInformation = localDeviceInformation + "User: " + user + "\n";
        Log.i(INFO, "Avail Mem: " + availableInternalMemory);
        localDeviceInformation = localDeviceInformation + "Avail Mem: " + availableInternalMemory + "\n";
        Log.i(INFO, "Total Mem: " + totalInternalMemory);
        localDeviceInformation = localDeviceInformation + "Total Mem: " + totalInternalMemory + "\n";

        Logger.info("Device Information::\n" + localDeviceInformation);
    }


    public void submitLogToAmazonServer(Context context) {
        // Submitting the report
        //Generate current date in YYYY-MM-DD format
        Calendar currentDate = Calendar.getInstance();
        int month = currentDate.get(Calendar.MONTH) + 1;
        //Calendar.MONTH starts with 0
        final String now = "" + currentDate.get(Calendar.YEAR) + "-" + month + "-" + currentDate.get(Calendar.DATE);

        //To generate Universal Unique ID
        final String randomID = UUID.randomUUID().toString();
        String logFileFullPath = "";
        if (context != null) {
            logFileFullPath = DeviceInfoUtil.getSdCardPath() + context.getString(R.string.log_folder_path) + "/" +
                    context.getString(R.string.log_file) +
                    "." + context.getString(R.string.log_extension);
        }

        final String phoneModels = "" + android.os.Build.MODEL;

        final File logFile = new File(logFileFullPath);
        //TODO:Need to check with Rajesh
        new Thread(new Runnable() {
            @Override
            public void run() {
                StoreToS3.sendToS3(logFile, "android/logs/" + now + "/" + GlobalClass.LOG_FB_USER_NAME
                        + "_" + phoneModels + "_V" + versionName + "_" + randomID + ".log", null);
            }
        }).start();

    }

}
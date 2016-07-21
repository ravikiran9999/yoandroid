package com.orion.android.common.logging;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.yo.android.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;


/**
 * This class simply records the user desired data to log file created in the
 * application memory. A new file is created every day and the log data is
 * appended to that particular file. Additional functionality could be built,
 * such as sending the log file to server every day, deleting the past files.
 *
 * @author pcs-05
 */

public class Logger {
    private static String TAG = "LOGGER";
    private static boolean isExternalStorageAvailable = false;
    private static boolean isExternalStorageWriteable = false;
    private static boolean isMemoryAvailable = false;
    private static long megAvailable;

    private static Context context = null;

    private static final long BYTES_AVAILABLE = 1048576L;

    /**
     * Perform operations to initialize the LoggerClass and writes the device info as the first log
     *
     * @param context <code>Context</code> to be used for all the system operations. NOTE: Should not be <code>null</code><br>
     *                The operations are as follows:<br>
     *                <ul>
     *                <li>Initialized the context</li>
     *                <li>Create the log directory if it does not exists</li>
     *                <li>Write Device Info into the logs</li>
     *                </ul>
     * @return <code>true</code> if init is successful else <code>false</code>
     */
    public static boolean init(Context context) {
        boolean initialized = true;

        //Operation 1: Its very important to have a valid context here
        if (context == null) {
            Log.e(TAG, "Error initializing the Fabula LoggerClass. Supplied context is null");
            return false;
        }
        Logger.context = context;

        //Operation 2: Create the log directory
        if (createLogDir()) {
            Log.i(TAG, "Successfully created Logs dir or Found " +
                    "dir already created");

            createLogFile();
        } else {
            Log.e(TAG, "Error initializing the Fabula LoggerClass. " +
                    "Unable to create the log directory. " +
                    "Either SD card unavailable/not mounted or " +
                    "some unknown IO error. Please check the trace");
            return false;
        }

        checkMedia();

        return initialized;
    }

    /**
     * Create the log directory in the SD card
     *
     * @return <code>true</code> if directory is created successfully, <code>false</code> otherwise.
     */
    private static boolean createLogDir() {
        boolean created = false;
        try {
            File logDir = new File(DeviceInfoUtil.getSdCardPath() + "/" + context.getResources().getString(LogConstants.LOG_FOLDER_PTH));
            Log.e(TAG, "Log Directory " + DeviceInfoUtil.getSdCardPath() + context.getResources().getString(LogConstants.LOG_FOLDER_PTH));
            if (!logDir.exists()) {
                created = logDir.mkdirs();
            } else {
                //Log dir already present.
                created = true;
            }
        } catch (Exception e) {
            if (GlobalClass.DEBUG) {
                Log.e(TAG, "Exception while creating Log Directory");
            }
            logStackTrace(e);
        }

        return created;
    }

    public static void createLogFile() {
        try {
            File logFile = new File(DeviceInfoUtil.getSdCardPath() + "/" + context.getResources().getString(LogConstants.LOG_FOLDER_PTH), generateTheFileName());
            if (!logFile.isDirectory()) {
                logFile.delete();
                logFile.createNewFile();
            }

        } catch (IOException e) {
            if (GlobalClass.DEBUG) {
                Log.e(TAG, "Exception while creating Log File");
            }
            logStackTrace(e);
        }

    }

    /**
     * Generates the log file name based on today's date
     *
     * @return
     */
    private static String generateTheFileName() {
        String fileName = "";

        String now = DateFormat.getDateInstance(DateFormat.SHORT).format(new Date());
        now = now.replaceAll("/", "");

        fileName = context.getResources().getString(R.string.log_file) + "." + context.getResources().getString(R.string.log_extension);

        return fileName;

    }

    public static void warn(String message) {
        //Call writeLog(Severity.WARNING, message)
        if (GlobalClass.DEBUG) {
            Log.w(TAG, message);
            writeLog(Severity.WARNING, message);
            Log.w(TAG, message);
        }
    }

    public static void info(String message) {
        //Call writeLog(Severity.INFO, message)
        Log.e(TAG, message);
        if (GlobalClass.DEBUG) {
            Log.i(TAG, message);
            writeLog(Severity.INFO, message);
            Log.v(TAG, message);
        }
    }

    public static void fatal(String message) {
        //Call writeLog(Severity.FATAL, message)
        if (GlobalClass.DEBUG) {
            Log.e(TAG, message);
            writeLog(Severity.INFO, message);
            Log.e(TAG, message);
        }
    }

    private static synchronized boolean writeLog(Severity severity, String logString) {
        boolean written = false;
        //Check if context is valid or not
        //Check whether external media is present/mounted or not
        //Check whether its writable or not
        //Check if space is available or not
        //Check if the file name exists or not
        //WRITE!! -- TIMESTAMP, SEVERITY, MESSAGE

        if (!(context == null)) {
            if (isExternalStorageAvailable && isExternalStorageWriteable && isMemoryAvailable) {
                try {
                    FileWriter writer = new FileWriter(DeviceInfoUtil.getSdCardPath() + "/" + context.getResources().getString(LogConstants.LOG_FOLDER_PTH) + "/" + generateTheFileName(), true);
                    BufferedWriter out = new BufferedWriter(writer);
                    out.write("\n" + DateFormat.getDateTimeInstance().format(new Date()) + "\t\t" + severity.name() + "\t\t" + logString);
                    out.close();
                } catch (IOException e) {

                    logStackTrace(e);
                } catch (Exception e) {
                    logStackTrace(e);
                }
            } else {
                Log.w(TAG, "Something went wrong with SD card storage");
            }

        } else {
            if (GlobalClass.DEBUG) {
                Log.w(TAG, "Supplied Context not valid");
            }
        }

        return written;
    }

    public static void checkMedia() {
        String state = Environment.getExternalStorageState();

        if (state.equals(Environment.MEDIA_MOUNTED)) {
            isExternalStorageAvailable = true;

            if (!state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
                isExternalStorageWriteable = true;
            }

            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getBlockCount();
            megAvailable = bytesAvailable / BYTES_AVAILABLE;
            Log.e(TAG, "Megs :" + megAvailable);
            if (megAvailable > 0) {
                isMemoryAvailable = true;
            }
        } else {
            //Media not available
            Log.i(TAG, "SD card not available");
        }


    }

    public static void logStackTrace(Exception e) {
        String exception = "";
        exception = e.toString() + "\n";
        StackTraceElement[] temp = e.getStackTrace();
        for (int i = 0; i < temp.length; i++) {
            exception = exception + " Exception at " + temp[i].toString() + "\n";
        }

        Log.w(TAG, exception);
        writeLog(Severity.FATAL, exception);
        //return exception
    }

    enum Severity {
        INFO, WARNING, FATAL
    }

}

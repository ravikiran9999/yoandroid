
package com.orion.android.common.logging;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.os.StatFs;
import android.util.DisplayMetrics;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class DeviceInfoUtil.
 */
public class DeviceInfoUtil {

    /** The Constant MEGABYTE. */
    private static final int MEGABYTE = 1048576;

    /** The Constant TAG. */
    private static final String TAG = "DeviceInfoUtil";

    /** The m context. */
    private final Context mContext;

    /**
     * Instantiates a new device info util.
     * @param mContext the m context
     */
    public DeviceInfoUtil(final Context mContext) {
        this.mContext = mContext;
    }

    /**
     * Returns the application directory.
     * @param context the context
     * @return Application directory, <code>null</code> if not available
     */
    public static String getAppDirectory(final Context context) {
        if (context != null) {
            final String sdCardPath = DeviceInfoUtil.getSdCardPath();
            if (sdCardPath != null) {
                return sdCardPath;
            }
        }
        return null;
    }

    /**
     * Gets the available internal memory.
     * @return the available internal memory size
     */
    @SuppressWarnings("deprecation")
    public static long getAvailableInternalMemorySize() {
        final File path = Environment.getDataDirectory();
        final StatFs stat = new StatFs(path.getPath());
        final long blockSize = stat.getBlockSize();
        final long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    /**
     * This method is used to get the path of the SD card.
     * @return the sd card path
     */
    public static String getSdCardPath() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return Environment.getExternalStorageDirectory().getPath() + File.separator;
        }
        return null;
    }

    /**
     * Gets the total internal memory.
     * @return the total internal memory size
     */
    @SuppressWarnings("deprecation")
    public static long getTotalInternalMemorySize() {
        final File path = Environment.getDataDirectory();
        final StatFs stat = new StatFs(path.getPath());
        final long blockSize = stat.getBlockSize();
        final long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    /**
     * Check if media is readable or not.
     * @return <code>true</code> if external media is readable, otherwise <code>false</code>
     */
    public static boolean mediaReadable() {
        final String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Check whether external media is writable or not.
     * @return <code>true</code> if external media is available and have some free memory to write, else
     *         <code>false</code>
     */
    @SuppressWarnings("deprecation")
    public static boolean mediaWritable() {
        boolean writable = false;
        final String state = Environment.getExternalStorageState();
        // If media is mounted and read-only then verify whether space is available or not
        if (Environment.MEDIA_MOUNTED.equals(state) && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            final StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            // need the casts for the long to prevent negative bytesAvailable
            final long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getBlockCount();
            writable = (bytesAvailable / MEGABYTE) > 0;
        }
        return writable;
    }

    /**
     * Collect device info.
     * @return the list
     */
    public List<String> collectDeviceInfo() {
        final List<String> deviceInfoList = new ArrayList<String>();
        PackageManager pm = null;
        if (mContext != null) {
            pm = mContext.getPackageManager();
        }
        try {
            String versionName = "";
            String packageName = "";
            if (pm != null) {
                PackageInfo pi;
                pi = pm.getPackageInfo(mContext.getPackageName(), 0);
                // Version
                versionName = "versionName : " + pi.versionName;
                // Package name
                packageName = "packageName : " + pi.packageName;
            }
            String filePath = "";
            if (mContext != null) {
                // Files dir for storing the stack traces
                filePath = "filePath : " + mContext.getFilesDir().getAbsolutePath();
            }
            // Device model
            final String phoneModel = "phoneModel : " + android.os.Build.MODEL;
            // Android version
            final String androidVersion = "androidVersion : " + android.os.Build.VERSION.RELEASE;
            final String board = "board : " + android.os.Build.BOARD;
            final String brand = "brand : " + android.os.Build.BRAND;
            // CPU_ABI = android.os.Build.
            final String device = "device : " + android.os.Build.DEVICE;
            final String display = "display : " + android.os.Build.DISPLAY;
            final String fingerPrint = "fingerPrint : " + android.os.Build.FINGERPRINT;
            final String host = "host : " + android.os.Build.HOST;
            final String id = "id : " + android.os.Build.ID;
            // Manufacturer = android.os.Build.
            final String model = "model : " + android.os.Build.MODEL;
            final String product = "product : " + android.os.Build.PRODUCT;
            final String tags = "tags : " + android.os.Build.TAGS;
            final String time = "time : " + android.os.Build.TIME;
            final String type = "type : " + android.os.Build.TYPE;
            final String user = "user : " + android.os.Build.USER;
            // Internal Memory
            final String totalInternalMemory = "totalInternalMemory : " + getTotalInternalMemorySize() + "";
            final String availableInternalMemory = "availableInternalMemory : " + getAvailableInternalMemorySize() + "";
            String screenResolution = "";
            if (mContext != null && mContext instanceof Activity) {
                // Screen Resolution
                final DisplayMetrics dm = new DisplayMetrics();
                ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
                screenResolution = "screenResolution : " + dm.widthPixels + " x " + dm.heightPixels;
            }
            deviceInfoList.add(versionName);
            deviceInfoList.add(packageName);
            deviceInfoList.add(filePath);
            deviceInfoList.add(phoneModel);
            deviceInfoList.add(androidVersion);
            deviceInfoList.add(board);
            deviceInfoList.add(brand);
            deviceInfoList.add(device);
            deviceInfoList.add(display);
            deviceInfoList.add(fingerPrint);
            deviceInfoList.add(host);
            deviceInfoList.add(id);
            deviceInfoList.add(model);
            deviceInfoList.add(product);
            deviceInfoList.add(tags);
            deviceInfoList.add(time);
            deviceInfoList.add(type);
            deviceInfoList.add(user);
            deviceInfoList.add(totalInternalMemory);
            deviceInfoList.add(availableInternalMemory);
            deviceInfoList.add(screenResolution);
        } catch (final NameNotFoundException e) {
            if (LogConstants.AWS_LOGS) {
                Logger.fatal( TAG + e + "");
            }
        }
        return deviceInfoList;
    }

}

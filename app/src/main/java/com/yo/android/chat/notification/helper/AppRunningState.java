package com.yo.android.chat.notification.helper;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;

import java.util.List;

/**
 * Created by pcs-05 on 7/12/15.
 */
public class AppRunningState {

    private AppRunningState() {
        //defauilt constructor is nedded
    }

    /**
     * Checks if is running..
     *
     * @param ctx the context
     * @return true, if is running
     */
    public static boolean isRunning(final Context ctx) {
        // returns a handle to the system-level service by Context.ACTIVITY_SERVICE
        final ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        // Returns the list of tasks that are currently running with the most recent being first,and older ones later in
        // order
        final List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
        // Retrieves the information of a particular task that is currently running in the system
        for (final ActivityManager.RunningTaskInfo task : tasks) {
            if (ctx.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName())) {
                // isApplicationBroughtToBackground(ctx) method returns true,if the application is brought to the
                // background.
                return !isApplicationBroughtToBackground(ctx);
            }
        }
        return false;
    }


    /**
     * Checks if is application brought to background.
     *
     * @param context the context
     * @return true, if is application brought to background
     */
    public static boolean isApplicationBroughtToBackground(final Context context) {
        // returns a handle to the system-level service by Context.ACTIVITY_SERVICE
        final ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        // Getting the very first recent task out of the list...
        final List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        // checks whether the list contains no tasks
        if (!tasks.isEmpty()) {
            // returns the task the user is currently doing...
            final ComponentName topActivity = tasks.get(0).topActivity;
            // checking the recent activity to be not null & and checking whether the activity is in background
            if (topActivity != null && !topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * if the device is in locked state
     *
     * @param context
     * @return true if locked
     */
    public static boolean isLocked(final Context context) {
        KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return myKM.inKeyguardRestrictedInputMode();
    }
}

package com.yo.dialer;

import android.app.Activity;
import android.util.Log;

import com.yo.dialer.model.CallLog;

/**
 * Created by Rajesh Babu Polamarasetti on 11/7/17.
 */

public class CallLogs {
    private static final String TAG = CallLogs.class.getSimpleName();
    public static final int APP_TO_APP_CALL_LOG = 1;
    public static final int APP_TO_PSTN_CALL_LOG = 2;
    public static final int TOTAL_CALL_LOG = 3;
    public static final int CLEAR_CALL_LOGS = 4;

    /**
     * Returns call logs based on filter supplied like, app to app, app to pstn and total call logs.
     * Returns empty CallLog object if filter is unknown.
     *
     * @param activity   : Context required to read call logs from the database.
     * @param lister     : when reading call logs from the database is complete this will send callback with the results.
     * @param filterType : CallLogs.APP_TO_APP_CALL_LOG ,CallLogs.APP_TO_PSTN_CALL_LOG or CallLogs.TOTAL_CALL_LOG.
     */
    public static void load(final Activity activity, final CallLogCompleteLister lister, final int filterType) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                CallLog results = new CallLog();
                if (filterType == APP_TO_APP_CALL_LOG) {
                    results.setCallLogResults(com.yo.android.calllogs.CallLog.Calls.getAppToAppCallLog(activity));
                } else if (filterType == APP_TO_PSTN_CALL_LOG) {
                    results.setCallLogResults(com.yo.android.calllogs.CallLog.Calls.getPSTNCallLog(activity));
                } else if (filterType == TOTAL_CALL_LOG) {
                    results.setCallLogResults(com.yo.android.calllogs.CallLog.Calls.getCallLog(activity));
                } else if (filterType == CLEAR_CALL_LOGS) {
                    com.yo.android.calllogs.CallLog.Calls.clearCallHistory(activity);
                } else {
                    if (DialerConfig.ENABLE_LOGS) {
                        Log.e(TAG, "Invalid filter type to load call logs.");
                    }
                }
                if (lister != null) {
                    lister.callLogsCompleted(results);
                }
            }
        }).start();
    }
}

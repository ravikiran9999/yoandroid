package com.yo.dialer;

import android.app.Activity;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.calllogs.CallLog;
import com.yo.android.model.dialer.CallLogsResult;
import com.yo.android.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by root on 11/7/17.
 */

public class Filter {

    public static final String ALL_CALLS = "all calls";
    public static final String PAID_CALLS = "paid calls";
    public static final String APP_CALLS = "app calls";

    public static void save(final int itemId, final PreferenceEndPoint preferenceEndPoint) {
        String str = null;
        if (itemId == R.id.menu_all_calls) {
            str = ALL_CALLS;
        } else if (itemId == R.id.menu_paid_calls) {
            str = PAID_CALLS;
        } else if (itemId == R.id.menu_app_calls) {
            str = APP_CALLS;
        }
        if (str != null) {
            preferenceEndPoint.saveStringPreference(Constants.DIALER_FILTER, str);
        }
    }

    public static void filteredData(final Activity activity, final PreferenceEndPoint preferenceEndPoint, final CallLogCompleteLister lister) {
        final String filter = preferenceEndPoint.getStringPreference(Constants.DIALER_FILTER, ALL_CALLS);
        final FilterData filterData = getFilterType(filter,activity);
        CallLogs.load(activity, new CallLogCompleteLister() {
            @Override
            public void callLogsCompleted(com.yo.dialer.model.CallLog callLog) {
                ArrayList<Map.Entry<String, List<CallLogsResult>>> tempResults = new ArrayList<>();
                ArrayList<Map.Entry<String, List<CallLogsResult>>> results = new ArrayList<>();
                results = prepare(filterData.getFilterTitle(), results, filterData.getFilterData());
                tempResults.addAll(results);
                results.clear();
                results.addAll(tempResults);
                //Sending the call logs result
                com.yo.dialer.model.CallLog callLogs = new com.yo.dialer.model.CallLog();
                callLogs.setCallLogResults(results);
                lister.callLogsCompleted(callLogs);
            }
        }, filterData.getFilterType());
    }

    public static FilterData getFilterType(String filter,Activity activity) {
        int filterType = -1;
        FilterData filterData = new FilterData();
        if (filter.equalsIgnoreCase(ALL_CALLS)) {
            filterType = CallLogs.TOTAL_CALL_LOG;
            filterData.setFilterType(filterType);
            filterData.setFilterData(CallLog.Calls.getCallLog(activity));
            filterData.setFilterTitle(activity.getString(R.string.all_calls));
        } else if (filter.equalsIgnoreCase(APP_CALLS)) {
            filterType = CallLogs.APP_TO_APP_CALL_LOG;
            filterData.setFilterType(filterType);
            filterData.setFilterData(CallLog.Calls.getAppToAppCallLog(activity));
            filterData.setFilterTitle(activity.getString(R.string.free_calls));
        } else if (filter.equalsIgnoreCase(PAID_CALLS)) {
            filterType = CallLogs.APP_TO_PSTN_CALL_LOG;
            filterData.setFilterType(filterType);
            filterData.setFilterData(CallLog.Calls.getPSTNCallLog(activity));
            filterData.setFilterTitle(activity.getString(R.string.paid_calls));
        }
        return filterData;
    }

    private static ArrayList<Map.Entry<String, List<CallLogsResult>>> prepare(String type, ArrayList<Map.Entry<String, List<CallLogsResult>>> results, ArrayList<Map.Entry<String, List<CallLogsResult>>> checkList) {
        if (!checkList.isEmpty()) {
            List<CallLogsResult> resultList = new ArrayList<>();
            HashMap<String, List<CallLogsResult>> hashMap = new HashMap<>();
            CallLogsResult result = new CallLogsResult();
            result.setHeader(true);
            result.setHeaderTitle(type);
            resultList.add(result);
            hashMap.put(type, resultList);
            results = new ArrayList(hashMap.entrySet());
            results.addAll(checkList);
        }
        return results;
    }
}

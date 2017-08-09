package com.yo.dialer.model;

import com.yo.android.model.dialer.CallLogsResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Rajesh on 11/7/17.
 */

public class CallLog {
    private ArrayList<Map.Entry<String, List<CallLogsResult>>> results;

    public ArrayList<Map.Entry<String, List<CallLogsResult>>> getResults() {
        return results;
    }

    public void setCallLogResults(ArrayList<Map.Entry<String, List<CallLogsResult>>> callLogResults) {
        this.results = callLogResults;
    }

}
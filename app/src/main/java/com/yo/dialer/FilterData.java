package com.yo.dialer;

import com.yo.android.model.dialer.CallLogsResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by root on 11/8/17.
 */

class FilterData {
    private int filterType;

    public String getFilterTitle() {
        return filterTitle;
    }

    public void setFilterTitle(String filterTitle) {
        this.filterTitle = filterTitle;
    }

    private String filterTitle;

    public ArrayList<Map.Entry<String, List<CallLogsResult>>> getFilterData() {
        return filterData;
    }

    public void setFilterData(ArrayList<Map.Entry<String, List<CallLogsResult>>> filterData) {
        this.filterData = filterData;
    }

    private ArrayList<Map.Entry<String, List<CallLogsResult>>> filterData;

    public int getFilterType() {
        return filterType;
    }

    public void setFilterType(int filterType) {
        this.filterType = filterType;
    }
}

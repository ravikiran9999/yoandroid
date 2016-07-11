package com.yo.android.model.dialer;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CallRatesData {

    @SerializedName("PACKAGERATELIST")
    private List<CallRateDetail> callRateDetailList;

    @SerializedName("TOTALRECORDS")
    private String totalRecords;

    public List<CallRateDetail> getCallRateDetailList() {
        return callRateDetailList;
    }

    public String getTotalRecords() {
        return totalRecords;
    }


    @Override
    public String toString() {
        return "ClassPojo [callRateDetailList = " + callRateDetailList + ", TOTALRECORDS = " + totalRecords + "]";
    }
}
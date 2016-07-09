package com.yo.android.model.dialer;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Ramesh on 9/7/16.
 */
public class CallRatesResponse {
    @SerializedName("DATA")
    private CallRatesData data;

    public CallRatesData getData() {
        return data;
    }
}

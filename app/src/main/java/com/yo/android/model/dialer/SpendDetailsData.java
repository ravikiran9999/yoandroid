package com.yo.android.model.dialer;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SpendDetailsData {
    @SerializedName("SUBSCRIBERSLIST")
    private List<SubscribersList> subscriberslist;

    public List<SubscribersList> getSubscriberslist() {
        return subscriberslist;
    }


    @Override
    public String toString() {
        return "ClassPojo [subscriberslist = " + subscriberslist + "]";
    }
}

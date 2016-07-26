package com.yo.android.model.dialer;

import java.util.List;

public class SpendDetailsData {
    private List<SubscribersList> subscriberslist;

    public List<SubscribersList> getSubscriberslist() {
        return subscriberslist;
    }


    @Override
    public String toString() {
        return "ClassPojo [subscriberslist = " + subscriberslist + "]";
    }
}

package com.yo.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.yo.android.ui.TabsHeaderActivity;
import com.yo.android.util.NetworkUtil;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private TabsHeaderActivity tabsHeaderActivity;

    public NetworkChangeReceiver(TabsHeaderActivity mContext) {
        tabsHeaderActivity = mContext;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        int status = NetworkUtil.getConnectivityStatusString(context);
        tabsHeaderActivity.showNetworkStatus(status);
    }
}

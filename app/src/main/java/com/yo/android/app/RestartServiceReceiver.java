package com.yo.android.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.yo.android.api.YoApi;
import com.yo.android.pjsip.YoSipService;
import com.yo.android.util.Constants;
import com.yo.android.util.ReCreateService;
import com.yo.dialer.CallExtras;

/**
 * Created by root on 26/4/17.
 */

public class RestartServiceReceiver extends BroadcastReceiver {

    private static final String TAG = "RestartServiceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive");
        //Toast.makeText(context, "OnReceive from killed service", Toast.LENGTH_SHORT).show();
        Intent service = new Intent(context, com.yo.dialer.YoSipService.class);
        service.setAction(CallExtras.REGISTER);
        context.startService(service);
    }
}

package com.yo.android.di;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.orion.android.common.logging.Logger;
import com.orion.android.common.logging.ParadigmExceptionHandler;

/**
 * Created by Ramesh on 3/7/16.
 */
public class AwsCallBackImpl implements AwsLogsCallBack {

    @Override
    public void onCalled(Context context, Intent intent) {
        Logger.init(context);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(intent),
                PendingIntent.FLAG_CANCEL_CURRENT);
        ParadigmExceptionHandler mParadigmException = new ParadigmExceptionHandler(context, pendingIntent);
        Thread.setDefaultUncaughtExceptionHandler(mParadigmException);
    }
}

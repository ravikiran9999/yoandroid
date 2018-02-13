package com.yo.android.di;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;

import com.orion.android.common.logging.Logger;
import com.orion.android.common.logging.ParadigmExceptionHandler;
import com.yo.android.BuildConfig;

/**
 * Created by Ramesh on 3/7/16.
 */
public class AwsCallBackImpl implements AwsLogsCallBack {

    @Override
    public void onCalled(Context context, Intent intent) {
        if (BuildConfig.AWS_LOGS_ENABLE) {
            Logger.init(context);
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(intent),
                    PendingIntent.FLAG_CANCEL_CURRENT);
            ParadigmExceptionHandler mParadigmException = new ParadigmExceptionHandler(context, pendingIntent);
            Thread.setDefaultUncaughtExceptionHandler(mParadigmException);
        }
    }
}

package com.yo.android.ui;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.orion.android.common.logger.Log;
import com.orion.android.common.logging.Logger;
import com.orion.android.common.logging.ParadigmExceptionHandler;
import com.orion.android.common.util.ResourcesHelper;
import com.orion.android.common.util.ToastFactory;
import com.yo.android.BuildConfig;
import com.yo.android.di.Injector;
import com.yo.android.util.ProgressDialogFactory;

import javax.inject.Inject;

/**
 * Created by ramesh on 12/3/16.
 */
public class BaseActivity extends AppCompatActivity {

    @Inject
    protected Log mLog;

    @Inject
    protected ToastFactory mToastFactory;

    @Inject
    protected ResourcesHelper mResourcesHelper;

    @Inject
    protected ProgressDialogFactory mProgressDialogFactory;

    protected Dialog mProgressDialog;
    private boolean enableBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.obtain(getApplication()).inject(this);
        if (BuildConfig.AWS_LOGS_ENABLE) {
            awsLogs();
        }
    }

    protected void enableBack() {
        enableBack = true;
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

    }

    /**
     * Initializing AWS logs
     * Sending Crash report when crash occurs
     */
    protected void awsLogs() {
        Logger.init(this);
        PendingIntent intent = PendingIntent.getActivity(getBaseContext(), 0, new Intent(getIntent()),
                PendingIntent.FLAG_CANCEL_CURRENT);
        ParadigmExceptionHandler mParadigmException = new ParadigmExceptionHandler(this, intent);
        Thread.setDefaultUncaughtExceptionHandler(mParadigmException);
    }

    /**
     * show progress dialog
     */
    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = mProgressDialogFactory.createTransparentDialog(this);
        }
        if (mProgressDialog != null)
            mProgressDialog.show();
    }

    /**
     * dismiss progress dialog
     */
    public void dismissProgressDialog() {
        if (mProgressDialog != null && !isFinishing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (enableBack) {
                    finish();
                    return true;
                }
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

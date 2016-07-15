package com.yo.android.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.orion.android.common.logger.Log;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.orion.android.common.util.ResourcesHelper;
import com.orion.android.common.util.ToastFactory;
import com.yo.android.di.AwsLogsCallBack;
import com.yo.android.di.Injector;
import com.yo.android.util.ProgressDialogFactory;
import com.yo.android.vox.VoxApi;
import com.yo.android.vox.VoxFactory;

import javax.inject.Inject;
import javax.inject.Named;

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

    @Inject
    AwsLogsCallBack mAwsLogsCallBack;

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;

    @Inject
    VoxFactory voxFactory;
    @Inject
    VoxApi.VoxService voxService;


    protected Dialog mProgressDialog;
    private boolean enableBack;

    private boolean isDestroyed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.obtain(getApplication()).inject(this);
        mAwsLogsCallBack.onCalled(getBaseContext(), getIntent());

    }

    protected void enableBack() {
        enableBack = true;
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroyed = true;
    }


    /**
     * show progress dialog
     */
    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = mProgressDialogFactory.createTransparentDialog(this);
        }
        if (mProgressDialog != null) {
            if (!mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
        }
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
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean hasDestroyed() {
        return isDestroyed;
    }
}

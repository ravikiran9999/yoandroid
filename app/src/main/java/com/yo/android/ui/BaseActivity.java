package com.yo.android.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.orion.android.common.logger.Log;
import com.orion.android.common.util.ActivityHelper;
import com.orion.android.common.util.ResourcesHelper;
import com.orion.android.common.util.ToastFactory;
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
    protected ActivityHelper mActivityHelper;

    @Inject
    protected ResourcesHelper mResourcesHelper;

    @Inject
    protected ProgressDialogFactory mProgressDialogFactory;

    protected Dialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.obtain(getApplication()).inject(this);
        //
//        if (BuildConfig.DEV == false) {
//            Fabric.with(this, new Crashlytics());
//        }
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = mProgressDialogFactory.createTransparentDialog(this);
        }
        if (mProgressDialog != null)
            mProgressDialog.show();
    }

    public void dismissProgressDialog() {
        if (mProgressDialog != null && !isFinishing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }

    }


}

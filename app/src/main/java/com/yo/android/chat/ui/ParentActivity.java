package com.yo.android.chat.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.orion.android.common.logger.Log;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.orion.android.common.util.ToastFactory;
import com.yo.android.di.Injector;
import com.yo.android.util.ProgressDialogFactory;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by rajesh on 13/9/16.
 */
public class ParentActivity extends AppCompatActivity {

    @Inject
    protected Log mLog;

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;

    @Inject
    protected ToastFactory mToastFactory;

    protected Dialog mProgressDialog;

    @Inject
    protected ProgressDialogFactory mProgressDialogFactory;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.obtain(getApplication()).inject(this);
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
}

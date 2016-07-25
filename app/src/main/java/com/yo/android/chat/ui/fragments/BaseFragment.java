package com.yo.android.chat.ui.fragments;


import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.orion.android.common.logger.Log;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.orion.android.common.util.ToastFactory;
import com.yo.android.R;
import com.yo.android.di.Injector;
import com.yo.android.ui.BottomTabsActivity;
import com.yo.android.util.ProgressDialogFactory;
import com.yo.android.vox.BalanceHelper;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * A simple {@link Fragment} subclass.
 */
public class BaseFragment extends Fragment {

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;
    @Inject
    protected ProgressDialogFactory mProgressDialogFactory;
    @Inject
    protected ToastFactory mToastFactory;
    @Inject
    protected Log mLog;
    @Inject
    protected BalanceHelper mBalanceHelper;

    protected Dialog mProgressDialog;

    public BaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.obtain(getActivity().getApplication()).inject(this);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView textView = new TextView(getActivity());
        textView.setText(R.string.hello_blank_fragment);
        return textView;
    }

    /**
     * show progress dialog
     */
    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = mProgressDialogFactory.createTransparentDialog(getActivity());
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
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;

        }
    }

    public boolean onBackPressHandle() {
        return false;
    }

    public void showOrHideTabs(boolean show) {
        if (getActivity() instanceof BottomTabsActivity) {
            ((BottomTabsActivity) getActivity()).showOrHideTabs(show);
        }
    }
}

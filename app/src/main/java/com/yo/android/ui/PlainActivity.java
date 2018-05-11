package com.yo.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;


import com.yo.android.R;
import com.yo.android.ui.fragments.GeneralWebViewFragment;
import com.yo.android.util.Constants;


import butterknife.BindView;
import butterknife.ButterKnife;

public class PlainActivity extends BaseActivity {

    public final static String KEY_ARGS = "args";

    @BindView(R.id.parent_container) FrameLayout parentContainer;
    /*@BindView(R.id.tool_bar)
    Toolbar toolbar;*/

    public static void start(@NonNull Activity activity,
                             String screenName, Bundle bundle) {
        Intent intent = createIntent(activity, screenName, bundle);
        activity.startActivity(intent);
    }

    private static Intent createIntent(@NonNull Activity activity,
                                       String screenName, Bundle bundle) {
        Intent intent = new Intent(activity, PlainActivity.class);
        intent.putExtra(Constants.TERMS_CONDITIONS, screenName);
        if (bundle != null) {
            intent.putExtra(KEY_ARGS, bundle);
        }
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plain);

        ButterKnife.bind(this);

        //initToolbar();

        if (savedInstanceState == null) {
            String termsConditionsScreen = getIntent().getStringExtra(Constants.TERMS_CONDITIONS);
            Bundle args = getIntent().getBundleExtra(KEY_ARGS);
            replaceFragment(this, GeneralWebViewFragment.newInstance(args), termsConditionsScreen);
            setTitleHideIcon(R.string.terms_and_conditions);
        }
    }

    @Override
    public void finish() {
        super.finish();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /*@Override
    public void showLoading(Dialog.OnCancelListener onCancelListener) {
        showLoadingProgressIndicator(parentContainer, onCancelListener);
    }

    @Override
    public void dismissLoading() {
        dismissLoadingProgressIndicator(parentContainer);
    }*/

    private static void replaceFragment(AppCompatActivity activity, Fragment fragment, String fragmentTag) {
        FragmentManager fm = activity.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        //applyFragmentTransitionAnimation(ft, direction);
        ft.replace(R.id.dynFragment, fragment, fragmentTag);
        ft.addToBackStack(fragmentTag);
        ft.commitAllowingStateLoss();
    }
}
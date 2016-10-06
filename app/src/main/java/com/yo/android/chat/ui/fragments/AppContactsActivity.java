package com.yo.android.chat.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.yo.android.R;
import com.yo.android.ui.BaseActivity;
import com.yo.android.util.Constants;

public class AppContactsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        YoContactsFragment yoContactsFragment = new YoContactsFragment();
        Bundle args = new Bundle();
        args.putBoolean(Constants.IS_CHAT_FORWARD, getIntent().hasExtra(Constants.IS_CHAT_FORWARD));
        if (getIntent().getParcelableArrayListExtra(Constants.CHAT_FORWARD) != null) {

            args.putParcelableArrayList(Constants.CHAT_FORWARD, getIntent().getParcelableArrayListExtra(Constants.CHAT_FORWARD));
        }
        yoContactsFragment.setArguments(args);
        getSupportFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, yoContactsFragment)
                .commit();
        enableBack();


    }
}

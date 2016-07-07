package com.yo.android.chat.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.yo.android.R;
import com.yo.android.ui.BaseActivity;

public class AppContactsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        YoContactsFragment yoContactsFragment = new YoContactsFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, yoContactsFragment)
                .commit();
        enableBack();
    }
}

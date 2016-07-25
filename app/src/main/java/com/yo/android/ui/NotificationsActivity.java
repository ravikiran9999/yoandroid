package com.yo.android.ui;

import android.os.Bundle;

import com.yo.android.R;

public class NotificationsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications_list_item);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String title = "Notifications";

        getSupportActionBar().setTitle(title);
    }
}

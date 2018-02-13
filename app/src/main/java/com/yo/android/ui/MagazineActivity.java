package com.yo.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.yo.android.R;

public class MagazineActivity extends BaseActivity {

    public static void start(Activity activity) {
        Intent intent = new Intent(activity, MagazineActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magazine);
    }
}

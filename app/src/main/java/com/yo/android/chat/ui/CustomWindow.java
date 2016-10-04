package com.yo.android.chat.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.yo.android.R;
import com.yo.android.ui.BaseActivity;

public class CustomWindow extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_custom_window);
        //Set the titlebar layout
        this.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
    }
}

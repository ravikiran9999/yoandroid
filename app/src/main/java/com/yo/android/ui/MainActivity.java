package com.yo.android.ui;

import android.os.Bundle;

import com.yo.android.R;
import com.yo.android.util.RestApi;


public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new RestApi().upload(this);

    }
}

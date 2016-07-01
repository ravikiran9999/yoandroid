package com.yo.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.chat.ui.LoginActivity;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Ramesh on 30/6/16.
 */
public class SplashScreenActivity extends BaseActivity {

    @Inject
    @Named("login")
    PreferenceEndPoint preferenceEndPoint;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if(!preferenceEndPoint.getStringPreference("phone").isEmpty()) {
            startActivity(new Intent(this, NavigationDrawerActivity.class));
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
        //startActivity(new Intent(this, NavigationDrawerActivity.class));

        finish();
    }
}

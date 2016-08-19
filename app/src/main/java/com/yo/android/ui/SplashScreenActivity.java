package com.yo.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.ImageView;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.chat.ui.LoginActivity;
import com.yo.android.pjsip.YoSipService;
import com.yo.android.util.Constants;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Ramesh on 30/6/16.
 */
public class SplashScreenActivity extends BaseActivity {
    private final static String TAG = "SplashScreenActivity";
    @Inject
    @Named("login")
    PreferenceEndPoint preferenceEndPoint;
    private Handler mHandler = new Handler();
    private static final long DURATION = 1000L;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.ic_splash);
        mHandler.postDelayed(runnable, DURATION);
        startService(new Intent(this, YoSipService.class));
    }


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!TextUtils.isEmpty(preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER))) {
                //1.Profile
                //2.Follow
                //3. Home
                if (preferenceEndPoint.getBooleanPreference(Constants.ENABLE_PROFILE_SCREEN)) {
                    startActivity(new Intent(SplashScreenActivity.this, UpdateProfileActivity.class));
                } else if (preferenceEndPoint.getBooleanPreference(Constants.ENABLE_FOLLOW_TOPICS_SCREEN)) {
                    startActivity(new Intent(SplashScreenActivity.this, FollowMoreTopicsActivity.class));
                } else {
                    startActivity(new Intent(SplashScreenActivity.this, BottomTabsActivity.class));
                }
            } else {
                startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
            }
            finish();
        }
    };
}

package com.yo.android.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.ImageView;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.WebserviceUsecase;
import com.yo.android.api.ApiCallback;
import com.yo.android.chat.ui.LoginActivity;
import com.yo.android.model.Lock;
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
    @Inject
    WebserviceUsecase webserviceUsecase;
    private Handler mHandler = new Handler();
    private static final long DURATION = 1000L;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new SplashScreen().execute();
        //startService(new Intent(this, YoSipService.class));

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

    private class SplashScreen extends AsyncTask<Void, Void, Void> {
        String phoneNumber;
        boolean enableProfileScreen;
        boolean enableFollowTopicsScreen;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setContentView(R.layout.activity_splash);
        }

        @Override
        protected Void doInBackground(Void... params) {
            phoneNumber = preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER);
            enableProfileScreen = preferenceEndPoint.getBooleanPreference(Constants.ENABLE_PROFILE_SCREEN);
            enableFollowTopicsScreen = preferenceEndPoint.getBooleanPreference(Constants.ENABLE_FOLLOW_TOPICS_SCREEN);


            /*ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageResource(R.drawable.ic_splash);*/

            if (TextUtils.isEmpty(phoneNumber) || enableProfileScreen || enableFollowTopicsScreen) {
                try {
                    Thread.sleep(DURATION);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                webserviceUsecase.appStatus(new ApiCallback<Lock>() {
                    @Override
                    public void onResult(Lock result) {
                        return;
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (!TextUtils.isEmpty(phoneNumber)) {
                //1.Profile
                //2.Follow
                //3. Home
                if (enableProfileScreen) {
                    startActivity(new Intent(SplashScreenActivity.this, UpdateProfileActivity.class));
                } else if (enableFollowTopicsScreen) {
                    startActivity(new Intent(SplashScreenActivity.this, FollowMoreTopicsActivity.class));
                } else {
                    startActivity(new Intent(SplashScreenActivity.this, BottomTabsActivity.class));
                }
            } else {
                startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
            }
            finish();
            super.onPostExecute(aVoid);
        }
    }
}

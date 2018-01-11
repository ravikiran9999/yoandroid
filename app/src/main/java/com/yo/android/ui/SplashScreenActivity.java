package com.yo.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.usecase.WebserviceUsecase;
import com.yo.android.api.ApiCallback;
import com.yo.android.chat.ui.LoginActivity;
import com.yo.android.model.Lock;
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
    //private Handler mHandler = new Handler();
    private static final long DURATION = 1000L;
    private static Activity mActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new SplashScreen(this, preferenceEndPoint, webserviceUsecase).execute();
        //startService(new Intent(this, YoSipService.class));

    }


    //Remove it
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
                    if (!BuildConfig.NEW_FOLLOW_MORE_TOPICS) {
                        startActivity(new Intent(SplashScreenActivity.this, FollowMoreTopicsActivity.class));
                    } else {
                        startActivity(new Intent(SplashScreenActivity.this, NewFollowMoreTopicsActivity.class));
                    }
                } else {
                    startActivity(new Intent(SplashScreenActivity.this, BottomTabsActivity.class));
                }
            } else {
                startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
            }
            finish();
        }
    };

    private static class SplashScreen extends AsyncTask<Void, Void, Void> {
        String phoneNumber;
        boolean enableProfileScreen;
        boolean enableFollowTopicsScreen;

        private PreferenceEndPoint mPreferenceEndPoint;
        private WebserviceUsecase mWebserviceUsecase;


        public SplashScreen(Activity activity, PreferenceEndPoint preferenceEndPoint, WebserviceUsecase webserviceUsecase) {
            mPreferenceEndPoint = preferenceEndPoint;
            mWebserviceUsecase = webserviceUsecase;
            mActivity = activity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActivity.setContentView(R.layout.activity_splash);
        }

        @Override
        protected Void doInBackground(Void... params) {
            phoneNumber = mPreferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER);
            enableProfileScreen = mPreferenceEndPoint.getBooleanPreference(Constants.ENABLE_PROFILE_SCREEN);
            enableFollowTopicsScreen = mPreferenceEndPoint.getBooleanPreference(Constants.ENABLE_FOLLOW_TOPICS_SCREEN);


            /*ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageResource(R.drawable.ic_splash);*/

            if (TextUtils.isEmpty(phoneNumber) || enableProfileScreen || enableFollowTopicsScreen) {
                try {
                    Thread.sleep(DURATION);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                mWebserviceUsecase.appStatus(new ApiCallback<Lock>() {
                    @Override
                    public void onResult(Lock result) {
                        return;
                    }

                    @Override
                    public void onFailure(String message) {

                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                if (!TextUtils.isEmpty(phoneNumber)) {
                    //1.Profile
                    //2.Follow
                    //3. Home
                    if (enableProfileScreen) {
                        mActivity.startActivity(new Intent(mActivity, UpdateProfileActivity.class));
                    } else if (enableFollowTopicsScreen) {
                        if (!BuildConfig.NEW_FOLLOW_MORE_TOPICS) {
                            mActivity.startActivity(new Intent(mActivity, FollowMoreTopicsActivity.class));
                        } else {
                            mActivity.startActivity(new Intent(mActivity, NewFollowMoreTopicsActivity.class));
                        }
                    } else {
                        mActivity.startActivity(new Intent(mActivity, BottomTabsActivity.class));
                    }
                } else {
                    mActivity.startActivity(new Intent(mActivity, LoginActivity.class));
                }
                mActivity.finish();
            } finally {
                if (mActivity != null) {
                    mActivity = null;
                }
            }
            super.onPostExecute(aVoid);
        }


    }

    @Override
    protected void onDestroy() {
        if (mActivity != null) {
            mActivity = null;
        }
        super.onDestroy();

    }
}

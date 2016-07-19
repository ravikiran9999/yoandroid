package com.yo.android.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.widget.ImageView;

import com.j256.ormlite.stmt.query.In;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.chat.firebase.FirebaseService;
import com.yo.android.chat.firebase.MyServiceConnection;
import com.yo.android.chat.ui.LoginActivity;
import com.yo.android.util.Constants;
import com.yo.android.vox.VoxApi;
import com.yo.android.vox.VoxFactory;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ramesh on 30/6/16.
 */
public class SplashScreenActivity extends BaseActivity {

    @Inject
    @Named("login")
    PreferenceEndPoint preferenceEndPoint;
    @Inject
    VoxFactory voxFactory;
    @Inject
    VoxApi.VoxService voxService;
    private Handler mHandler = new Handler();
    private static final long DURATION = 1000L;

    @Inject
    MyServiceConnection myServiceConnection;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.ic_splash);
        mHandler.postDelayed(runnable, DURATION);
        try {
            TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String mPhoneNumber = tMgr.getLine1Number();
            String zipCode = getCountryZipCode();
            mLog.e("Splash", "Phone number %s zipcode: %s ", mPhoneNumber, zipCode);
        } catch (Exception e) {
            mLog.w("Splash", e);
        }
//        testVox();

        // firebase service

        if(!myServiceConnection.isServiceConnection()) {
            Intent intent = new Intent(this, FirebaseService.class);
            startService(intent);

            bindService(intent, myServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (preferenceEndPoint.getBooleanPreference(Constants.LOGED_IN)) {
                startActivity(new Intent(SplashScreenActivity.this, BottomTabsActivity.class));
            } else {
                startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
            }
            finish();
        }
    };

    public void testVox() {
        //Debug purpose
        voxService.executeAction(voxFactory.verifyOTP("8341569102")).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public String getCountryZipCode() {

        String CountryID = "";
        String CountryZipCode = "";

        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        //getNetworkCountryIso
        CountryID = manager.getSimCountryIso().toUpperCase();
        String[] rl = this.getResources().getStringArray(R.array.CountryCodes);
        for (int i = 0; i < rl.length; i++) {
            String[] g = rl[i].split(",");
            if (g[1].trim().equals(CountryID.trim())) {
                CountryZipCode = g[0];
                break;
            }
        }
        return CountryID;
    }
}

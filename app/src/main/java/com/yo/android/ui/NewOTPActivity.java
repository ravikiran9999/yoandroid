package com.yo.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.google.gson.internal.LinkedTreeMap;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.chat.ui.LoginActivity;
import com.yo.android.chat.ui.fragments.OTPFragment;
import com.yo.android.model.Response;
import com.yo.android.ui.fragments.NewOTPFragment;
import com.yo.android.util.Constants;

import java.net.SocketTimeoutException;

import retrofit2.Call;
import retrofit2.Callback;


public class NewOTPActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NewOTPFragment newOTPFragment = new NewOTPFragment();
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, newOTPFragment)
                .commit();
        enableBack();
    }

    public void callLoginService(final String phoneNumber) {
        showProgressDialog();
        String countryCode = preferenceEndPoint.getStringPreference(Constants.COUNTRY_CODE_FROM_SIM);
        String type = BuildConfig.ORIGINAL_SMS_VERIFICATION ? "original" : "dummy";
        //yoService.loginUserAPI(phoneNumber, type, countryCode).enqueue(new Callback<Response>() {
        yoService.loginUserAPI(countryCode + phoneNumber, type).enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                try {
                    dismissProgressDialog();
                    if (response.isSuccessful()) {
                        Response response1 = response.body();
                        if (response1 != null) {
                            boolean isNewUser = (boolean) ((LinkedTreeMap) response1.getData()).get("isNewUser");
                            String userId = (String) ((LinkedTreeMap) response1.getData()).get("id");
                            preferenceEndPoint.saveStringPreference(Constants.USER_ID, userId);
                            preferenceEndPoint.saveBooleanPreference("isNewUser", isNewUser);
                            boolean balanceAdded = (boolean) ((LinkedTreeMap) response1.getData()).get("balanceAdded");
                            preferenceEndPoint.saveBooleanPreference("balanceAdded", balanceAdded);
                        }
                        /*if(!BuildConfig.NEW_OTP_SCREEN) {
                            OTPFragment otpFragment = new OTPFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString(Constants.PHONE_NUMBER, phoneNumber);
                            otpFragment.setArguments(bundle);
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.add(android.R.id.content, otpFragment, FRAGMENT_TAG);
                            transaction.disallowAddToBackStack();
                            transaction.commit();
                        } else {
                            Intent intent = new Intent(LoginActivity.this, NewOTPActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString(Constants.PHONE_NUMBER, phoneNumber);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }*/
                    } else {
                        mToastFactory.showToast("Please enter valid phone number.");
                    }
                } catch (Exception e) {

                }

            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                if (!mHelper.isConnected() || t instanceof SocketTimeoutException) {
                    mToastFactory.showToast(getResources().getString(R.string.connectivity_network_settings));
                } else {
                    mToastFactory.showToast(t.getLocalizedMessage());
                }
                dismissProgressDialog();
            }
        });

    }
}

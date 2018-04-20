package com.yo.android.ui;

import android.os.Bundle;
import com.google.gson.internal.LinkedTreeMap;
import com.yo.android.BuildConfig;
import com.yo.android.R;
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

        setTitleHideIcon(R.string.verify_phone_number);

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
                            boolean balanceAdded = (boolean) ((LinkedTreeMap) response1.getData()).get("balanceAdded");
                            preferenceEndPoint.saveStringPreference(Constants.USER_ID, userId);
                            preferenceEndPoint.saveBooleanPreference("isNewUser", isNewUser);
                            preferenceEndPoint.saveBooleanPreference("balanceAdded", balanceAdded);
                        }
                    } else {
                        mToastFactory.showToast(R.string.enter_mobile_number_error);
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

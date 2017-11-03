package com.yo.android.usecase;


import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.api.ApiCallback;
import com.yo.android.api.YoApi;
import com.yo.android.model.PackageDenomination;
import com.yo.android.model.TransferBalanceDenomination;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PackageDenominationsUsecase {

    private static final String noWifiError = "Please check your wifi connection";

    @Inject
    YoApi.YoService yoService;
    @Inject
    @Named("login")
    PreferenceEndPoint loginPrefs;

    public void getPackageDenominationsUsecase(final ApiCallback<ArrayList<PackageDenomination>> packageCallback) {
        String accessToken = loginPrefs.getStringPreference("access_token");
        Call<List<PackageDenomination>> call = yoService.giftPackageDenominationApi(accessToken);
        call.enqueue(new Callback<List<PackageDenomination>>() {
            @Override
            public void onResponse(Call<List<PackageDenomination>> call, Response<List<PackageDenomination>> response) {
                if (response.body() != null && response.code() == 200) {
                    ArrayList<PackageDenomination> denominations = new ArrayList<>(response.body());

                    Collections.sort(denominations, new Comparator<PackageDenomination>() {
                        @Override
                        public int compare(PackageDenomination lhs, PackageDenomination rhs) {
                            int x = Integer.valueOf(lhs.getPackage());
                            int y = Integer.valueOf(rhs.getPackage());

                            return x - y;
                        }
                    });
                    packageCallback.onResult(denominations);
                } else {
                    errorMessage(response, packageCallback);
                }

            }

            @Override
            public void onFailure(Call<List<PackageDenomination>> call, Throwable t) {
                if(t != null && t instanceof ConnectException) {
                    packageCallback.onFailure(noWifiError);
                } else {
                    packageCallback.onFailure(t.getMessage());
                }
            }
        });
    }

    private void errorMessage(Response<List<PackageDenomination>> response, final ApiCallback<ArrayList<PackageDenomination>> mPackageCallback) {
        try {
            JSONObject jsonObjectError = new JSONObject(response.errorBody().string());
            int code = jsonObjectError.getInt("code");
            if (code == 422 || code == 404) {
                mPackageCallback.onFailure(jsonObjectError.getString("data"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

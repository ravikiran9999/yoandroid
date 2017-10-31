package com.yo.android.usecase;


import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.api.ApiCallback;
import com.yo.android.api.YoApi;
import com.yo.android.model.TransferBalanceDenomination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DenominationsUsecase {

    @Inject
    YoApi.YoService yoService;
    @Inject
    @Named("login")
    PreferenceEndPoint loginPrefs;

    public void getDenominations(final ApiCallback<ArrayList<TransferBalanceDenomination>> balanceTransferCallback) {
        String accessToken = loginPrefs.getStringPreference("access_token");
        Call<List<TransferBalanceDenomination>> call = yoService.transferBalanceDenominationAPI(accessToken);
        call.enqueue(new Callback<List<TransferBalanceDenomination>>() {
            @Override
            public void onResponse(Call<List<TransferBalanceDenomination>> call, Response<List<TransferBalanceDenomination>> response) {
                if (response.body() != null && response.code() == 200) {
                    ArrayList<TransferBalanceDenomination> denominations = new ArrayList<>(response.body());

                    Collections.sort(denominations, new Comparator<TransferBalanceDenomination>() {
                        @Override
                        public int compare(TransferBalanceDenomination lhs, TransferBalanceDenomination rhs) {
                            int x = Integer.valueOf(lhs.getDenomination());
                            int y = Integer.valueOf(rhs.getDenomination());

                            return x - y;
                        }
                    });
                    balanceTransferCallback.onResult(denominations);
                } else if (response.code() == 404 || response.code() == 422) {
                    balanceTransferCallback.onFailure(response.message());
                } else {
                    balanceTransferCallback.onFailure(response.message());
                }

            }

            @Override
            public void onFailure(Call<List<TransferBalanceDenomination>> call, Throwable t) {
                balanceTransferCallback.onFailure(t.getMessage());
            }
        });
    }


}
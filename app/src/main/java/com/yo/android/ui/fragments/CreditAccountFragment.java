package com.yo.android.ui.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.inapp.UnManageInAppPurchaseActivity;
import com.yo.android.util.Constants;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Ramesh on 24/7/16.
 */
public class CreditAccountFragment extends BaseFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Bind(R.id.txt_balance)
    TextView txt_balance;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceEndPoint.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        preferenceEndPoint.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return LayoutInflater.from(getActivity()).inflate(R.layout.activity_credit_account, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String balance = preferenceEndPoint.getStringPreference(Constants.CURRENT_BALANCE, "2.0");
        txt_balance.setText(String.format("$%s", balance));
    }

    @OnClick(R.id.btn1)
    public void onBtnClick() {
        final Intent intent = new Intent(getActivity(), UnManageInAppPurchaseActivity.class);
        intent.putExtra("sku", "com.yo.product.credits.FIVE");
        intent.putExtra("price", 5f);
        final String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        intent.putExtra(Constants.USER_ID, userId);
        startActivity(intent);
    }

    @OnClick(R.id.btn2)
    public void onBtnClick2() {
        final Intent intent = new Intent(getActivity(), UnManageInAppPurchaseActivity.class);
        intent.putExtra("sku", "com.yo.product.credits.TEN");
        final String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        intent.putExtra(Constants.USER_ID, userId);
        intent.putExtra("price", 10f);
        startActivity(intent);
    }

    @OnClick(R.id.btn3)
    public void onBtnClick3() {
        final Intent intent = new Intent(getActivity(), UnManageInAppPurchaseActivity.class);
        intent.putExtra("sku", "com.yo.product.credits.FIFTEEN");
        intent.putExtra("price", 15f);
        final String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        intent.putExtra(Constants.USER_ID, userId);
        startActivity(intent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Constants.CURRENT_BALANCE)) {
            String balance = preferenceEndPoint.getStringPreference(Constants.CURRENT_BALANCE, "2.0");
            txt_balance.setText(String.format("$%s", balance));
        }
    }
}

package com.yo.android.ui.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.MoreListAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.ui.LoginActivity;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.inapp.UnManageInAppPurchaseActivity;
import com.yo.android.model.MoreData;
import com.yo.android.pjsip.YoSipService;
import com.yo.android.provider.YoAppContactContract;
import com.yo.android.ui.MoreSettingsActivity;
import com.yo.android.ui.NotificationsActivity;
import com.yo.android.ui.TabsHeaderActivity;
import com.yo.android.util.Constants;
import com.yo.android.voip.VoipConstants;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ramesh on 24/7/16.
 */
public class CreditAccountFragment extends BaseFragment implements SharedPreferences.OnSharedPreferenceChangeListener, AdapterView.OnItemClickListener {

    @Bind(R.id.txt_balance)
    TextView txt_balance;

    private MoreListAdapter menuAdapter;
    @Inject
    YoApi.YoService yoService;

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
        String balance = mBalanceHelper.getCurrentBalance();
        String currencySymbol = mBalanceHelper.getCurrencySymbol();
        txt_balance.setText(String.format("%s%s", currencySymbol, balance));
        prepareCreditAccountList();
    }

    @OnClick(R.id.btn1)
    public void onBtnClick() {
        final Intent intent = new Intent(getActivity(), UnManageInAppPurchaseActivity.class);
        intent.putExtra("sku", "com.yo.products.credit.FIVE");
        intent.putExtra("price", 5f);
        final String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        intent.putExtra(Constants.USER_ID, userId);
        startActivity(intent);
    }

    @OnClick(R.id.btn2)
    public void onBtnClick2() {
        final Intent intent = new Intent(getActivity(), UnManageInAppPurchaseActivity.class);
        intent.putExtra("sku", "com.yo.products.credit.TEN");
        final String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        intent.putExtra(Constants.USER_ID, userId);
        intent.putExtra("price", 10f);
        startActivity(intent);
    }

    @OnClick(R.id.btn3)
    public void onBtnClick3() {
        final Intent intent = new Intent(getActivity(), UnManageInAppPurchaseActivity.class);
        intent.putExtra("sku", "com.yo.products.credit.FIFTEEN");
        intent.putExtra("price", 15f);
        final String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        intent.putExtra(Constants.USER_ID, userId);
        startActivity(intent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Constants.CURRENT_BALANCE)) {
            String balance = mBalanceHelper.getCurrentBalance();
            String currencySymbol = mBalanceHelper.getCurrencySymbol();
            txt_balance.setText(String.format("%s%s", currencySymbol, balance));
        }
    }

    /**
     * Prepares the Credit Account list
     */
    public void prepareCreditAccountList() {
        menuAdapter = new MoreListAdapter(getActivity()) {
            @Override
            public int getLayoutId() {
                return R.layout.item_with_options;
            }
        };
        ListView menuListView = (ListView) getView().findViewById(R.id.lv_settings);
        menuAdapter.addItems(getMenuList());

        menuListView.setAdapter(menuAdapter);
        menuListView.setOnItemClickListener(this);
    }

    /**
     * Creates the Credit Account list
     *
     * @return
     */
    public List<MoreData> getMenuList() {

        List<MoreData> menuDataList = new ArrayList<>();
        menuDataList.add(new MoreData("Voucher Recharge", true));
        return menuDataList;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String name = ((MoreData) parent.getAdapter().getItem(position)).getName();

        if (name.equalsIgnoreCase("Voucher Recharge")) {

            showVoucherDialog();
        }
    }

    public void showVoucherDialog() {

        if (getActivity() != null) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            final View view = layoutInflater.inflate(R.layout.custom_voucher, null);
            builder.setView(view);

            Button yesBtn = (Button) view.findViewById(R.id.yes_btn);
            Button noBtn = (Button) view.findViewById(R.id.no_btn);
            TextView namePhoneText = (TextView) view.findViewById(R.id.dialog_content);
            final EditText voucherNumberEdit = (EditText) view.findViewById(R.id.dialog_content_edit);

            final String userName = preferenceEndPoint.getStringPreference(Constants.USER_NAME);
            String phonNumber = preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER);

            if(userName != null) {
                namePhoneText.setText(userName);
            }else if(phonNumber != null) {
                namePhoneText.setText(phonNumber);
            }else {
                namePhoneText.setText("Unknown");
            }

            final AlertDialog alertDialog = builder.create();
            alertDialog.setCancelable(true);
            alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            alertDialog.show();

            yesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String voucherNumber = voucherNumberEdit.getText().toString();

                    if (!TextUtils.isEmpty(voucherNumber.trim())) {
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.voucherRechargeAPI(accessToken, voucherNumber).enqueue(new Callback<com.yo.android.model.Response>() {
                            @Override
                            public void onResponse(Call<com.yo.android.model.Response> call, Response<com.yo.android.model.Response> response) {

                                if (response.isSuccessful()) {
                                    if (response.code() == 200) {
                                        try {
                                            int statusCode = Integer.parseInt(response.body().getCode());
                                            switch (statusCode) {
                                                case 200:
                                                    mToastFactory.showToast(R.string.voucher_recharge_successful);
                                                    alertDialog.dismiss();
                                                    mBalanceHelper.checkBalance();
                                                    break;
                                                case 600:
                                                    mToastFactory.showToast(R.string.invalid_voucher);
                                                    break;
                                                case 601:
                                                    mToastFactory.showToast(R.string.invalid_pin_request);
                                                    break;
                                                case 602:
                                                    mToastFactory.showToast(R.string.voucher_used);
                                                    break;
                                                case 603:
                                                    mToastFactory.showToast(R.string.voucher_expired);
                                                    break;
                                                case 604:
                                                    mToastFactory.showToast(R.string.unsuccessful_recharge);
                                                    break;
                                                default:
                                                    mToastFactory.showToast(R.string.invalid_voucher);
                                                    break;
                                            }
                                        } catch (ClassCastException e) {
                                            mLog.d("ClassCastException", e.getMessage());
                                        }
                                    } else {
                                        mToastFactory.showToast(R.string.invalid_voucher);
                                    }

                                } else {
                                    mToastFactory.showToast(R.string.invalid_voucher);
                                }

                            }

                            @Override
                            public void onFailure(Call<com.yo.android.model.Response> call, Throwable t) {

                                mToastFactory.showToast(R.string.invalid_voucher);

                            }
                        });
                    } else {
                        mToastFactory.showToast("Please enter a Voucher Number");
                    }

                }
            });

            noBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });
        }
    }

}

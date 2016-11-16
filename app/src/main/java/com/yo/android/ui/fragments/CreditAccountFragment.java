package com.yo.android.ui.fragments;

import android.app.Activity;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.MoreListAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.ui.LoginActivity;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.helpers.MenuViewHolder;
import com.yo.android.inapp.UnManageInAppPurchaseActivity;
import com.yo.android.model.MoreData;
import com.yo.android.pjsip.YoSipService;
import com.yo.android.provider.YoAppContactContract;
import com.yo.android.ui.MoreSettingsActivity;
import com.yo.android.ui.NotificationsActivity;
import com.yo.android.ui.TabsHeaderActivity;
import com.yo.android.ui.TransferBalanceSelectContactActivity;
import com.yo.android.util.Constants;
import com.yo.android.voip.VoipConstants;
import com.yo.android.BuildConfig;


import java.text.DecimalFormat;
import java.text.NumberFormat;
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

    private static final String TAG = CreditAccountFragment.class.getSimpleName();
    @Bind(R.id.txt_balance)
    TextView txt_balance;

    private MoreListAdapter menuAdapter;
    @Inject
    YoApi.YoService yoService;

    private String balance;

    private static final int OPEN_ADD_BALANCE_RESULT = 1000;


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
        balance = mBalanceHelper.getCurrentBalance();
        String currencySymbol = mBalanceHelper.getCurrencySymbol();
        NumberFormat formatter = new DecimalFormat("#0.00");
        txt_balance.setText(String.format("%s%s", currencySymbol, balance));
        prepareCreditAccountList();
    }


    /**
     * Close current activity becasuse once balance is added it should navitate to calling activity.
     *
     * @param data
     */
    private void closeActivityAddBalance(int resultcode, Intent data) {
        if (resultcode == Activity.RESULT_CANCELED) {
            return;
        }
        if (getArguments() != null && getArguments().getBoolean(Constants.OPEN_ADD_BALANCE, false)) {
            getActivity().setResult(resultcode);
            getActivity().finish();
        }
    }

    /*@OnClick(R.id.btn1)
    public void onBtnClick() {
        final Intent intent = new Intent(getActivity(), UnManageInAppPurchaseActivity.class);
        intent.putExtra("sku", "com.yo.products.credit.FIVE");
        intent.putExtra("price", 5f);
        final String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        intent.putExtra(Constants.USER_ID, userId);
        startActivityForResult(intent, OPEN_ADD_BALANCE_RESULT);
    }*/

    private void addGooglePlayBalance(String sku, float price) {
        final Intent intent = new Intent(getActivity(), UnManageInAppPurchaseActivity.class);
        intent.putExtra("sku", sku);// "com.yo.products.credit.TEN"
        intent.putExtra("price", price);//10f
        final String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        intent.putExtra(Constants.USER_ID, userId);
        startActivityForResult(intent, OPEN_ADD_BALANCE_RESULT);
    }

    /*@OnClick(R.id.btn2)
    public void onBtnClick2() {


    }*/

    /*@OnClick(R.id.btn3)
    public void onBtnClick3() {
        final Intent intent = new Intent(getActivity(), UnManageInAppPurchaseActivity.class);
        intent.putExtra("sku", "com.yo.products.credit.FIFTEEN");
        intent.putExtra("price", 15f);
        final String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        intent.putExtra(Constants.USER_ID, userId);
        startActivityForResult(intent, OPEN_ADD_BALANCE_RESULT);
    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mBalanceHelper != null && requestCode == 11 && resultCode == Activity.RESULT_OK) {
            showProgressDialog();
            mBalanceHelper.checkBalance(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    dismissProgressDialog();
                    try {
                        DecimalFormat df = new DecimalFormat("0.000");
                        String format = df.format(Double.valueOf(mBalanceHelper.getCurrentBalance()));
                        preferenceEndPoint.saveStringPreference(Constants.CURRENT_BALANCE, format);
                    } catch (IllegalArgumentException e) {
                        mLog.w(TAG, "getCurrentBalance", e);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    dismissProgressDialog();

                }
            });
        } else if (mBalanceHelper != null && resultCode == Activity.RESULT_OK) {
            try {
                DecimalFormat df = new DecimalFormat("0.000");
                String format = df.format(Double.valueOf(mBalanceHelper.getCurrentBalance()));
                preferenceEndPoint.saveStringPreference(Constants.CURRENT_BALANCE, format);
            } catch (IllegalArgumentException e) {
                mLog.w(TAG, "getCurrentBalance", e);
            }
        }
        /*else {
            mToastFactory.showToast(getString(R.string.failed_add_balance));
        }*/
        closeActivityAddBalance(resultCode, data);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Constants.CURRENT_BALANCE)) {
            balance = mBalanceHelper.getCurrentBalance();
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

            @Override
            public void bindView(int position, MenuViewHolder holder, MoreData item) {
                View viewById = holder.getRootView().findViewById(R.id.add_google_play_items);
                viewById.findViewById(R.id.btn1).setOnClickListener(payBtnListener);
                viewById.findViewById(R.id.btn2).setOnClickListener(payBtnListener);
                viewById.findViewById(R.id.btn3).setOnClickListener(payBtnListener);
                if (position == 0) {
                    viewById.setVisibility(View.VISIBLE);
                } else {
                    viewById.setVisibility(View.GONE);
                }
                super.bindView(position, holder, item);
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
        menuDataList.add(new MoreData(getString(R.string.add_balance_from_google_play), false));
        menuDataList.add(new MoreData(getString(R.string.add_balance_from_voucher), true));
        if (getArguments() == null) {
            menuDataList.add(new MoreData(getString(R.string.transfer_balance), true));
        }

        return menuDataList;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String name = ((MoreData) parent.getAdapter().getItem(position)).getName();

        if (name.equalsIgnoreCase(getString(R.string.add_balance_from_voucher))) {
            showVoucherDialog();
        } else if (name.equalsIgnoreCase(getString(R.string.transfer_balance))) {
            //mToastFactory.showToast("Need to implement");
            String balance = mBalanceHelper.getCurrentBalance();
            String currencySymbol = mBalanceHelper.getCurrencySymbol();
            Intent intent = new Intent(getActivity(), TransferBalanceSelectContactActivity.class);
            intent.putExtra("balance", balance);
            intent.putExtra("currencySymbol", currencySymbol);
            startActivityForResult(intent, 11);
        }
    }

    private OnClickListener payBtnListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn1) {
                if (Double.valueOf(balance) < 5.000) {
                    addGooglePlayBalance("com.yo.products.credit.FIVE", 5f);
                } else {
                    mToastFactory.showToast(R.string.disabled);
                }
            } else if (v.getId() == R.id.btn2) {
                if (!BuildConfig.DISABLE_ADD_BALANCE) {
                    addGooglePlayBalance("com.yo.products.credit.TEN", 10f);
                } else {
                    mToastFactory.showToast(R.string.disabled);

                }

            } else if (v.getId() == R.id.btn3) {
                if (!BuildConfig.DISABLE_ADD_BALANCE) {
                    addGooglePlayBalance("com.yo.products.credit.FIFTEEN", 15f);
                } else {
                    mToastFactory.showToast(R.string.disabled);
                }
            }
        }
    };

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

            if (userName != null) {
                namePhoneText.setText(userName);
            } else if (phonNumber != null) {
                namePhoneText.setText(phonNumber);
            } else {
                namePhoneText.setText("Unknown");
            }

            final AlertDialog alertDialog = builder.create();
            alertDialog.setCancelable(true);
            alertDialog.setCanceledOnTouchOutside(true);
            alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            alertDialog.show();

            yesBtn.setOnClickListener(new OnClickListener() {
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
                                                    mBalanceHelper.checkBalance(new Callback<ResponseBody>() {
                                                        @Override
                                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                            closeActivityAddBalance(Activity.RESULT_OK, null);
                                                        }

                                                        @Override
                                                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                            closeActivityAddBalance(Activity.RESULT_CANCELED, null);
                                                        }
                                                    });
                                                    break;
                                                case 600:
                                                    showMessage(R.string.invalid_voucher);
                                                    break;
                                                case 601:
                                                    showMessage(R.string.invalid_pin_request);
                                                    break;
                                                case 602:
                                                    showMessage(R.string.voucher_used);
                                                    break;
                                                case 603:
                                                    showMessage(R.string.voucher_expired);
                                                    break;
                                                case 604:
                                                    showMessage(R.string.unsuccessful_recharge);
                                                    break;
                                                default:
                                                    showMessage(R.string.invalid_voucher);
                                                    break;
                                            }
                                        } catch (ClassCastException e) {
                                            mLog.d("ClassCastException", e.getMessage());
                                        }
                                    } else {
                                        showMessage(R.string.invalid_voucher);
                                    }

                                } else {
                                    showMessage(R.string.invalid_voucher);
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

            noBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });
        }
    }

    private void showMessage(int resoureId) {
        mToastFactory.showToast(resoureId);
        closeActivityAddBalance(Activity.RESULT_CANCELED, null);
    }

}

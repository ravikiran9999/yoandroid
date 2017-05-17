package com.yo.android.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Network;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
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
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.MoreListAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.ui.LoginActivity;
import com.yo.android.chat.ui.NonScrollListView;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.helpers.MenuViewHolder;
import com.yo.android.inapp.UnManageInAppPurchaseActivity;
import com.yo.android.model.FindPeople;
import com.yo.android.model.MoreData;
import com.yo.android.model.denominations.Denominations;
import com.yo.android.pjsip.YoSipService;
import com.yo.android.provider.YoAppContactContract;
import com.yo.android.ui.MoreSettingsActivity;
import com.yo.android.ui.NotificationsActivity;
import com.yo.android.ui.TabsHeaderActivity;
import com.yo.android.ui.TransferBalanceSelectContactActivity;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;
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

    @Bind(R.id.lv_settings)
    protected ListView menuListView;

    @Bind(R.id.txtEmpty)
    protected TextView txtEmpty;
    private static final int OPEN_ADD_BALANCE_RESULT = 1000;
    private EditText voucherNumberEdit;

    @Override
    public void onResume() {
        super.onResume();
        preferenceEndPoint.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        Call<List<Denominations>> call = yoService.getDenominations(accessToken);
        call.enqueue(new Callback<List<Denominations>>() {
            @Override
            public void onResponse(Call<List<Denominations>> call, Response<List<Denominations>> response) {
                if (response.body() != null && response.body().size() > 0) {
                    txtEmpty.setVisibility(View.GONE);
                    List<Denominations> demonimations = response.body();
                    prepareCreditAccountList(demonimations);
                    if (demonimations != null && demonimations.size() > 0) {
                        preferenceEndPoint.saveStringPreference(Constants.CURRENCY_SYMBOL, demonimations.get(0).getCurrencySymbol());
                        txt_balance.setText(String.format("%s %s", MoreFragment.currencySymbolDollar, balance));
                    } else {
                        txtEmpty.setVisibility(View.VISIBLE);
                        FragmentActivity activity = getActivity();
                        if (activity != null) {
                            txtEmpty.setText(activity.getResources().getString(R.string.no_denominations_for_your_country));
                        }
                    }
                } else {
                    txtEmpty.setVisibility(View.VISIBLE);
                    FragmentActivity activity = getActivity();
                    if (activity != null) {
                        txtEmpty.setText(activity.getResources().getString(R.string.no_denominations_for_your_country));
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Denominations>> call, Throwable t) {
                Log.w(TAG, "Data Failed to load currenncy");
                txtEmpty.setVisibility(View.VISIBLE);


            }
        });
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


    private void addGooglePlayBalance(String sku, float price) {
        final Intent intent = new Intent(getActivity(), UnManageInAppPurchaseActivity.class);
        intent.putExtra("sku", sku);// "com.yo.products.credit.TEN"
        intent.putExtra("price", price);//10f
        final String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        intent.putExtra(Constants.USER_ID, userId);
        startActivityForResult(intent, OPEN_ADD_BALANCE_RESULT);
    }


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
            txt_balance.setText(String.format("%s%s", MoreFragment.currencySymbolDollar, balance));
        }
    }

    /**
     * Prepares the Credit Account list
     *
     * @param demonimations
     */
    public void prepareCreditAccountList(final List<Denominations> demonimations) {
        final CurrencyListAdapter adapter = prepareCurrencyAdapter();
        menuAdapter = new MoreListAdapter(getActivity()) {
            @Override
            public int getLayoutId() {
                return R.layout.item_with_options;
            }

            @Override
            public void bindView(int position, MenuViewHolder holder, MoreData item) {
                NonScrollableGridView viewById = (NonScrollableGridView) holder.getRootView().findViewById(R.id.add_google_play_items);
                viewById.setAdapter(adapter);
                adapter.addItems(demonimations);
                if (position == 0) {
                    viewById.setVisibility(View.VISIBLE);
                } else {
                    viewById.setVisibility(View.GONE);
                }
                super.bindView(position, holder, item);
            }
        };
        menuAdapter.addItems(getMenuList());
        menuListView.setAdapter(menuAdapter);
        menuListView.setOnItemClickListener(this);
    }

    private CurrencyListAdapter prepareCurrencyAdapter() {
        return new CurrencyListAdapter(getActivity()) {
            @Override
            public void bindView(int position, CurrencyViewHolder holder, Denominations item) {
                Button viewById = holder.getButtonView();
                viewById.setText(item.getCurrencySymbol() + " " + item.getDenomination());
                viewById.setTag(R.id.btn1, item);
                viewById.setOnClickListener(payBtnListener);
            }
        };

    }

    /**
     * Creates the Credit Account list
     *
     * @return
     */
    public List<MoreData> getMenuList() {
        List<MoreData> menuDataList = new ArrayList<>();
        FragmentActivity activity = getActivity();
        if (activity != null) {
            menuDataList.add(new MoreData(activity.getString(R.string.add_balance_from_google_play), false));
            menuDataList.add(new MoreData(activity.getString(R.string.add_balance_from_voucher), true));
            if (getArguments() == null) {
                menuDataList.add(new MoreData(activity.getString(R.string.transfer_balance), true));
            }
        }
        return menuDataList;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String name = ((MoreData) parent.getAdapter().getItem(position)).getName();
        FragmentActivity activity = getActivity();
        if (activity != null) {
            if (name.equalsIgnoreCase(activity.getString(R.string.add_balance_from_voucher))) {
                Bundle arguments = getArguments();
                if (!BuildConfig.INTERNAL_MTUITY_RELEASE || (arguments != null && arguments.getBoolean(Constants.OPEN_ADD_BALANCE))) {
                    showVoucherDialog();
                } else {
                    showInternalBuildMessage();
                }
            } else if (name.equalsIgnoreCase(activity.getString(R.string.transfer_balance))) {
                //TODO: Need to implement allow balance transfer even in out going call.
                if (YoSipService.currentCall != null && YoSipService.outgoingCallUri != null) {
                    mToastFactory.showToast(getActivity().getResources().getString(R.string.balance_transfer_not_allowed));
                } else {
                    String balance = mBalanceHelper.getCurrentBalance();
                    String currencySymbol = mBalanceHelper.getCurrencySymbol();
                    Intent intent = new Intent(activity, TransferBalanceSelectContactActivity.class);
                    intent.putExtra("balance", balance);
                    intent.putExtra("currencySymbol", currencySymbol);
                    startActivityForResult(intent, 11);
                }

            }
        }
    }

    private void showInternalBuildMessage() {
        mToastFactory.showToast(R.string.internal_build_cant_add_balance);
    }

    private OnClickListener payBtnListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Bundle arguments = getArguments();
            if (!BuildConfig.INTERNAL_MTUITY_RELEASE || (arguments != null && arguments.getBoolean(Constants.OPEN_ADD_BALANCE))) {
                Denominations item = (Denominations) v.getTag(R.id.btn1);
                addGooglePlayBalance("android.test.purchased", item.getDenomination());
            } else {
                showInternalBuildMessage();
            }

            //Bundle arguments = getArguments();
            //if (arguments != null) {
            //if (Double.valueOf(balance) < 5.000 && arguments.getBoolean(Constants.OPEN_ADD_BALANCE)) {
           /* if (Double.valueOf(balance) < 5.000) {

            } else {
                addGooglePlayBalance(item.getProductID(), item.getDenomination());
            }*/
                    /*} else {
                        mToastFactory.showToast(R.string.disabled);
                    }*/
            /*} else {
                mToastFactory.showToast(R.string.disabled);
            }*/
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
            voucherNumberEdit = (EditText) view.findViewById(R.id.dialog_content_edit);

            final String userName = preferenceEndPoint.getStringPreference(Constants.USER_NAME);
            String phonNumber = preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER);

            if (userName != null) {
                namePhoneText.setText(userName);
            } else if (phonNumber != null) {
                namePhoneText.setText(phonNumber);
            } else {
                if (getActivity() != null) {
                    namePhoneText.setText(getActivity().getString(R.string.unknown));
                } else {
                    namePhoneText.setText("Unknown");
                }
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
                                                    Util.hideKeyboard(getActivity(), voucherNumberEdit);
                                                    mToastFactory.showToast(response.body().getData().toString());
                                                    closeActivityAddBalance(Activity.RESULT_CANCELED, null);
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

                                Util.hideKeyboard(getActivity(), voucherNumberEdit);
                                mToastFactory.showToast(R.string.invalid_voucher);

                            }
                        });
                    } else {
                        Util.hideKeyboard(getActivity(), voucherNumberEdit);
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
        Util.hideKeyboard(getActivity(), voucherNumberEdit);
        mToastFactory.showToast(resoureId);
        closeActivityAddBalance(Activity.RESULT_CANCELED, null);
    }

}

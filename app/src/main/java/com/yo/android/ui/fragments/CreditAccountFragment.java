package com.yo.android.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.BalanceAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.inapp.UnManageInAppPurchaseActivity;
import com.yo.android.model.MoreData;
import com.yo.android.model.denominations.Denominations;
import com.yo.android.model.wallet.Balance;
import com.yo.android.pjsip.YoSipService;
import com.yo.android.ui.TransferBalanceActivity;
import com.yo.android.ui.TransferBalanceSelectContactActivity;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;
import com.yo.android.BuildConfig;
import com.yo.android.vox.BalanceHelper;
import com.yo.dialer.CallHelper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreditAccountFragment extends BaseFragment implements SharedPreferences.OnSharedPreferenceChangeListener, BalanceAdapter.MoreItemListener {

    private static final String TAG = CreditAccountFragment.class.getSimpleName();
    private static final int OPEN_ADD_BALANCE_RESULT = 1000;
    public static final int SEPERATOR = 1;

    @Bind(R.id.lv_settings)
    protected RecyclerView menuRecyclerView;
    @Bind(R.id.txtEmpty)
    protected TextView txtEmpty;

    @Inject
    YoApi.YoService yoService;
    @Inject
    BalanceHelper balanceHelper;


    private EditText voucherNumberEdit;
    private BalanceAdapter balanceAdapter;
    private ArrayList<Object> denominationData = new ArrayList<>();
    private ArrayList<Object> data = new ArrayList<>();

    @Override
    public void onResume() {
        super.onResume();
        preferenceEndPoint.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        prepareMenuList(denominationData);
        balanceHelper.checkBalance(null);
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

        balanceAdapter = new BalanceAdapter(getActivity(), data, denominationData, CreditAccountFragment.this);
        balanceAdapter.setMoreItemListener(this);
        menuRecyclerView.setAdapter(balanceAdapter);
        menuRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        retrieveDenominations();

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
        if (getArguments() != null && getArguments().getBoolean(Constants.RENEWAL, false)) {
            getActivity().setResult(1001);
            getActivity().finish();
            de.greenrobot.event.EventBus.getDefault().post(Constants.RENEWAL);
        }
    }


    public void addGooglePlayBalance(String sku, float price) {
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
        if (mBalanceHelper != null && (requestCode == 11 || requestCode == 22) && resultCode == Activity.RESULT_OK) {
            showProgressDialog();
            mBalanceHelper.checkBalance(new Callback<Balance>() {
                @Override
                public void onResponse(Call<Balance> call, Response<Balance> response) {
                    dismissProgressDialog();
                    try {
                        preferenceEndPoint.saveStringPreference(Constants.CURRENT_BALANCE, mBalanceHelper.getCurrentBalance());
                    } catch (IllegalArgumentException e) {
                        mLog.w(TAG, "getCurrentBalance", e);
                    }
                }

                @Override
                public void onFailure(Call<Balance> call, Throwable t) {
                    dismissProgressDialog();

                }
            });
        } else if (mBalanceHelper != null && resultCode == Activity.RESULT_OK) {
            try {
                preferenceEndPoint.saveStringPreference(Constants.CURRENT_BALANCE, mBalanceHelper.getCurrentBalance());

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
            prepareMenuList(denominationData);
        }
    }

    /**
     * Prepares the Credit Account list
     *
     * @param denominations
     */
    public void prepareCreditAccountList(final List<Denominations> denominations) {
        denominationData.add(denominations);
        prepareMenuList(denominationData);
    }

    /**
     * Creates the Credit Account list
     *
     * @return
     */
    public void prepareMenuList(ArrayList<Object> denominationsList) {
        String balance = mBalanceHelper.getCurrentBalance();

        //Todo remove these lines as we are not using
        String mSBalance = mBalanceHelper.getSwitchBalance();
        String mWBalance = mBalanceHelper.getWalletBalance();

        data = new ArrayList<>();
        FragmentActivity activity = getActivity();

        if (activity != null && denominationsList != null) {
            data.add(new MoreData(activity.getString(R.string.your_total_balance), false, balance));
            data.add(new MoreData(activity.getString(R.string.add_balance_from_google_play), false, null));
            data.addAll(denominationsList);
            data.add(new MoreData(activity.getString(R.string.add_balance_from_voucher), true, null));
            if (getArguments() == null) {
                data.add(new MoreData(activity.getString(R.string.transfer_balance), true, null));
            }

            //data.add(SEPERATOR);

        }
        balanceAdapter.addItems(data);
    }

    private void showInternalBuildMessage() {
        mToastFactory.showToast(R.string.internal_build_cant_add_balance);
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
                        String accessToken = preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
                        yoService.voucherRechargeAPI(accessToken, voucherNumber).enqueue(new Callback<com.yo.android.model.Response>() {
                            @Override
                            public void onResponse(Call<com.yo.android.model.Response> call, Response<com.yo.android.model.Response> response) {

                                if (response.isSuccessful()) {
                                    if (response.code() == 200) {
                                        try {
                                            int statusCode = response.body().getCode();
                                            switch (statusCode) {
                                                case 200:
                                                    mToastFactory.showToast(R.string.voucher_recharge_successful);
                                                    alertDialog.dismiss();
                                                    mBalanceHelper.checkBalance(new Callback<Balance>() {
                                                        @Override
                                                        public void onResponse(Call<Balance> call, Response<Balance> response) {
                                                            closeActivityAddBalance(Activity.RESULT_OK, null);
                                                        }

                                                        @Override
                                                        public void onFailure(Call<Balance> call, Throwable t) {
                                                            closeActivityAddBalance(Activity.RESULT_CANCELED, null);
                                                            CallHelper.uploadToGoogleSheetBalanceFail(preferenceEndPoint, "", "", "Failed to load balance");
                                                        }
                                                    });
                                                    break;
                                                case 600:
                                                    Util.hideKeyboard(getActivity(), voucherNumberEdit);
                                                    mToastFactory.showToast(response.body().getData().toString());
                                                    CallHelper.uploadToGoogleSheetBalanceFail(preferenceEndPoint, "", "", "Failed to recharge voucher because of " + response.body().getData().toString());
                                                    closeActivityAddBalance(Activity.RESULT_CANCELED, null);
                                                    break;
                                                case 708:
                                                    Util.hideKeyboard(getActivity(), voucherNumberEdit);
                                                    mToastFactory.showToast(response.body().getData().toString());
                                                    closeActivityAddBalance(Activity.RESULT_CANCELED, null);
                                                    break;
                                                default:
                                                    showMessage(R.string.invalid_voucher);
                                                    CallHelper.uploadToGoogleSheetBalanceFail(preferenceEndPoint, "", "", getResources().getString(R.string.invalid_voucher));
                                                    break;
                                            }
                                        } catch (ClassCastException e) {
                                            mLog.d("ClassCastException", e.getMessage());
                                        }
                                    } else {
                                        showMessage(R.string.invalid_voucher);
                                        CallHelper.uploadToGoogleSheetBalanceFail(preferenceEndPoint, "", "", getResources().getString(R.string.invalid_voucher));
                                    }

                                } else {
                                    showMessage(R.string.invalid_voucher);
                                    CallHelper.uploadToGoogleSheetBalanceFail(preferenceEndPoint, "", "", getResources().getString(R.string.invalid_voucher));
                                }

                            }

                            @Override
                            public void onFailure(Call<com.yo.android.model.Response> call, Throwable t) {

                                Util.hideKeyboard(getActivity(), voucherNumberEdit);
                                mToastFactory.showToast(R.string.invalid_voucher);
                                CallHelper.uploadToGoogleSheetBalanceFail(preferenceEndPoint, "", "", getResources().getString(R.string.invalid_voucher));
                            }
                        });
                    } else {
                        Util.hideKeyboard(getActivity(), voucherNumberEdit);
                        mToastFactory.showToast(getString(R.string.enter_vocher_number));
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

    @Override
    public void onRowSelected(int position) {
        FragmentActivity activity = getActivity();
        if (activity != null && data.get(position) instanceof MoreData) {
            String name = ((MoreData) data.get(position)).getName();
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
                    /*boolean userType = preferenceEndPoint.getBooleanPreference(Constants.USER_TYPE, false);
                    if (userType) {
                        TransferBalanceActivity.start(activity, currencySymbol, balance, true);
                    } else {
                        TransferBalanceSelectContactActivity.start(activity, balance, false);
                    }*/

                }

            }
        }
    }

    private void retrieveDenominations() {
        String accessToken = preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
        Call<List<Denominations>> call = yoService.getDenominations(accessToken);
        call.enqueue(new Callback<List<Denominations>>() {
            @Override
            public void onResponse(Call<List<Denominations>> call, Response<List<Denominations>> response) {
                if (response.body() != null && response.body().size() > 0) {
                    try {
                        txtEmpty.setVisibility(View.GONE);
                        List<Denominations> demonimations = response.body();
                        prepareCreditAccountList(demonimations);
                        if (demonimations != null && demonimations.size() > 0) {
                            preferenceEndPoint.saveStringPreference(Constants.CURRENCY_SYMBOL, demonimations.get(0).getCurrencySymbol());

                        } else {
                            txtEmpty.setVisibility(View.VISIBLE);
                            FragmentActivity activity = getActivity();
                            if (activity != null) {
                                txtEmpty.setText(activity.getResources().getString(R.string.no_denominations_for_your_country));
                            }
                        }
                    } finally {
                        if(response != null && response.body() != null) {
                            try {
                                response.body().clear();
                                response = null;
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
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
}

package com.yo.android.ui.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;
import com.yo.android.api.YoApi;
import com.yo.android.usecase.DenominationsUsecase;
import com.yo.android.R;
import com.yo.android.api.ApiCallback;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.model.Response;
import com.yo.android.model.TransferBalanceDenomination;
import com.yo.android.pjsip.YoSipService;
import com.yo.android.ui.TransferBalanceActivity;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;


public class NewCreditAccountFragment extends BaseFragment {

    @Bind(R.id.avi_first)
    protected AVLoadingIndicatorView aviFirst;
    @Bind(R.id.avi_second)
    protected AVLoadingIndicatorView aviSecond;
    @Bind(R.id.avi_third)
    protected AVLoadingIndicatorView aviThird;
    @Bind(R.id.tv_first)
    protected TextView tvFirst;
    @Bind(R.id.tv_second)
    protected TextView tvSecond;
    @Bind(R.id.tv_third)
    protected TextView tvThird;

    @Inject
    DenominationsUsecase denominationsUsecase;
    @Inject
    YoApi.YoService yoService;

    View view;
    EditText voucherNumberEdit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = LayoutInflater.from(getActivity()).inflate(R.layout.new_yo_credit_details, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        showAVLoader();
        getTransferBalanceDenominations();
    }

    private void showAVLoader() {
        aviFirst.setIndicator("BallClipRotateMultipleIndicator");
        aviFirst.setIndicatorColor(getResources().getColor(R.color.colorPrimaryDark));

        aviSecond.setIndicator("BallClipRotateMultipleIndicator");
        aviSecond.setIndicatorColor(getResources().getColor(R.color.colorPrimaryDark));

        aviThird.setIndicator("BallClipRotateMultipleIndicator");
        aviThird.setIndicatorColor(getResources().getColor(R.color.colorPrimaryDark));

    }

    private void showDenominations(List<TransferBalanceDenomination> transferBalanceDenominationList) {
        aviFirst.setVisibility(View.GONE);
        tvFirst.setVisibility(View.VISIBLE);
        tvFirst.setText(String.format(getResources().getString(R.string.currency_code_with_denomination), transferBalanceDenominationList.get(0).getCurrencySymbol(), transferBalanceDenominationList.get(0).getDenomination()));

        aviSecond.setVisibility(View.GONE);
        tvSecond.setText(String.format(getResources().getString(R.string.currency_code_with_denomination), transferBalanceDenominationList.get(1).getCurrencySymbol(), transferBalanceDenominationList.get(1).getDenomination()));
        tvSecond.setVisibility(View.VISIBLE);

        aviThird.setVisibility(View.GONE);
        tvThird.setText(String.format(getResources().getString(R.string.currency_code_with_denomination), transferBalanceDenominationList.get(2).getCurrencySymbol(), transferBalanceDenominationList.get(2).getDenomination()));
        tvThird.setVisibility(View.VISIBLE);

    }

    @OnClick(R.id.give_first)
    public void giveFirst() {
        transferBalance(tvFirst.getText().toString());
    }

    @OnClick(R.id.give_second)
    public void giveSecond() {
        transferBalance(tvSecond.getText().toString());
    }

    @OnClick(R.id.give_third)
    public void giveThird() {
        transferBalance(tvThird.getText().toString());
    }

    @OnClick(R.id.buy_five_package)
    public void buyFivePackage() {

    }

    @OnClick(R.id.buy_ten_package)
    public void buyTenPackage() {

    }

    @OnClick(R.id.buy_twenty_package)
    public void buyTwentyPackage() {

    }

    @OnClick(R.id.add_balance_from_voucher)
    public void addBalanceFromVoucher() {
        showVoucherDialog();
    }

    public void transferBalance(String transferAmount) {
        if (YoSipService.currentCall != null && YoSipService.outgoingCallUri != null) {
            mToastFactory.showToast(getActivity().getResources().getString(R.string.balance_transfer_not_allowed));
        } else {
            String balance = mBalanceHelper.getCurrentBalance();
            navigateToTransferBalance(balance, transferAmount, true);

        }
    }

    private void getTransferBalanceDenominations() {
        denominationsUsecase.getDenominations(new ApiCallback<ArrayList<TransferBalanceDenomination>>() {
            @Override
            public void onResult(ArrayList<TransferBalanceDenomination> result) {
                showDenominations(result);
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });
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

            yesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String voucherNumber = voucherNumberEdit.getText().toString();

                    if (!TextUtils.isEmpty(voucherNumber.trim())) {
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.voucherRechargeAPI(accessToken, voucherNumber).enqueue(new Callback<com.yo.android.model.Response>() {
                            @Override
                            public void onResponse(Call<com.yo.android.model.Response> call, retrofit2.Response<Response> response) {

                                if (response.isSuccessful()) {
                                    if (response.code() == 200) {
                                        try {
                                            int statusCode = response.body().getCode();
                                            switch (statusCode) {
                                                case 200:
                                                    mToastFactory.showToast(R.string.voucher_recharge_successful);
                                                    alertDialog.dismiss();
                                                    mBalanceHelper.checkBalance(new Callback<ResponseBody>() {
                                                        @Override
                                                        public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
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
                                                case 708:
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
                        mToastFactory.showToast(getString(R.string.enter_vocher_number));
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

    private void showMessage(int resoureId) {
        Util.hideKeyboard(getActivity(), voucherNumberEdit);
        mToastFactory.showToast(resoureId);
        closeActivityAddBalance(Activity.RESULT_CANCELED, null);
    }

    private void closeActivityAddBalance(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        }
        if (getArguments() != null && getArguments().getBoolean(Constants.OPEN_ADD_BALANCE, false)) {
            getActivity().setResult(resultCode);
            getActivity().finish();
        }
        if (getArguments() != null && getArguments().getBoolean(Constants.RENEWAL, false)) {
            getActivity().setResult(1001);
            getActivity().finish();
            de.greenrobot.event.EventBus.getDefault().post(Constants.RENEWAL);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mBalanceHelper != null && (requestCode == 11 || requestCode == 22) && resultCode == Activity.RESULT_OK) {

            // Available balance after transaction
            if (data != null) {
                String currentAvailableAmount = data.getStringExtra(Constants.CURRENT_BALANCE);
                preferenceEndPoint.saveStringPreference(Constants.CURRENT_BALANCE, currentAvailableAmount);
                EventBus.getDefault().post(Constants.BALANCE_UPDATED_ACTION);
            } else {
                showProgressDialog();
                mBalanceHelper.checkBalance(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                        dismissProgressDialog();
                        preferenceEndPoint.saveStringPreference(Constants.CURRENT_BALANCE, mBalanceHelper.getCurrentBalance());
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        dismissProgressDialog();

                    }
                });
            }
        } else if (mBalanceHelper != null && resultCode == Activity.RESULT_OK) {
            preferenceEndPoint.saveStringPreference(Constants.CURRENT_BALANCE, mBalanceHelper.getCurrentBalance());
        }

        closeActivityAddBalance(resultCode, data);
    }

    private void navigateToTransferBalance(String availableBalance, String transferAmount, boolean userType) {

        Intent intent = new Intent(getActivity(), TransferBalanceActivity.class);
        intent.putExtra(Constants.CURRENT_BALANCE, availableBalance);
        intent.putExtra(Constants.USER_TYPE, userType);
        intent.putExtra(Constants.TRANSFER_AMOUNT, transferAmount);
        startActivityForResult(intent, 11);
    }
}

package com.yo.android.ui.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
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
import com.yo.android.model.PackageDenomination;
import com.yo.android.usecase.DenominationsUsecase;
import com.yo.android.R;
import com.yo.android.api.ApiCallback;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.model.Response;
import com.yo.android.model.TransferBalanceDenomination;
import com.yo.android.pjsip.YoSipService;
import com.yo.android.ui.TransferBalanceActivity;
import com.yo.android.usecase.PackageDenominationsUsecase;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;
import com.yo.android.vox.BalanceHelper;
import com.yo.dialer.CallHelper;

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

    private static final String BALL_CLIP_ROTATE = "BallClipRotateMultipleIndicator";

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
    @Bind(R.id.give_first)
    Button giveFirst;
    @Bind(R.id.give_second)
    Button giveSecond;
    @Bind(R.id.give_third)
    Button giveThird;
    @Bind(R.id.buy_first_package)
    Button buyFirstPackage;
    @Bind(R.id.buy_second_package)
    Button buySecondPackage;
    @Bind(R.id.buy_third_package)
    Button buyThirdPackage;

    @Inject
    DenominationsUsecase denominationsUsecase;
    @Inject
    PackageDenominationsUsecase packageDenominationsUsecase;
    @Inject
    YoApi.YoService yoService;
    @Inject
    BalanceHelper mBalanceHelper;

    View view;
    EditText voucherNumberEdit;
    String currencySymbol;
    List<TransferBalanceDenomination> transferBalanceDenominationList;
    Activity activity;

    private static final String ONE_ZERO = "0";
    private static final String TWO_ZERO = "00";
    private static final String THREE_ZERO = "000";
    private static final String FOUR_ZERO = "0000";
    private static final String FIVE_ZERO = "00000";
    private static final String SIX_ZERO = "000000";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = LayoutInflater.from(getActivity()).inflate(R.layout.new_yo_credit_details, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {
            activity = (Activity) context;
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        showDenominationAVLoader();
        inActivePackageDenomination();
        getTransferBalanceDenominations();
        getBuyPackageDenomination();
        EventBus.getDefault().register(this);
    }

    private void showDenominationAVLoader() {
        aviFirst.setIndicator(BALL_CLIP_ROTATE);
        aviFirst.setIndicatorColor(getResources().getColor(R.color.colorPrimaryDark));
        giveFirst.setAlpha(0.5f);
        giveFirst.setClickable(false);

        aviSecond.setIndicator(BALL_CLIP_ROTATE);
        aviSecond.setIndicatorColor(getResources().getColor(R.color.colorPrimaryDark));
        giveSecond.setAlpha(0.5f);
        giveSecond.setClickable(false);

        aviThird.setIndicator(BALL_CLIP_ROTATE);
        aviThird.setIndicatorColor(getResources().getColor(R.color.colorPrimaryDark));
        giveThird.setAlpha(0.5f);
        giveThird.setClickable(false);
    }

    private void hideDenominationAVLoader() {
        aviFirst.setVisibility(View.GONE);
        aviSecond.setVisibility(View.GONE);
        aviThird.setVisibility(View.GONE);
    }

    private void showDenominations() {
        hideDenominationAVLoader();
        currencySymbol = transferBalanceDenominationList.get(0).getCurrencySymbol();
        preferenceEndPoint.saveStringPreference(Constants.CURRENCY_SYMBOL, currencySymbol);
        if (transferBalanceDenominationList.get(0) != null && transferBalanceDenominationList.get(0).getStatus().equalsIgnoreCase(Constants.PACKAGE_STATUS)) {
            tvFirst.setVisibility(View.VISIBLE);
            tvFirst.setText(String.format(activity.getResources().getString(R.string.currency_code_with_denomination), mBalanceHelper.currencySymbolLookup(currencySymbol), transferBalanceDenominationList.get(0).getDenomination()));
            giveFirst.setAlpha(1);
            giveFirst.setClickable(true);
        }

        if (transferBalanceDenominationList.get(1) != null && transferBalanceDenominationList.get(1).getStatus().equalsIgnoreCase(Constants.PACKAGE_STATUS)) {
            tvSecond.setText(String.format(activity.getResources().getString(R.string.currency_code_with_denomination), mBalanceHelper.currencySymbolLookup(currencySymbol), transferBalanceDenominationList.get(1).getDenomination()));
            tvSecond.setVisibility(View.VISIBLE);
            giveSecond.setAlpha(1);
            giveSecond.setClickable(true);
        }

        if (transferBalanceDenominationList.get(2) != null && transferBalanceDenominationList.get(2).getStatus().equalsIgnoreCase(Constants.PACKAGE_STATUS)) {
            tvThird.setText(String.format(activity.getResources().getString(R.string.currency_code_with_denomination), mBalanceHelper.currencySymbolLookup(currencySymbol), transferBalanceDenominationList.get(2).getDenomination()));
            tvThird.setVisibility(View.VISIBLE);
            giveThird.setAlpha(1);
            giveThird.setClickable(true);
        }
    }

    private void inActivePackageDenomination() {
        buyFirstPackage.setAlpha(0.5f);
        buyFirstPackage.setClickable(false);

        buySecondPackage.setAlpha(0.5f);
        buySecondPackage.setClickable(false);

        buyThirdPackage.setAlpha(0.5f);
        buyThirdPackage.setClickable(false);
    }

    private void showActivePackageDenomination(List<PackageDenomination> packageDenominationList) {
        String pCurrencySymbol = packageDenominationList.get(0).getCurrencySymbol();
        pCurrencySymbol = mBalanceHelper.currencySymbolLookup(pCurrencySymbol);

        buyFirstPackage.setAlpha(1);
        buyFirstPackage.setClickable(true);
        if (packageDenominationList.get(0) != null) {
            buyFirstPackage.setText(String.format(activity.getResources().getString(R.string.currency_code_with_denomination), pCurrencySymbol, packageDenominationList.get(0).getPackage()));
        }

        buySecondPackage.setAlpha(1);
        buySecondPackage.setClickable(true);
        if (packageDenominationList.get(1) != null) {
            buySecondPackage.setText(String.format(activity.getResources().getString(R.string.currency_code_with_denomination), pCurrencySymbol, packageDenominationList.get(1).getPackage()));
        }

        buyThirdPackage.setAlpha(1);
        buyThirdPackage.setClickable(true);
        if (packageDenominationList.get(1) != null) {
            buyThirdPackage.setText(String.format(activity.getResources().getString(R.string.currency_code_with_denomination), pCurrencySymbol, packageDenominationList.get(2).getPackage()));
        }
    }


    @OnClick(R.id.give_first)
    public void giveFirst() {
        if (transferBalanceDenominationList.get(0) != null && transferBalanceDenominationList.get(0).getStatus().equalsIgnoreCase(Constants.PACKAGE_STATUS)) {
            transferBalance(tvFirst.getText().toString());
        } else {
            showErrorMessage(R.string.transfer_balance_error_message);
        }

    }

    @OnClick(R.id.give_second)
    public void giveSecond() {
        if (transferBalanceDenominationList.get(1) != null && transferBalanceDenominationList.get(1).getStatus().equalsIgnoreCase(Constants.PACKAGE_STATUS)) {
            transferBalance(tvSecond.getText().toString());
        } else {
            showErrorMessage(R.string.transfer_balance_error_message);
        }
    }

    @OnClick(R.id.give_third)
    public void giveThird() {
        if (transferBalanceDenominationList.get(2) != null && transferBalanceDenominationList.get(2).getStatus().equalsIgnoreCase(Constants.PACKAGE_STATUS)) {
            transferBalance(tvThird.getText().toString());
        } else {
            showErrorMessage(R.string.transfer_balance_error_message);
        }
    }

    @OnClick(R.id.add_more_amount)
    public void addMoreAmount() {
        if (Util.isOnline(getActivity())) {
            showBalanceDialog();
        } else {
            showAlertDialog("", getResources().getString(R.string.network_error));
        }

    }

    // Need to implement buy package
    @OnClick(R.id.buy_first_package)
    public void buyFivePackage() {
        showErrorMessage(R.string.offers_error_message);
    }

    @OnClick(R.id.buy_second_package)
    public void buyTenPackage() {
        showErrorMessage(R.string.offers_error_message);
    }

    @OnClick(R.id.buy_third_package)
    public void buyTwentyPackage() {
        showErrorMessage(R.string.offers_error_message);
    }

    @OnClick(R.id.add_balance_from_voucher)
    public void addBalanceFromVoucher() {
        showVoucherDialog();
    }

    public void transferBalance(String transferAmount) {
        if (YoSipService.currentCall != null && YoSipService.outgoingCallUri != null) {
            if (activity == null) {
                activity = getActivity();
            }
            mToastFactory.showToast(activity.getResources().getString(R.string.balance_transfer_not_allowed));
        } else {
            String balance = mBalanceHelper.getCurrentBalance();
            navigateToTransferBalance(balance, transferAmount, true);

        }
    }

    private void getTransferBalanceDenominations() {
        denominationsUsecase.getDenominations(new ApiCallback<ArrayList<TransferBalanceDenomination>>() {
            @Override
            public void onResult(ArrayList<TransferBalanceDenomination> result) {
                if (result != null && result.size() > 0) {
                    transferBalanceDenominationList = result;
                    showDenominations();
                }
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                hideDenominationAVLoader();
            }
        });
    }

    private void getBuyPackageDenomination() {
        packageDenominationsUsecase.getPackageDenominationsUsecase(new ApiCallback<ArrayList<PackageDenomination>>() {
            @Override
            public void onResult(ArrayList<PackageDenomination> result) {
                if (result != null && result.size() > 0) {
                    showActivePackageDenomination(result);
                }
            }

            @Override
            public void onFailure(String message) {
                //Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showVoucherDialog() {

        if (activity != null) {

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
                if (activity != null) {
                    namePhoneText.setText(activity.getString(R.string.unknown));
                } else {
                    namePhoneText.setText(activity.getString(R.string.unknown));
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
                        showProgressDialog();
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.voucherRechargeAPI(accessToken, voucherNumber).enqueue(new Callback<com.yo.android.model.Response>() {
                            @Override
                            public void onResponse(Call<com.yo.android.model.Response> call, retrofit2.Response<Response> response) {
                                dismissProgressDialog();
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
                                                            preferenceEndPoint.saveBooleanPreference("isNewUser", false);
                                                            closeActivityAddBalance(Activity.RESULT_OK, null);
                                                        }

                                                        @Override
                                                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                            closeActivityAddBalance(Activity.RESULT_CANCELED, null);
                                                            CallHelper.uploadToGoogleSheetBalanceFail(preferenceEndPoint, "", "", "Failed to load balance");
                                                        }
                                                    });
                                                    break;
                                                case 600:
                                                    Util.hideKeyboard(activity, voucherNumberEdit);
                                                    mToastFactory.showToast(response.body().getData().toString());
                                                    CallHelper.uploadToGoogleSheetBalanceFail(preferenceEndPoint, "", "", "Failed to recharge voucher because of " + response.body().getData().toString());
                                                    closeActivityAddBalance(Activity.RESULT_CANCELED, null);
                                                    break;
                                                case 706:
                                                case 708:
                                                    Util.hideKeyboard(activity, voucherNumberEdit);
                                                    mToastFactory.showToast(response.body().getData().toString());
                                                    CallHelper.uploadToGoogleSheetBalanceFail(preferenceEndPoint, "", "", "Failed to recharge voucher because of " + response.body().getData().toString());
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
                                    }

                                } else {
                                    showMessage(R.string.invalid_voucher);
                                }

                            }

                            @Override
                            public void onFailure(Call<com.yo.android.model.Response> call, Throwable t) {
                                dismissProgressDialog();
                                Util.hideKeyboard(activity, voucherNumberEdit);
                                mToastFactory.showToast(R.string.invalid_voucher);

                            }
                        });
                    } else {
                        Util.hideKeyboard(activity, voucherNumberEdit);
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
        Util.hideKeyboard(activity, voucherNumberEdit);
        mToastFactory.showToast(resoureId);
        closeActivityAddBalance(Activity.RESULT_CANCELED, null);
    }

    private void closeActivityAddBalance(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        }
        if (getArguments() != null && getArguments().getBoolean(Constants.OPEN_ADD_BALANCE, false)) {
            if (activity == null) {
                activity = getActivity();
            }
            activity.setResult(resultCode);
            activity.finish();
        }
        if (getArguments() != null && getArguments().getBoolean(Constants.RENEWAL, false)) {
            if (activity == null) {
                activity = getActivity();
            }
            activity.setResult(1001);
            activity.finish();
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

    /**
     * Navigate to transfer balance screen
     * @param availableBalance
     * @param transferAmount
     * @param userType
     */
    private void navigateToTransferBalance(String availableBalance, String transferAmount, boolean userType) {

        Intent intent = new Intent(activity, TransferBalanceActivity.class);
        intent.putExtra(Constants.CURRENT_BALANCE, availableBalance);
        intent.putExtra(Constants.USER_TYPE, userType);
        intent.putExtra(Constants.TRANSFER_AMOUNT, transferAmount);
        startActivityForResult(intent, 11);
    }

    /**
     * Dialog which shows complete balance cannot be transferred
     */
    private void showBalanceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        final View view = layoutInflater.inflate(R.layout.add_more_amount_dialog, null);
        builder.setView(view);

        TextView textView = (TextView) view.findViewById(R.id.current_balance);
        //textView.setText(getCurrentAvailableBalance());


        Button processed = (Button) view.findViewById(R.id.processed_btn);
        Button cancel = (Button) view.findViewById(R.id.cancel);
        final EditText editText = (EditText) view.findViewById(R.id.edit_amount);
        final TextView giveFour = (TextView) view.findViewById(R.id.give_four);
        final TextView giveFive = (TextView) view.findViewById(R.id.give_five);

        final String availableBalance = mBalanceHelper.getCurrentBalance();
        textView.setText(mBalanceHelper.currencySymbolLookup(availableBalance));

        if (transferBalanceDenominationList != null) {
            if (transferBalanceDenominationList.get(3) != null && transferBalanceDenominationList.get(3).getStatus().equalsIgnoreCase(Constants.PACKAGE_STATUS)) {
                giveFour.setVisibility(View.VISIBLE);
                giveFour.setText(String.format(getResources().getString(R.string.currency_code_with_denomination), mBalanceHelper.currencySymbolLookup(currencySymbol), transferBalanceDenominationList.get(3).getDenomination()));
                giveFour.setAlpha(1);
                giveFour.setClickable(true);
            }

            if (transferBalanceDenominationList.get(4) != null && transferBalanceDenominationList.get(4).getStatus().equalsIgnoreCase(Constants.PACKAGE_STATUS)) {
                giveFive.setVisibility(View.VISIBLE);
                giveFive.setText(String.format(getResources().getString(R.string.currency_code_with_denomination), mBalanceHelper.currencySymbolLookup(currencySymbol), transferBalanceDenominationList.get(4).getDenomination()));
                giveFive.setAlpha(1);
                giveFive.setClickable(true);
            }
        } else {
            mToastFactory.newToast(getResources().getString(R.string.network_error), Toast.LENGTH_LONG);
        }
        giveFour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedValue = mBalanceHelper.removeCurrencyCodeString(giveFour.getText().toString());
                editText.setText(String.valueOf(selectedValue));
            }
        });

        giveFive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedValue = mBalanceHelper.removeCurrencyCodeString(giveFive.getText().toString());
                editText.setText(selectedValue);
            }
        });
        processed.setText(getResources().getString(R.string.give));


        final AlertDialog alert = builder.create();
        alert.setCancelable(false);
        alert.getWindow().setBackgroundDrawable(new BitmapDrawable());
        alert.show();

        processed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText() != null) {

                    String enteredAmount = editText.getText().toString();

                    if (!TextUtils.isEmpty(enteredAmount) && currencySymbol != null && !enteredAmount.contains(currencySymbol) && isValid(enteredAmount)) {
                        if (!isGreaterThanHundred(enteredAmount)) {
                            double val = mBalanceHelper.removeCurrencyCode(enteredAmount);
                            if (val < mBalanceHelper.removeCurrencyCode(availableBalance)) {
                                if (enteredAmount.startsWith(".")) {
                                    enteredAmount = "0" + enteredAmount;
                                }
                                enteredAmount = String.format(getResources().getString(R.string.currency_code_with_denomination), mBalanceHelper.currencySymbolLookup(currencySymbol), enteredAmount);
                                alert.dismiss();
                                transferBalance(enteredAmount);
                            } else {
                                mToastFactory.showToast(R.string.insufficient_amount);
                            }

                        } else {
                            mToastFactory.newToast(getResources().getString(R.string.more_than_hundred_error), Toast.LENGTH_LONG);
                        }

                    } else {
                        mToastFactory.newToast(getResources().getString(R.string.correct_denomination), Toast.LENGTH_LONG);
                    }
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });
    }

    private void showAlertDialog(String titleMsg, String contentMsg) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        final View view = layoutInflater.inflate(R.layout.custom_error_dialog, null);
        builder.setView(view);

        Button okBtn = (Button) view.findViewById(R.id.yes_btn);
        TextView tvDesc = (TextView) view.findViewById(R.id.dialog_content);

        tvDesc.setText(contentMsg);
        okBtn.setText(R.string.ok);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.getWindow().setBackgroundDrawable(new BitmapDrawable());
        alertDialog.show();

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    private boolean isGreaterThanHundred(String enteredAmount) {
        double doubleValue = Double.parseDouble(enteredAmount);
        return doubleValue > 200;
    }

    private void showErrorMessage(int errorMessage) {
        showAlertDialog("", getResources().getString(errorMessage));
    }

    public void onEventMainThread(String action) {
        if (Constants.BALANCE_TRANSFER_NEW_USER.equals(action)) {
            checkBalance();
        }
    }

    private void checkBalance() {
        mBalanceHelper.checkBalance(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                closeActivityAddBalance(Activity.RESULT_OK, null);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                closeActivityAddBalance(Activity.RESULT_CANCELED, null);
                CallHelper.uploadToGoogleSheetBalanceFail(preferenceEndPoint, "", "", activity.getResources().getString(R.string.fail_to_load_balance));
            }
        });
    }

    /**
     * validation for entered number shouldn't be all zeros
     * @param mEnteredAmount
     * @return
     */
    private boolean isValid(String mEnteredAmount) {
        switch (mEnteredAmount) {
            case ONE_ZERO:
                return false;
            case TWO_ZERO:
                return false;
            case THREE_ZERO:
                return false;
            case FOUR_ZERO:
                return false;
            case FIVE_ZERO:
                return false;
            case SIX_ZERO:
                return false;
            default:
                return true;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hideDenominationAVLoader();

    }
}

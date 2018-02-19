package com.yo.android.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.yo.android.R;
import com.yo.android.helpers.Settings;
import com.yo.android.model.Contact;
import com.yo.android.photo.util.ColorGenerator;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;
import com.yo.android.vox.BalanceHelper;
import com.yo.dialer.CallHelper;

import java.text.DecimalFormat;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * This activity is used to transfer the balance to another Yo app user
 */
public class TransferBalanceActivity extends BaseActivity {

    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private String name;
    private String phoneNo;


    @Bind(R.id.et_enter_phone)
    EditText enteredPhoneNumber;
    @Bind(R.id.current_balance)
    TextView tvBalance;
    @Bind(R.id.transfer_amount)
    TextView tvTransferAmount;

    @Inject
    protected BalanceHelper mBalanceHelper;

    private String currencySymbol;
    private String mTransferAmount;

    public static void start(Activity activity, String currencySymbol, String availableBalance, String fullName, String phoneNo, String userAvatar, String userId, boolean userType) {
        Intent intent = createIntent(activity, currencySymbol, availableBalance, fullName, phoneNo, userAvatar, userId, userType);
        activity.startActivityForResult(intent, 22);
    }

    private static Intent createIntent(Activity activity, String currencySymbol, String availableBalance, String fullName, String phoneNo, String userAvatar, String userId, boolean userType) {

        Intent intent = new Intent(activity, TransferBalanceActivity.class);
        intent.putExtra(Constants.CURRENT_BALANCE, availableBalance);
        intent.putExtra(Constants.USER_TYPE, userType);
        intent.putExtra("currencySymbol", currencySymbol);
        intent.putExtra(Constants.USER_NAME, fullName);
        intent.putExtra(Constants.PHONE_NUMBER, phoneNo);
        intent.putExtra("profilePic", userAvatar);
        intent.putExtra("id", userId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_balance);
        ButterKnife.bind(this);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String title = getString(R.string.transfer_balance);

        getSupportActionBar().setTitle(title);

        String balance = getIntent().getStringExtra(Constants.CURRENT_BALANCE);
        mTransferAmount = getIntent().getStringExtra(Constants.TRANSFER_AMOUNT);

        /*enterNumberView.setVisibility(View.VISIBLE);
        contactNumberView.setVisibility(View.GONE);

        if(!BuildConfig.NEW_YO_CREDIT_SCREEN) {
            if (userType) {
                enterNumberView.setVisibility(View.VISIBLE);
                contactNumberView.setVisibility(View.GONE);
            } else {
                enterNumberView.setVisibility(View.GONE);
                contactNumberView.setVisibility(View.VISIBLE);
                userSelectedFromContacts();
            }
        } else {
            enterNumberView.setVisibility(View.VISIBLE);
            contactNumberView.setVisibility(View.GONE);
            etAmount.setVisibility(View.GONE);

        }*/

        tvTransferAmount.setText(String.format(getString(R.string.transfer_amount), mTransferAmount));
        tvBalance.setText(String.format(getString(R.string.your_yo_balance_without_line_break), mBalanceHelper.currencySymbolLookup(balance)));

        EventBus.getDefault().register(this);


    }

    @OnClick(R.id.btn_transfer)
    public void balanceTransfer() {
        String phoneNumber = enteredPhoneNumber.getText().toString();
        String mPhoneNumber = phoneNo != null ? phoneNo : phoneNumber;
        try {
            if (!TextUtils.isEmpty(mPhoneNumber.trim()) && phoneNumber.length() > 7) {
                double val = mBalanceHelper.removeCurrencyCode(mTransferAmount);
                if (val != 0) {
                    if (mBalanceHelper.removeCurrencyCode(mBalanceHelper.getCurrentBalance()) > val) {
                        String tranferVal = decimalFormat(val);
                        showMessageDialog(tranferVal, mBalanceHelper.getCurrentBalance(), mPhoneNumber);

                    }
                    /* Nexge support
                    *
                    * @Team, Earlier also we have the same rate for SIPTOSIP, we haven't updated this value. Earlier also we faced same issue like allowing app to app call with 0 balance. That time we got different answer from your side. could you please confirmme which one we need to take?
                      Like earlier when we asked you said you wont allow any calls with 0 balance, to make any call user should have atleast 0.00001 balance

                      Regarding above, as you requested. we have made changes to allow SIPTOSIP calls even when there is no balance. Pls check your mails regarding the same.
                    *
                    * */

                    /*else if (mBalanceHelper.removeCurrencyCode(mBalanceHelper.getCurrentBalance()) == val) {
                        showBalanceDialog();
                    }*/
                    else {
                        mToastFactory.showToast(R.string.insufficient_amount);
                    }
                } else {
                    mToastFactory.showToast(getResources().getString(R.string.enter_valid_amount));
                }
            } else {
                mToastFactory.showToast(getResources().getString(R.string.valid_phone));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @OnClick(R.id.btn_cancel)
    public void cancelTransfer() {
        finish();
    }


    @OnClick(R.id.select_contact)
    public void openContactView() {
        TransferBalanceSelectContactActivity.start(this);
    }

    /**
     * Loads the user's avatar image
     *
     * @param imvProfilePic The CircleImageView
     */
    private void loadAvatarImage(CircleImageView imvProfilePic) {

        Drawable tempImage = getResources().getDrawable(R.drawable.dynamic_profile);
        LayerDrawable bgDrawable = (LayerDrawable) tempImage;
        final GradientDrawable shape = (GradientDrawable) bgDrawable.findDrawableByLayerId(R.id.shape_id);
        if (Settings.isTitlePicEnabled) {
            shape.setColor(mColorGenerator.getRandomColor());
            if (imvProfilePic.getTag(Settings.imageTag) == null) {
                imvProfilePic.setTag(Settings.imageTag, tempImage);
            }
            imvProfilePic.setImageDrawable((Drawable) imvProfilePic.getTag(Settings.imageTag));
        } else {
            RequestOptions requestOptions = new RequestOptions()
                    .fitCenter()
                    .placeholder(R.drawable.dynamic_profile)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.drawable.dynamic_profile);
            Glide.with(this)
                    .load("")
                    .apply(requestOptions)
                    .transition(withCrossFade())
                    .into(imvProfilePic);
        }
    }

    /**
     * Transfers the balance to the other user
     *
     * @param amount The amount to be transferred
     */
    private void transferBalance(String amount, final String phoneNo) {
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        showProgressDialog();
        yoService.balanceTransferAPI(accessToken, phoneNo, amount).enqueue(new Callback<com.yo.android.model.Response>() {
            @Override
            public void onResponse(Call<com.yo.android.model.Response> call, Response<com.yo.android.model.Response> response) {
                dismissProgressDialog();

                if (response.isSuccessful()) {

                    if (response.code() == 200) {
                        try {
                            int statusCode = response.body().getCode();
                            switch (statusCode) {
                                case 200:
                                    String mName = name != null ? name : phoneNo;
                                    showAlertDialog(response.body().getBalance(), getString(R.string.transfer_success), getString(R.string.successful_transfer, mName), R.drawable.right_icon, true);
                                    break;
                                case 606:
                                case 607:
                                case 608:
                                case 609:
                                case 610:
                                case 611:
                                case 612:
                                case 613:
                                case 614:
                                case 615:
                                case 616:
                                    showAlertDialog(response.body().getBalance(), getString(R.string.transfer_failure), String.format(getString(R.string.transfer_failure_reason), response.body().getData().toString()), R.drawable.transaction_failed_icon, false);
                                    CallHelper.uploadToGoogleSheetBalanceFail(preferenceEndPoint, phoneNo, name, "Failed to transfer balance because of " + response.body().getData().toString());
                                    break;
                                case 617:
                                    showAlertDialog(response.body().getBalance(), getString(R.string.transfer_failure), String.format(getString(R.string.transfer_failure_reason), response.body().getData().toString()), R.drawable.transaction_failed_icon, false);
                                    CallHelper.uploadToGoogleSheetBalanceFail(preferenceEndPoint, phoneNo, name, "Failed to transfer balance because of " + response.body().getData().toString());
                                    break;
                                default:
                                    if (response.body().getData() != null) {
                                        mToastFactory.showToast(response.body().getData().toString());
                                        CallHelper.uploadToGoogleSheetBalanceFail(preferenceEndPoint, phoneNo, name, "Failed to transfer balance because of " + response.body().getData().toString());
                                    } else {
                                        mToastFactory.showToast(R.string.transfer_balance_failed);
                                        CallHelper.uploadToGoogleSheetBalanceFail(preferenceEndPoint, phoneNo, name, "Failed to transfer balance");
                                    }
                                    break;
                            }
                        } catch (ClassCastException e) {
                            mLog.d("ClassCastException", e.getMessage());
                        }
                    } else {
                        mToastFactory.showToast(R.string.transfer_balance_failed);
                        CallHelper.uploadToGoogleSheetBalanceFail(preferenceEndPoint, phoneNo, name, "Failed to transfer balance");
                    }
                } else {
                    mToastFactory.showToast(R.string.transfer_balance_failed);
                    CallHelper.uploadToGoogleSheetBalanceFail(preferenceEndPoint, phoneNo, name, "Failed to transfer balance");
                }
            }

            @Override
            public void onFailure(Call<com.yo.android.model.Response> call, Throwable t) {
                dismissProgressDialog();

                mToastFactory.showToast(R.string.transfer_balance_failed);
                CallHelper.uploadToGoogleSheetBalanceFail(preferenceEndPoint, phoneNo, name, "Failed to transfer balance");
            }
        });
    }


    /**
     * Shows the confirmation dialog to transfer the balance
     *
     * @param amount      The amount to be transferred
     * @param phoneNumber The phone number of the user to whom the balance needs to be transferred to
     */
    private void showMessageDialog(final String amount, final String amountWithDenomination, final String phoneNumber) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        final View view = layoutInflater.inflate(R.layout.custom_dialog, null);
        builder.setView(view);

        TextView textView = (TextView) view.findViewById(R.id.dialog_content);

        String mAmount = Util.addDenomination(amount, amountWithDenomination);

        String confirmationText;

        if (name != null) {
            confirmationText = getString(R.string.transfer_balance_alert, mAmount, name, phoneNumber);
        } else {
            confirmationText = getString(R.string.transfer_balance_alert_number, mAmount, phoneNumber);
        }

        textView.setText(confirmationText);


        Button yesBtn = (Button) view.findViewById(R.id.yes_btn);
        yesBtn.setText(getResources().getString(R.string.yes));
        Button noBtn = (Button) view.findViewById(R.id.no_btn);
        noBtn.setText(getResources().getString(R.string.cancel));
        noBtn.setVisibility(View.VISIBLE);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.getWindow().setBackgroundDrawable(new BitmapDrawable());
        alertDialog.show();


        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                transferBalance(amount, phoneNumber);
            }

        });

        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    public void onEventMainThread(String action) {
        if (Constants.BALANCE_TRANSFER_NOTIFICATION_ACTION.equals(action)) {
            if (mBalanceHelper != null) {
                showProgressDialog();
                mBalanceHelper.checkBalance(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        dismissProgressDialog();
                        try {
                            DecimalFormat df = new DecimalFormat("0.000");
                            String format = df.format(Double.valueOf(mBalanceHelper.getCurrentBalance()));
                            preferenceEndPoint.saveStringPreference(Constants.CURRENT_BALANCE, format);
                            tvBalance.setText(String.format("%s%s", currencySymbol, format));
/*                            double val = Double.parseDouble(format.trim());
                            if (val <= 2) {
                                mLog.w("TransferBalanceActivity", "Current balance is less than or equal to $2");
                                Util.setBigStyleNotificationForBalance(TransferBalanceActivity.this, "Credit", "You are having insufficient balance in your account. Please add balance.", "Credit", "");

                            }*/
                        } catch (IllegalArgumentException e) {
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        dismissProgressDialog();

                    }
                });
            }
        }
    }

    /**
     * Dialog which shows complete balance cannot be transferred
     */
    private void showBalanceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        final View view = layoutInflater.inflate(R.layout.custom_dialog, null);
        builder.setView(view);
        TextView textView = (TextView) view.findViewById(R.id.dialog_content);
        textView.setText(R.string.cannot_transfer_full_balance);

        Button yesBtn = (Button) view.findViewById(R.id.yes_btn);
        yesBtn.setText(getResources().getString(R.string.ok));

        final AlertDialog alert = builder.create();
        alert.setCancelable(false);
        alert.getWindow().setBackgroundDrawable(new BitmapDrawable());
        alert.show();

        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }

        });
    }

    private void showAlertDialog(final String value, String titleMsg, String contentMsg, int drawable, final boolean success) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(TransferBalanceActivity.this);

        LayoutInflater layoutInflater = LayoutInflater.from(TransferBalanceActivity.this);
        final View view = layoutInflater.inflate(R.layout.custom_dialog, null);
        builder.setView(view);

        Button okBtn = (Button) view.findViewById(R.id.yes_btn);
        TextView tvTitle = (TextView) view.findViewById(R.id.dialog_title);
        TextView tvDesc = (TextView) view.findViewById(R.id.dialog_content);

        tvTitle.setText(titleMsg);
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0);
        tvTitle.setCompoundDrawablePadding(5);
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
                if (success) {
                    Intent intent = new Intent();
                    intent.putExtra(Constants.CURRENT_BALANCE, value);
                    setResult(RESULT_OK, intent);
                }
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 33 && resultCode == Activity.RESULT_OK) {
            Contact selectedContactToTransfer = data.getParcelableExtra(Constants.SELECTED_CONTACT_TO_TRANSFER);
            enteredPhoneNumber.setText(selectedContactToTransfer.getPhoneNo());
        }
    }

    private String decimalFormat(double mValue) {
        DecimalFormat format = new DecimalFormat("#");
        format.setMinimumFractionDigits(2);
        return format.format(mValue);
    }
}
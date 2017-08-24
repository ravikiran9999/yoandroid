package com.yo.android.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yo.android.R;
import com.yo.android.helpers.Settings;
import com.yo.android.photo.TextDrawable;
import com.yo.android.photo.util.ColorGenerator;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;
import com.yo.android.vox.BalanceHelper;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class TransferBalanceActivity extends BaseActivity {

    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private String name;
    private String phoneNo;
    String profilePic;

    @Bind(R.id.enter_contact_view)
    RelativeLayout enterNumberView;
    @Bind(R.id.et_amount)
    EditText etAmount;
    @Bind(R.id.et_enter_phone)
    EditText enteredPhoneNumber;
    @Bind(R.id.txt_balance)
    TextView tvBalance;
    @Bind(R.id.contact_view)
    RelativeLayout contactNumberView;
    @Bind(R.id.tv_phone_number)
    TextView tvPhoneNumber;
    @Bind(R.id.tv_contact_email)
    TextView tvContactMail;
    @Bind(R.id.imv_contact_pic)
    CircleImageView imvProfilePic;


    @Inject
    protected BalanceHelper mBalanceHelper;

    private String currencySymbol;

    public static void start(Activity activity, String currencySymbol, String availableBalance, String fullName, String phoneNo, String userAvatar, String userId, boolean userType) {
        Intent intent = createIntent(activity, currencySymbol, availableBalance, fullName, phoneNo, userAvatar, userId,  userType);
        activity.startActivityForResult(intent, 22);
    }

    public static void start(Activity activity, String currencySymbol, String availableBalance, boolean userType) {
        Intent intent = createIntent(activity, currencySymbol, availableBalance, userType);
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

    private static Intent createIntent(Activity activity, String currencySymbol, String availableBalance, boolean userType) {

        Intent intent = new Intent(activity, TransferBalanceActivity.class);
        intent.putExtra(Constants.CURRENT_BALANCE, availableBalance);
        intent.putExtra(Constants.USER_TYPE, userType);
        intent.putExtra("currencySymbol", currencySymbol);
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
        currencySymbol = getIntent().getStringExtra("currencySymbol");
        boolean userType = getIntent().getBooleanExtra(Constants.USER_TYPE, false);

        if(userType) {
            enterNumberView.setVisibility(View.VISIBLE);
            contactNumberView.setVisibility(View.GONE);
        } else {
            enterNumberView.setVisibility(View.GONE);
            contactNumberView.setVisibility(View.VISIBLE);
            userSelectedFromContacts();
        }

        tvBalance.setText(String.format("%s", balance));

        EventBus.getDefault().register(this);


    }

    private void userSelectedFromContacts() {

        name = getIntent().getStringExtra(Constants.USER_NAME);
        phoneNo = getIntent().getStringExtra(Constants.PHONE_NUMBER);
        profilePic = getIntent().getStringExtra("profilePic");
        String id = getIntent().getStringExtra("id");

        if (!TextUtils.isEmpty(name)) {
            tvPhoneNumber.setText(name);
            tvPhoneNumber.setVisibility(View.VISIBLE);
        } else {
            tvPhoneNumber.setVisibility(View.GONE);
        }
        tvPhoneNumber.setText(name);

        if ((name != null) && (!name.replaceAll("\\s+", "").equalsIgnoreCase(phoneNo))) {
            tvContactMail.setText(phoneNo);
            tvContactMail.setVisibility(View.VISIBLE);

        } else {
            tvContactMail.setVisibility(View.GONE);
        }
        TextDrawable.IBuilder mDrawableBuilder = TextDrawable.builder().round();

        if (!TextUtils.isEmpty(profilePic)) {

            Glide.with(this)
                    .load(profilePic)
                    .fitCenter()
                    .placeholder(R.drawable.dynamic_profile)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.drawable.dynamic_profile)
                    .into(imvProfilePic);
        } else if (Settings.isTitlePicEnabled) {
            if (name != null && name.length() >= 1) {
                String title1 = String.valueOf(name.charAt(0)).toUpperCase();
                Pattern p = Pattern.compile("^[a-zA-Z]");
                Matcher m = p.matcher(title1);
                boolean b = m.matches();
                if (b) {
                    Drawable drawable = mDrawableBuilder.build(title1, mColorGenerator.getRandomColor());
                    imvProfilePic.setImageDrawable(drawable);
                } else {
                    loadAvatarImage(imvProfilePic);
                }
            }
        } else {
            loadAvatarImage(imvProfilePic);
        }
    }

    @OnClick(R.id.btn_transfer)
    public void balanceTransfer() {
        //Check current balance and call is going on cases before transfer.
        String amount = etAmount.getText().toString();
        String phoneNumber = enteredPhoneNumber.getText().toString();
        String mPhoneNumber = phoneNo != null ? phoneNo : phoneNumber;
        if (!TextUtils.isEmpty(amount.trim())) {
            double val = Double.parseDouble(amount.trim());
            if (val != 0) {
                if (Double.parseDouble(Util.numberFromNexgeFormat(mBalanceHelper.getCurrentBalance())) > Double.parseDouble(Util.numberFromNexgeFormat(amount))) {

                    showMessageDialog(amount, mBalanceHelper.getCurrentBalance(), mPhoneNumber);

                } else if(Double.parseDouble(mBalanceHelper.getCurrentBalance()) == Double.parseDouble(amount)) {
                    showBalanceDialog();
                } else {
                    mToastFactory.showToast(R.string.insufficient_amount);
                }
            } else {
                mToastFactory.showToast(getResources().getString(R.string.enter_valid_amount));
            }
        } else {
            mToastFactory.showToast(getResources().getString(R.string.enter_amount_to_transfer));
        }
    }

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
            Glide.with(this)
                    .load("")
                    .fitCenter()
                    .placeholder(R.drawable.dynamic_profile)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.drawable.dynamic_profile)
                    .into(imvProfilePic);
        }
    }

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
                            int statusCode = Integer.parseInt(response.body().getCode());
                            switch (statusCode) {
                                case 200:
                                    String mName = name != null ? name : phoneNo;
                                    showAlertDialog(response.body().getBalance(), getString(R.string.successful_transfer, mName));
                                    break;
                                case 606:
                                    mToastFactory.showToast(response.body().getData().toString());
                                    break;
                                case 607:
                                    mToastFactory.showToast(response.body().getData().toString());
                                    break;
                                case 608:
                                    mToastFactory.showToast(response.body().getData().toString());
                                    break;
                                case 609:
                                    mToastFactory.showToast(response.body().getData().toString());
                                    break;
                                case 610:
                                    mToastFactory.showToast(response.body().getData().toString());
                                    break;
                                case 611:
                                    mToastFactory.showToast(response.body().getData().toString());
                                    break;
                                case 612:
                                    mToastFactory.showToast(response.body().getData().toString());
                                    break;
                                case 613:
                                    mToastFactory.showToast(response.body().getData().toString());
                                    break;
                                case 614:
                                    mToastFactory.showToast(response.body().getData().toString());
                                    break;
                                case 616:
                                    mToastFactory.showToast(response.body().getData().toString());
                                    break;
                                default:
                                    mToastFactory.showToast(R.string.transfer_balance_failed);
                                    break;
                            }
                        } catch (ClassCastException e) {
                            mLog.d("ClassCastException", e.getMessage());
                        }
                    } else {
                        mToastFactory.showToast(R.string.transfer_balance_failed);
                    }
                } else {
                    mToastFactory.showToast(R.string.transfer_balance_failed);
                }
            }

            @Override
            public void onFailure(Call<com.yo.android.model.Response> call, Throwable t) {
                dismissProgressDialog();

                mToastFactory.showToast(R.string.transfer_balance_failed);

            }
        });
    }

    private void showMessageDialog(final String amount, final String amountWithDenomination, final String phoneNumber) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        final View view = layoutInflater.inflate(R.layout.custom_dialog, null);
        builder.setView(view);

        TextView textView = (TextView) view.findViewById(R.id.dialog_content);
        String mAmount = Util.addDenomination(amount, amountWithDenomination);
        String confirmationText;

        if(name != null) {
            confirmationText = getString(R.string.transfer_balance_alert, mAmount, name, phoneNumber);
        } else{
            confirmationText = getString(R.string.transfer_balance_alert_number, mAmount, phoneNumber);
        }

        textView.setText(confirmationText);


        Button yesBtn = (Button) view.findViewById(R.id.yes_btn);
        yesBtn.setText(getResources().getString(R.string.yes));
        Button noBtn = (Button) view.findViewById(R.id.no_btn);


        final AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
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
                            double val = Double.parseDouble(format.trim());
                            if (val <= 2) {
                                mLog.w("TransferBalanceActivity", "Current balance is less than or equal to $2");
                                Util.setBigStyleNotificationForBalance(TransferBalanceActivity.this, "Credit", "You are having insufficient balance in your account. Please add balance.", "Credit", "");

                            }
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

    private void showBalanceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.cannot_transfer_full_balance)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showAlertDialog(String value, String titleMsg) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(TransferBalanceActivity.this);

        LayoutInflater layoutInflater = LayoutInflater.from(TransferBalanceActivity.this);
        final View view = layoutInflater.inflate(R.layout.transfer_balance_dialog, null);
        builder.setView(view);

        Button okBtn = (Button) view.findViewById(R.id.yes_btn);
        TextView tvTitle = (TextView) view.findViewById(R.id.dialog_title);
        TextView tvDesc = (TextView) view.findViewById(R.id.dialog_content);

        tvTitle.setText(titleMsg);
        if(value != null) {
            String remainingBalance = "\"Your Remaining Balance is " + value + "\"";
            final SpannableString text = new SpannableString(remainingBalance);
            text.setSpan(new ForegroundColorSpan(Color.RED), 27, text.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvDesc.setText(text);
        }

        final AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                setResult(RESULT_OK);
                finish();
            }
        });
    }
}
package com.yo.android.ui;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yo.android.R;
import com.yo.android.calllogs.CallLog;
import com.yo.android.helpers.RegisteredContactsViewHolder;
import com.yo.android.helpers.Settings;
import com.yo.android.photo.TextDrawable;
import com.yo.android.photo.util.ColorGenerator;
import com.yo.android.util.Constants;
import com.yo.android.vox.BalanceHelper;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransferBalanceActivity extends BaseActivity {

    private TextDrawable.IBuilder mDrawableBuilder;
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private EditText etAmount;
    private String name;
    private String id;
    @Inject
    BalanceHelper balanceHelper;
    private TextView tvBalance;
    private String currencySymbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_balance);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String title = getString(R.string.transfer_balance);

        getSupportActionBar().setTitle(title);

        String balance = getIntent().getStringExtra("balance");
        currencySymbol = getIntent().getStringExtra("currencySymbol");
        name = getIntent().getStringExtra("name");
        final String phoneNo = getIntent().getStringExtra("phoneNo");
        String profilePic = getIntent().getStringExtra("profilePic");
        id = getIntent().getStringExtra("id");

        tvBalance = (TextView) findViewById(R.id.txt_balance);
        tvBalance.setText(String.format("%s%s", currencySymbol, balance));

        TextView tvPhoneNumber = (TextView) findViewById(R.id.tv_phone_number);

        TextView tvContactMail = (TextView) findViewById(R.id.tv_contact_email);

        CircleImageView imvProfilePic = (CircleImageView) findViewById(R.id.imv_contact_pic);

        Button btnTransfer = (Button) findViewById(R.id.btn_transfer);

        etAmount = (EditText) findViewById(R.id.et_amount);

        EventBus.getDefault().register(this);

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
        mDrawableBuilder = TextDrawable.builder()
                .round();
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
                    Drawable drawable = mDrawableBuilder.build(title, mColorGenerator.getRandomColor());
                    imvProfilePic.setImageDrawable(drawable);
                } else {
                    loadAvatarImage(imvProfilePic);
                }
            }
        } else {
            loadAvatarImage(imvProfilePic);
        }

        btnTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amount = etAmount.getText().toString();

                if (!TextUtils.isEmpty(amount.trim())) {
                    double val = Double.parseDouble(amount.trim());
                    if (val != 0) {
                        showMessageDialog(amount, phoneNo);
                    } else {
                        mToastFactory.showToast("Please enter a valid amount to transfer");
                    }
                } else {
                    mToastFactory.showToast("Please enter an amount to transfer");
                }

            }
        });
    }

    private void loadAvatarImage(CircleImageView imvProfilePic) {
        Drawable tempImage = getResources().getDrawable(R.drawable.dynamic_profile);
        LayerDrawable bgDrawable = (LayerDrawable) tempImage;
        final GradientDrawable shape = (GradientDrawable) bgDrawable.findDrawableByLayerId(R.id.shape_id);
        if (Settings.isTitlePicEnabled) {
            shape.setColor(mColorGenerator.getRandomColor());
        }
        if (imvProfilePic.getTag() == null) {
            imvProfilePic.setTag(Settings.imageTag, tempImage);
        }
        imvProfilePic.setImageDrawable((Drawable) imvProfilePic.getTag(Settings.imageTag));
    }

    private void transferBalance(String amount) {
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        showProgressDialog();
        yoService.balanceTransferAPI(accessToken, id, amount).enqueue(new Callback<com.yo.android.model.Response>() {
            @Override
            public void onResponse(Call<com.yo.android.model.Response> call, Response<com.yo.android.model.Response> response) {
                dismissProgressDialog();

                if (response.isSuccessful()) {

                    if (response.code() == 200) {
                        try {
                            int statusCode = Integer.parseInt(response.body().getCode());
                            switch (statusCode) {
                                case 200:
                                    final AlertDialog.Builder builder = new AlertDialog.Builder(TransferBalanceActivity.this);

                                    LayoutInflater layoutInflater = LayoutInflater.from(TransferBalanceActivity.this);
                                    final View view = layoutInflater.inflate(R.layout.transfer_balance_dialog, null);
                                    builder.setView(view);

                                    Button okBtn = (Button) view.findViewById(R.id.yes_btn);
                                    TextView tvTitle = (TextView) view.findViewById(R.id.dialog_title);
                                    TextView tvDesc = (TextView) view.findViewById(R.id.dialog_content);

                                    tvTitle.setText("Balance has been transferred successfully to " + "\"" + name + "\".");
                                    DecimalFormat df = new DecimalFormat("0.000");
                                    String format = df.format(Double.valueOf(response.body().getBalance()));
                                    String remainingBalance = "\"Your Remaining Balance is $" + format + "\"";
                                    final SpannableString text = new SpannableString(remainingBalance);
                                    text.setSpan(new ForegroundColorSpan(Color.RED), 27, text.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    tvDesc.setText(text);

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

                                    break;
                                case 606:
                                    mToastFactory.showToast(R.string.invalid_request);
                                    break;
                                case 607:
                                    mToastFactory.showToast(R.string.invalid_pin_request);
                                    break;
                                case 608:
                                    mToastFactory.showToast(R.string.insufficient_amount);
                                    break;
                                case 609:
                                    mToastFactory.showToast(R.string.unsuccessful_amount_transfer);
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

    private void showMessageDialog(final String amount, final String phoneNumber) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        final View view = layoutInflater.inflate(R.layout.custom_dialog, null);
        builder.setView(view);

        TextView textView = (TextView) view.findViewById(R.id.dialog_content);
        DecimalFormat df = new DecimalFormat("0.000");
        String format = df.format(Double.valueOf(amount));
        String confirmationText = "Are you sure you want to transfer $" + format + " balance to " + name + " with number " + phoneNumber + "?";
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
                transferBalance(amount);
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
            if (balanceHelper != null) {
                showProgressDialog();
                balanceHelper.checkBalance(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        dismissProgressDialog();
                        try {
                            DecimalFormat df = new DecimalFormat("0.000");
                            String format = df.format(Double.valueOf(balanceHelper.getCurrentBalance()));
                            preferenceEndPoint.saveStringPreference(Constants.CURRENT_BALANCE, format);
                            tvBalance.setText(String.format("%s%s", currencySymbol, format));
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
}

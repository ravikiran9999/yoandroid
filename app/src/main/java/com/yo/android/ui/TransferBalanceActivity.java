package com.yo.android.ui;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yo.android.R;
import com.yo.android.helpers.RegisteredContactsViewHolder;
import com.yo.android.helpers.Settings;
import com.yo.android.photo.TextDrawable;
import com.yo.android.photo.util.ColorGenerator;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class TransferBalanceActivity extends BaseActivity {

    private TextDrawable.IBuilder mDrawableBuilder;
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_balance);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String title = getString(R.string.transfer_balance);

        getSupportActionBar().setTitle(title);

        String balance = getIntent().getStringExtra("balance");
        String currencySymbol = getIntent().getStringExtra("currencySymbol");
        String name = getIntent().getStringExtra("name");
        String phoneNo = getIntent().getStringExtra("phoneNo");
        String profilePic = getIntent().getStringExtra("profilePic");

        TextView tvBalance = (TextView) findViewById(R.id.txt_balance);
        tvBalance.setText(String.format("%s%s", currencySymbol, balance));

        TextView tvPhoneNumber = (TextView) findViewById(R.id.tv_phone_number);

        TextView tvContactMail = (TextView) findViewById(R.id.tv_contact_email);

        CircleImageView imvProfilePic = (CircleImageView) findViewById(R.id.imv_contact_pic);

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
            if (name!= null && name.length() >= 1) {
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
    }

    private void loadAvatarImage(CircleImageView imvProfilePic) {
        Drawable tempImage = getResources().getDrawable(R.drawable.dynamic_profile);
        LayerDrawable bgDrawable = (LayerDrawable) tempImage;
        final GradientDrawable shape = (GradientDrawable) bgDrawable.findDrawableByLayerId(R.id.shape_id);
        if (Settings.isTitlePicEnabled) {
            shape.setColor(mColorGenerator.getRandomColor());
        }
        imvProfilePic.setImageDrawable(tempImage);
    }
}

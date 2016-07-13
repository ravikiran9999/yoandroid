package com.yo.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.orion.android.common.util.ConnectivityHelper;
import com.yo.android.R;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;
import com.yo.android.voip.DialPadView;
import com.yo.android.voip.OutGoingCallActivity;
import com.yo.android.voip.SipService;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Ramesh on 27/6/16.
 */
public class DialerActivity extends BaseActivity {
    private DialPadView dialPadView;
    private ImageButton deleteButton;
    private static final int[] mButtonIds = new int[]{R.id.zero, R.id.one, R.id.two, R.id.three,
            R.id.four, R.id.five, R.id.six, R.id.seven, R.id.eight, R.id.nine, R.id.star,
            R.id.pound};
    private ImageView btnCallGreen;
    private ImageView btnDialer;
    private TextView txtBalance;
    private TextView txtCallRate;
    private View bottom_layout;
    private boolean show;
    @Inject
    ConnectivityHelper mConnectivityHelper;
    @Inject
    @Named("voip_support")
    boolean isVoipSupported;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialer);
        getSupportActionBar().setTitle(R.string.activity_title_dialer);
        enableBack();
        //
        Intent in = new Intent(getBaseContext(), SipService.class);
        startService(in);

        dialPadView = (DialPadView) findViewById(R.id.dialPadView);
        bottom_layout = findViewById(R.id.bottom_layout);
        txtBalance = (TextView) findViewById(R.id.txt_balance);
        txtCallRate = (TextView) findViewById(R.id.txt_call_rate);
        btnCallGreen = (ImageView) findViewById(R.id.btnCall);
        btnDialer = (ImageView) findViewById(R.id.btnDialer);
        findViewById(R.id.btnMessage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mToastFactory.showToast("Message: Need to implement");
            }
        });
        findViewById(R.id.btnContacts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mToastFactory.showToast("Contacts: Need to implement");
            }
        });
        deleteButton = (ImageButton) findViewById(R.id.deleteButton);
        for (int id : mButtonIds) {
            dialPadView.findViewById(id).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView numberView = (TextView) v.findViewById(R.id.dialpad_key_number);
                    String prev = dialPadView.getDigits().getText().toString();
                    String current = prev + numberView.getText().toString();
                    dialPadView.getDigits().setText(current);
                    dialPadView.getDigits().setSelection(current.length());
                }
            });
        }
        btnDialer.setVisibility(View.GONE);
        btnDialer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialPad();
            }
        });
        btnCallGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String number = dialPadView.getDigits().getText().toString().trim();
                if (!isVoipSupported) {
                    mToastFactory.newToast(getString(R.string.voip_not_supported_error_message), Toast.LENGTH_SHORT);
                } else if (!mConnectivityHelper.isConnected()) {
                    mToastFactory.showToast(getString(R.string.connectivity_network_settings));
                } else if (!isVoipSupported) {
                    mToastFactory.newToast(getString(R.string.voip_not_supported_error_message), Toast.LENGTH_LONG);
                } else if (number.length() == 0) {
                    mToastFactory.showToast("Please enter number.");
                } else {
                    Intent intent = new Intent(DialerActivity.this, OutGoingCallActivity.class);
                    intent.putExtra(OutGoingCallActivity.CALLER_NO, number);
                    startActivity(intent);
                }
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String prev = dialPadView.getDigits().getText().toString();
                String finalString = prev;
                int startPos = dialPadView.getDigits().getSelectionStart();
                int endPos = dialPadView.getDigits().getSelectionEnd();
                try {
                    String str = new StringBuilder(prev).replace(startPos - 1, endPos, "").toString();
                    mLog.i("Dialer", "final:" + str);
                    dialPadView.getDigits().setText(str);
                    dialPadView.getDigits().setSelection(startPos - 1);
                } catch (Exception e) {
                    mLog.w("DialerActivity", e);
                }

            }
        });
        String balance = preferenceEndPoint.getStringPreference(Constants.CURRENT_BALANCE, "2.0");
        txtBalance.setText("Balance $" + balance);
        //
        setCallRateText();
        txtCallRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(DialerActivity.this, CountryListActivity.class), 100);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_dialer, menu);
        Util.prepareSearch(this, menu, null);
        return super.onCreateOptionsMenu(menu);
    }


    private void showDialPad() {
        show = true;
        dialPadView.setVisibility(View.VISIBLE);
        Animation bottomUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bottom_up);
        bottomUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                dialPadView.setVisibility(View.VISIBLE);
                btnCallGreen.setVisibility(View.VISIBLE);
                bottom_layout.setVisibility(View.VISIBLE);
                btnDialer.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        dialPadView.startAnimation(bottomUp);
    }

    private void hideDialPad() {
        show = false;
        Animation bottomUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bottom_down);
        dialPadView.startAnimation(bottomUp);
        bottomUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                dialPadView.setVisibility(View.GONE);
                btnCallGreen.setVisibility(View.GONE);
                bottom_layout.setVisibility(View.GONE);
                btnDialer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        bottomUp.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            setCallRateText();
        }
    }

    private void setCallRateText() {
        String cName = preferenceEndPoint.getStringPreference(Constants.COUNTRY_NAME, null);
        String cRate = preferenceEndPoint.getStringPreference(Constants.COUNTRY_CALL_RATE, null);
        String cPulse = preferenceEndPoint.getStringPreference(Constants.COUNTRY_CALL_PULSE, null);
        String cPrefix = preferenceEndPoint.getStringPreference(Constants.COUNTRY_CODE_PREFIX, null);

        if (!TextUtils.isEmpty(cName)) {
            String pulse;
            if (cPulse.equals("60")) {
                pulse = "min";
            } else {
                pulse = "sec";
            }

            txtCallRate.setText(cName + "\n$" + cRate + "/" + pulse);
            if (!TextUtils.isEmpty(cPrefix)) {
                dialPadView.getDigits().setText(cPrefix);
                dialPadView.getDigits().setSelection(cPrefix.length());
            }
        }
    }
}

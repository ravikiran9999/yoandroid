package com.yo.android.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orion.android.common.util.ConnectivityHelper;
import com.yo.android.R;
import com.yo.android.helpers.Helper;
import com.yo.android.model.dialer.CallRateDetail;
import com.yo.android.pjsip.SipHelper;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;
import com.yo.android.voip.DialPadView;
import com.yo.android.vox.BalanceHelper;

import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by rajesh on 16/9/16.
 */
public class NewDailerActivity extends BaseActivity {

    private static final int OPEN_ADD_BALANCE_RESULT = 1000;
    private static final int PICK_CONTACT_REQUEST = 10001;


    private static final String TAG = NewDailerActivity.class.getSimpleName();
    @Inject
    ConnectivityHelper mConnectivityHelper;
    @Inject
    BalanceHelper mBalanceHelper;


    private static final int[] mButtonIds = new int[]{R.id.zero, R.id.one, R.id.two, R.id.three,
            R.id.four, R.id.five, R.id.six, R.id.seven, R.id.eight, R.id.nine, R.id.star,
            R.id.pound};
    @Bind(R.id.btnCall)
    protected ImageView btnCallGreen;

    @Bind(R.id.txt_balance)
    protected TextView txtBalance;

    @Bind(R.id.txt_call_rate)
    protected TextView txtCallRate;

    @Bind(R.id.country_name)
    protected TextView countryName;


    @Bind(R.id.dialPadView)
    protected DialPadView dialPadView;

    @Bind(R.id.add_person)
    protected ImageView addPerson;

    protected EditText mDigits;

    @Bind(R.id.deleteButton)
    protected ImageButton deleteButton;

    protected String sUserSimCountryCode;

    private List<CallRateDetail> callRateDetailList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newdialter);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.activity_title_dialer);
        ButterKnife.bind(this);
        String countryCode = preferenceEndPoint.getStringPreference(Constants.COUNTRY_CODE_FROM_SIM);

        mDigits = dialPadView.getDigits();

        registerChatOrPhoneBookClickListeners();
        for (int id : mButtonIds) {
            dialPadView.findViewById(id).setOnClickListener(keyPadButtonsClickListener());
        }
        String json = preferenceEndPoint.getStringPreference(Constants.COUNTRY_LIST);
        callRateDetailList = new Gson().fromJson(json, new TypeToken<List<CallRateDetail>>() {
        }.getType());
        dialPadView.findViewById(R.id.zero).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (dialPadView.getDigits().getText().toString().trim().length() == 0) {
                    dialPadView.getDigits().setText("+");
                    dialPadView.getDigits().setSelection(1);
                    return true;
                } else {
                    int startPos = dialPadView.getDigits().getSelectionStart();
                    int endPos = dialPadView.getDigits().getSelectionEnd();
                    //TODO: Number already entered without "+", need to add this symbol at first position
                }

                return false;
            }
        });
        btnCallGreen.setOnClickListener(btnCallGreenClickListener());
        deleteButton.setOnClickListener(btnDeleteClickListener());
        deleteButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                dialPadView.getDigits().setText("");
                return true;
            }
        });
        loadCurrentBalance();
        //Add Balance while tapping on balance.
        txtBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewDailerActivity.this, TabsHeaderActivity.class);
                intent.putExtra(Constants.OPEN_ADD_BALANCE, true);
                startActivityForResult(intent, OPEN_ADD_BALANCE_RESULT);
            }
        });
        Drawable drawable = getResources().getDrawable(R.drawable.ic_add_new_contact);
        drawable.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.MULTIPLY);
        addPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.createNewContactWithPhoneNumber(NewDailerActivity.this, mDigits.getText().toString());
            }
        });
        //
        setCallRateText();
        countryName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(NewDailerActivity.this, CountryListActivity.class), 100);
            }
        });

        dialPadView.getDigits().addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (s.length() != 0) {
                    if (callRateDetailList != null) {
                        for (CallRateDetail callRateDetail : callRateDetailList) {
                            try {
                                String plus = "+" + callRateDetail.getPrefix();
                                String zero = "00" + callRateDetail.getPrefix();
                                if ((s.length() >= plus.length() &&
                                        s.toString().trim().subSequence(0, plus.length()).equals(plus)) ||
                                        s.length() >= zero.length() && s.toString().trim().subSequence(0, zero.length()).equals(zero)) {
                                    preferenceEndPoint.saveStringPreference(Constants.COUNTRY_CALL_RATE, Util.removeTrailingZeros(callRateDetail.getRate()));
                                    preferenceEndPoint.saveStringPreference(Constants.COUNTRY_NAME, callRateDetail.getDestination());
                                    preferenceEndPoint.saveStringPreference(Constants.COUNTRY_CALL_PULSE, callRateDetail.getPulse());
                                    preferenceEndPoint.saveStringPreference(Constants.COUNTRY_CODE_PREFIX, "+" + callRateDetail.getPrefix());
                                    setCallRateText();
                                }
                            } catch (StringIndexOutOfBoundsException e) {
                                mLog.w(TAG, e);
                            }
                        }
                    } else {
                        callRateDetailList = (List<CallRateDetail>) dialPadView.getTag();
                    }

                }
            }
        });
    }

    private void loadCurrentBalance() {
        String balance = preferenceEndPoint.getStringPreference(Constants.CURRENT_BALANCE, "2.0");
        if (mBalanceHelper != null) {
            if (mBalanceHelper.getCurrentBalance() != null && mBalanceHelper.getCurrencySymbol() != null) {
                txtBalance.setText(String.format("%s %s%s", getString(R.string.balance), mBalanceHelper.getCurrencySymbol(), mBalanceHelper.getCurrentBalance()));
            } else {
                txtBalance.setVisibility(View.GONE);
            }
        } else if (balance != null) {
            txtBalance.setText(String.format("%s %s", getString(R.string.balance), balance));
        } else {
            txtBalance.setVisibility(View.GONE);
        }
    }

    @NonNull
    private View.OnClickListener keyPadButtonsClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView numberView = (TextView) v.findViewById(R.id.dialpad_key_number);
                String keyPadText = numberView.getText().toString();
                updateDialString(keyPadText.charAt(0));
            }
        };
    }

    @NonNull
    private View.OnClickListener btnDeleteClickListener() {
        return new View.OnClickListener() {
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
        };
    }

    @NonNull
    private View.OnClickListener btnCallGreenClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Need to improve logic for PSTN calls
                //Begin Normalizing PSTN number
                String temp = dialPadView.getDigits().getText().toString().trim();
                String cPrefix = preferenceEndPoint.getStringPreference(Constants.COUNTRY_CODE_PREFIX, null);
                boolean prefixrequired;
                String number;
                if (temp.startsWith("+")) {
                    prefixrequired = false;
                    number = temp;
                } else if (!TextUtils.isEmpty(cPrefix)) {
                    number = cPrefix + temp;
                } else {
                    number = temp;
                }
                number = number.replace(" ", "").replace("+", "");
                if (cPrefix != null) {
                    cPrefix = cPrefix.replace("+", "");
                }
                mLog.i(TAG, "Dialing number after normalized: " + number);
                //End Normalizing PSTN number
                if (!mConnectivityHelper.isConnected()) {
                    mToastFactory.showToast(getString(R.string.connectivity_network_settings));
                } else if (number.length() == 0) {
                    mToastFactory.showToast("Please enter number.");
                } else {
                    SipHelper.makeCall(NewDailerActivity.this, number);
                    finish();
                }
            }
        };
    }

    /**
     * Updates the dial string (mDigits) after inserting a Pause character (,)
     * or Wait character (;).
     */
    private void updateDialString(char newDigit) {

        int selectionStart;
        int selectionEnd;

        // SpannableStringBuilder editable_text = new SpannableStringBuilder(mDigits.getText());
        int anchor = mDigits.getSelectionStart();
        int point = mDigits.getSelectionEnd();

        selectionStart = Math.min(anchor, point);
        selectionEnd = Math.max(anchor, point);

        if (selectionStart == -1) {
            selectionStart = selectionEnd = mDigits.length();
        }

        Editable digits = mDigits.getText();

        if (canAddDigit(digits, selectionStart, selectionEnd)) {
            digits.replace(selectionStart, selectionEnd, Character.toString(newDigit));

            if (selectionStart != selectionEnd) {
                // Unselect: back to a regular cursor, just pass the character inserted.
                mDigits.setSelection(selectionStart + 1);
            }
        }
    }


    private boolean canAddDigit(CharSequence digits, int start, int end) {
        // False if no selection, or selection is reversed (end < start)
        if (start == -1 || end < start) {
            return false;
        }
        // unsupported selection-out-of-bounds state
        if (start > digits.length() || end > digits.length()) return false;

        // Special digit cannot be the first digit
        if (start == 0) return true;
        return true;
    }

    private void loadDefaultSimCountry() {
        final TelephonyManager manager = (TelephonyManager) getSystemService(
                Context.TELEPHONY_SERVICE);
        if (manager != null) {
            sUserSimCountryCode = manager.getSimCountryIso();
        }
    }

    private void registerChatOrPhoneBookClickListeners() {
        findViewById(R.id.btnMessage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NewDailerActivity.this, PhoneChatActivity.class));
            }
        });
        findViewById(R.id.btnContacts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NewDailerActivity.this, PhoneBookActivity.class));
            }
        });
    }

    private String setCallRateText() {
        String cName = preferenceEndPoint.getStringPreference(Constants.COUNTRY_NAME, null);
        String cRate = preferenceEndPoint.getStringPreference(Constants.COUNTRY_CALL_RATE, null);
        String cPulse = preferenceEndPoint.getStringPreference(Constants.COUNTRY_CALL_PULSE, null);
        String cPrefix = preferenceEndPoint.getStringPreference(Constants.COUNTRY_CODE_PREFIX, null);
        if (cName == null) {
            String prefixWhileLogin = preferenceEndPoint.getStringPreference(Constants.COUNTRY_CODE_FROM_SIM);
            for (CallRateDetail details : callRateDetailList) {
                if (details.getPrefix().equalsIgnoreCase(prefixWhileLogin)) {
                    cName = details.getDestination();
                    cRate = details.getRate();
                    cPulse = details.getPulse();
                    cPrefix = "+" + details.getPrefix();
                    break;
                }
            }
        }


        if (!TextUtils.isEmpty(cName)) {
            String pulse;
            if (cPulse.equals("60")) {
                pulse = "min";
            } else {
                pulse = "sec";
            }
            if (cName != null) {
                countryName.setText(cName.toUpperCase());
            }
            txtCallRate.setText("$" + cRate + "/" + pulse);
            if (TextUtils.isEmpty(dialPadView.getDigits().getText().toString())) {
                //TODO: Need to improve the logic
                String str = dialPadView.getDigits().getText().toString();
                str = str.substring(str.indexOf(" ") + 1);
                dialPadView.getDigits().setText(cPrefix + " " + str);
                dialPadView.getDigits().setSelection(cPrefix.length());
            }
        }
        return cPrefix;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            String cPrefix = setCallRateText();
            //TODO: Need to improve the logic
            String str = dialPadView.getDigits().getText().toString();
            str = str.substring(str.indexOf(" ") + 1);
            dialPadView.getDigits().setText(cPrefix + " " + str);
            dialPadView.getDigits().setSelection(cPrefix.length());
        } else if (requestCode == OPEN_ADD_BALANCE_RESULT && resultCode == Activity.RESULT_OK) {
            loadCurrentBalance();
        }
    }


}
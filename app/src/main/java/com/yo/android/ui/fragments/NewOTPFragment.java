package com.yo.android.ui.fragments;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.orion.android.common.util.ConnectivityHelper;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.chat.firebase.FireBaseAuthToken;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.model.Articles;
import com.yo.android.model.OTPResponse;
import com.yo.android.model.Subscriber;
import com.yo.android.pjsip.YoSipService;
import com.yo.android.ui.BottomTabsActivity;
import com.yo.android.ui.NewFollowMoreTopicsActivity;
import com.yo.android.ui.NewOTPActivity;
import com.yo.android.ui.UpdateProfileActivity;
import com.yo.android.util.Constants;
import com.yo.android.voip.DigitsEditText;
import com.yo.android.voip.IncomingSmsReceiver;
import com.yo.android.voip.VoipConstants;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by creatives on 7/31/2017.
 */

public class NewOTPFragment extends BaseFragment implements View.OnClickListener, View.OnFocusChangeListener, View.OnKeyListener, TextWatcher {

    private Handler mHandler = new Handler();
    private final static int MAX_DURATION = 60;
    private int duration = MAX_DURATION;
    private Handler dummyOTPHandler = new Handler();
    private String phoneNumber;
    @Inject
    YoApi.YoService yoService;
    @Inject
    ContactsSyncManager contactsSyncManager;
    @Inject
    ConnectivityHelper mHelper;
    private int count = 0;
    private boolean otpReceived = false;

    @Bind(R.id.tv_enter_otp)
    TextView tvEnterOTP;
    @Bind(R.id.tv_resend)
    TextView reSendTextBtn;
    @Bind(R.id.next_btn)
    Button nextBtn;

    private Button btnOne;
    private Button btnTwo;
    private Button btnThree;
    private Button btnFour;
    private Button btnFive;
    private Button btnSix;
    private Button btnSeven;
    private Button btnEight;
    private Button btnNine;
    private Button btnZero;
    private ImageButton imgBtnClear;
    private DigitsEditText etOtp;
    private EditText mPinFirstDigitEditText;
    private EditText mPinSecondDigitEditText;
    private EditText mPinThirdDigitEditText;
    private EditText mPinForthDigitEditText;
    private EditText mPinFifthDigitEditText;
    private EditText mPinSixthDigitEditText;
    private EditText mPinHiddenEditText;

    Activity activity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*Bundle phoneNumberBundle = this.getArguments();
        phoneNumber = phoneNumberBundle.getString(Constants.PHONE_NUMBER);*/
        phoneNumber = getActivity().getIntent().getExtras().getString(Constants.PHONE_NUMBER);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {
            activity = (Activity) context;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.new_otp_layout, container, false);
        ButterKnife.bind(this, view);

        View gvKeypad = view.findViewById(R.id.gv_keypad);
        View llPin = view.findViewById(R.id.pin_content_layout);
        etOtp = (DigitsEditText) view.findViewById(R.id.et_otp);

        // View viewDigits = inflater.inflate(R.layout.otp_keypad, container, false);
        initDigitsViews(gvKeypad);
        init(llPin);
        setPINListeners();

        tvEnterOTP.setText(String.format(getString(R.string.enter_the_code), phoneNumber));

        mHandler.post(runnable);

        return view;
    }

    @OnClick(R.id.next_btn)
    public void nextButton() {
        performNext();
    }

    @OnClick(R.id.tv_resend)
    public void resendOtp() {
        if ("Resend".equalsIgnoreCase(reSendTextBtn.getText().toString()) && getActivity() != null) {
            ((NewOTPActivity) getActivity()).callLoginService(phoneNumber);
            generateDummyOTP();
            mHandler.post(runnable);
        }
    }

    private void performNext() {
        String password = mPinHiddenEditText.getText().toString().trim();
        if (TextUtils.isEmpty(password)) {
            mToastFactory.showToast(getString(R.string.otp_empty));
        } else {
            stopTimer();
            signUp(phoneNumber, password);
        }
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        /*((NewOTPActivity) activity).getSupportActionBar().setHomeButtonEnabled(true);
        ((NewOTPActivity) activity).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((NewOTPActivity) activity).getSupportActionBar().setTitle(title);*/

        generateDummyOTP();
    }

    private Runnable dummyOTPRunnable = new Runnable() {
        @Override
        public void run() {
            dummyOTPHandler.removeCallbacks(this);
            stopTimer();
        }
    };

    private void generateDummyOTP() {
        //Debug purpose
        Random random = new Random();
        //int duration = Math.max(0, random.nextInt(60));
        int duration = Math.max(0, 60);
        if (!BuildConfig.ORIGINAL_SMS_VERIFICATION) {
            dummyOTPHandler.removeCallbacks(dummyOTPRunnable);
            dummyOTPHandler.postDelayed(dummyOTPRunnable, duration * 1000L);
        }
    }

    private void signUp(@NonNull final String phoneNumber, @NonNull final String password) {
        if (!mHelper.isConnected()) {
            mToastFactory.showToast(getActivity().getResources().getString(R.string.connectivity_network_settings));
            return;
        }
        count = 0;
        String countryCode = preferenceEndPoint.getStringPreference(Constants.COUNTRY_CODE_FROM_SIM);
        showProgressDialog();

        yoService.verifyOTP(BuildConfig.CLIENT_ID,
                BuildConfig.CLIENT_SECRET,
                "password", countryCode + phoneNumber, mPinHiddenEditText.getText().toString().trim()).enqueue(new Callback<OTPResponse>() {
            @Override
            public void onResponse(Call<OTPResponse> call, Response<OTPResponse> response) {
                //dismissProgressDialog();
                try {
                    if (response.isSuccessful()) {
                        preferenceEndPoint.saveBooleanPreference(Constants.SESSION_EXPIRE, false);
                        //contactsSyncManager.syncContacts();
                        count++;
                        storeTokens(response, phoneNumber, password);

                        generateFirebaseToken();

                        //finishAndNavigateToHome();
                        addSubscriber(response.body().getAccessToken());
                    } else {
                        if (activity != null) {
                            mToastFactory.showToast(getActivity().getResources().getString(R.string.otp_failure));
                        }
                    }
                } finally {
                    if (response != null && response.body() != null) {
                        try {
                            response = null;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<OTPResponse> call, Throwable t) {
                dismissProgressDialog();
                if (activity != null) {
                    if (!mHelper.isConnected() || t instanceof SocketTimeoutException) {
                        mToastFactory.showToast(activity.getResources().getString(R.string.connectivity_network_settings));
                    } else {
                        mToastFactory.showToast(activity.getResources().getString(R.string.otp_failure));
                    }
                }
            }
        });
    }

    private void addSubscriber(String accessToken) {
        yoService.subscribe(accessToken).enqueue(new Callback<Subscriber>() {
            @Override
            public void onResponse(Call<Subscriber> call, Response<Subscriber> response) {
                if (response.isSuccessful()) {
                    try {
                        preferenceEndPoint.saveStringPreference(Constants.SUBSCRIBER_ID, response.body().getNexge_subscriber_id());
                        preferenceEndPoint.saveStringPreference(Constants.CALLINGCARDNUMBER, response.body().getNexge_subscriber_telID());
                        preferenceEndPoint.saveStringPreference(Constants.VOX_USER_NAME, response.body().getNexge_subscriber_username());
                        preferenceEndPoint.saveStringPreference(Constants.PASSWORD, response.body().getNexge_subscriber_password());
                        finishAndNavigateToHome();
                    } finally {
                        if (response != null && response.body() != null) {
                            try {
                                response = null;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    mToastFactory.showToast(getActivity().getResources().getString(R.string.otp_failure));
                }
            }

            @Override
            public void onFailure(Call<Subscriber> call, Throwable t) {
                dismissProgressDialog();
                mToastFactory.showToast(getActivity().getResources().getString(R.string.otp_failure));
            }
        });
    }

    private void generateFirebaseToken() {
        FireBaseAuthToken.getInstance(activity).getFirebaseAuth(new FireBaseAuthToken.FireBaseAuthListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "FirebaseAuthToken Created");
            }

            @Override
            public void onFailed() {
                Log.i(TAG, "Failed FirebaseAuthToken");
            }
        });
    }

    private void storeTokens(Response<OTPResponse> response, @NonNull String phoneNumber, @NonNull String password) {
        preferenceEndPoint.saveStringPreference(YoApi.ACCESS_TOKEN, response.body().getAccessToken());
        preferenceEndPoint.saveStringPreference(YoApi.REFRESH_TOKEN, response.body().getRefreshToken());
        preferenceEndPoint.saveStringPreference(Constants.PHONE_NUMBER, phoneNumber);
    }

    private void finishAndNavigateToHome() {
        navigation();
    }

    private Callback<List<Articles>> callback = new Callback<List<Articles>>() {
        @Override
        public void onResponse(Call<List<Articles>> call, Response<List<Articles>> response) {
            dismissProgressDialog();

            if (response.body() != null && !response.body().isEmpty()) {
                if (!TextUtils.isEmpty(preferenceEndPoint.getStringPreference("cached_magazines"))) {
                    preferenceEndPoint.removePreference("cached_magazines");
                }
                preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(response.body()));
                navigation();

            } else {
                navigation();
            }

        }

        @Override
        public void onFailure(Call<List<Articles>> call, Throwable t) {
            if (t instanceof UnknownHostException) {
                mLog.e("Magazine", "Please check network settings");
            }
            dismissProgressDialog();
            navigation();
        }

    };

    private void navigation() {
        final boolean isNewUser = preferenceEndPoint.getBooleanPreference("isNewUser");
        final boolean balanceAdded = preferenceEndPoint.getBooleanPreference("balanceAdded");
        if (isNewUser) {
            //TODO:Enable flag for Profile
            preferenceEndPoint.saveBooleanPreference(Constants.ENABLE_PROFILE_SCREEN, true);
            preferenceEndPoint.saveBooleanPreference(Constants.LOGED_IN, true);
            Intent intent = new Intent(activity, UpdateProfileActivity.class);
            intent.putExtra(Constants.PHONE_NUMBER, phoneNumber);
            dismissProgressDialog();
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if (!balanceAdded) {
            mBalanceHelper.checkBalance(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    dismissProgressDialog();
                    Intent intent;
                    preferenceEndPoint.saveBooleanPreference(Constants.ENABLE_PROFILE_SCREEN, false);
                    preferenceEndPoint.saveBooleanPreference(Constants.ENABLE_FOLLOW_TOPICS_SCREEN, true);
                    preferenceEndPoint.saveBooleanPreference(Constants.LOGED_IN, true);
                    preferenceEndPoint.saveBooleanPreference(Constants.LOGED_IN_AND_VERIFIED, true);
                    /*if (!BuildConfig.NEW_FOLLOW_MORE_TOPICS) {
                        if (activity == null) {
                            activity = getActivity();
                        }
                        intent = new Intent(activity, FollowMoreTopicsActivity.class);
                    } else {*/
                        if (activity == null) {
                            activity = getActivity();
                        }
                        intent = new Intent(activity, NewFollowMoreTopicsActivity.class);
                    //}
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("From", "UpdateProfileActivity");
                    startActivity(intent);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    dismissProgressDialog();
                }
            });

        } else {
            startActivity(new Intent(activity, BottomTabsActivity.class));
        }
        //Start Sip service
        getActivity().startService(new Intent(activity, YoSipService.class));

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(VoipConstants.NEW_ACCOUNT_REGISTRATION);
        activity.sendBroadcast(broadcastIntent);

        activity.finish();
        EventBus.getDefault().post(Constants.FINISH_LOGIN_ACTIVITY_ACTION);
    }

    public void onEventMainThread(Bundle bundle) {
        if (otpReceived) {
            mLog.e("OTPFragment", "onEventMainThread: otp is already received!");
            return;
        }
        if (!isAdded() || activity == null) {
            mLog.e("OTPFragment", "onEventMainThread: activity is already destroyed");
            return;
        }
        otpReceived = true;
        stopTimer();
        if (mPinHiddenEditText.length() > 0) {
            clearOTP();
        }
        String otp = IncomingSmsReceiver.extractOTP(bundle);
        if (otp != null) {

            for (int i = 0; i < otp.length(); i++) {
                enterText(otp.charAt(i) + "");
            }

            nextBtn.performClick();
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            String timer = String.format("%d seconds left...", duration);
            formatDuration(duration);
            duration--;
            if (duration > 0) {
                mHandler.postDelayed(this, 1000);
            } else {
                reSendTextBtn.setText("Resend");
                reSendTextBtn.setEnabled(true);
                //Reset
                duration = MAX_DURATION;
                nextBtn.setEnabled(true);
                stopTimer();
            }
        }
    };

    private void formatDuration(int duration) {
        String str = duration + "";
        if (duration < 10) {
            str = "0" + duration;
        }
        reSendTextBtn.setText("(00:" + str + ")");
        reSendTextBtn.setEnabled(false);
    }

    private void stopTimer() {
        dummyOTPHandler.removeCallbacks(dummyOTPRunnable);
        mHandler.removeCallbacks(runnable);
        //Reset
        duration = MAX_DURATION;
        nextBtn.setEnabled(true);
        reSendTextBtn.setText("Resend");
        reSendTextBtn.setEnabled(true);
    }

    private void initDigitsViews(View viewDigits) {

        btnOne = (Button) viewDigits.findViewById(R.id.btn_one);
        btnTwo = (Button) viewDigits.findViewById(R.id.btn_two);
        btnThree = (Button) viewDigits.findViewById(R.id.btn_three);
        btnFour = (Button) viewDigits.findViewById(R.id.btn_four);
        btnFive = (Button) viewDigits.findViewById(R.id.btn_five);
        btnSix = (Button) viewDigits.findViewById(R.id.btn_six);
        btnSeven = (Button) viewDigits.findViewById(R.id.btn_seven);
        btnEight = (Button) viewDigits.findViewById(R.id.btn_eight);
        btnNine = (Button) viewDigits.findViewById(R.id.btn_nine);
        btnZero = (Button) viewDigits.findViewById(R.id.btn_zero);
        imgBtnClear = (ImageButton) viewDigits.findViewById(R.id.img_btn_clear);

        btnOne.setOnClickListener(this);
        btnTwo.setOnClickListener(this);
        btnThree.setOnClickListener(this);
        btnFour.setOnClickListener(this);
        btnFive.setOnClickListener(this);
        btnSix.setOnClickListener(this);
        btnSeven.setOnClickListener(this);
        btnEight.setOnClickListener(this);
        btnNine.setOnClickListener(this);
        btnZero.setOnClickListener(this);
        imgBtnClear.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_one:
                //updateDialString('1');
                //mPinHiddenEditText.setText("1");
                enterText("1");
                break;
            case R.id.btn_two:
                enterText("2");
                break;
            case R.id.btn_three:
                enterText("3");
                break;
            case R.id.btn_four:
                enterText("4");
                break;
            case R.id.btn_five:
                enterText("5");
                break;
            case R.id.btn_six:
                enterText("6");
                break;
            case R.id.btn_seven:
                enterText("7");
                break;
            case R.id.btn_eight:
                enterText("8");
                break;
            case R.id.btn_nine:
                enterText("9");
                break;
            case R.id.btn_zero:
                enterText("0");
                break;
            case R.id.img_btn_clear:
                if (mPinHiddenEditText.getText().length() == 6)
                    mPinSixthDigitEditText.setText("");
                else if (mPinHiddenEditText.getText().length() == 5)
                    mPinFifthDigitEditText.setText("");
                else if (mPinHiddenEditText.getText().length() == 4)
                    mPinForthDigitEditText.setText("");
                else if (mPinHiddenEditText.getText().length() == 3)
                    mPinThirdDigitEditText.setText("");
                else if (mPinHiddenEditText.getText().length() == 2)
                    mPinSecondDigitEditText.setText("");
                else if (mPinHiddenEditText.getText().length() == 1)
                    mPinFirstDigitEditText.setText("");

                if (mPinHiddenEditText.length() > 0)
                    mPinHiddenEditText.setText(mPinHiddenEditText.getText().subSequence(0, mPinHiddenEditText.length() - 1));
                break;
        }
    }

    private void enterText(String s) {
        String text = mPinHiddenEditText.getText().toString();
        String finalText = text.concat(s);
        mPinHiddenEditText.setText(finalText);
    }

    /**
     * Updates the dial string (mDigits) after inserting a Pause character (,)
     * or Wait character (;).
     */
    private void updateDialString(char newDigit) {

        int selectionStart;
        int selectionEnd;

        int anchor = etOtp.getSelectionStart();
        int point = etOtp.getSelectionEnd();

        selectionStart = Math.min(anchor, point);
        selectionEnd = Math.max(anchor, point);

        if (selectionStart == -1) {
            selectionStart = selectionEnd = etOtp.length();
        }

        Editable digits = etOtp.getText();

        if (canAddDigit(digits, selectionStart, selectionEnd)) {
            digits.replace(selectionStart, selectionEnd, Character.toString(newDigit));

            if (selectionStart != selectionEnd) {
                // Unselect: back to a regular cursor, just pass the character inserted.
                etOtp.setSelection(selectionStart + 1);
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

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    /**
     * Hides soft keyboard.
     *
     * @param editText EditText which has focus
     */
    public void hideSoftKeyboard(EditText editText) {
        if (editText == null)
            return;

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Service.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    /**
     * Initialize EditText fields.
     *
     * @param llPin
     */
    private void init(View llPin) {
        mPinFirstDigitEditText = (EditText) llPin.findViewById(R.id.pin_first_edittext);
        mPinSecondDigitEditText = (EditText) llPin.findViewById(R.id.pin_second_edittext);
        mPinThirdDigitEditText = (EditText) llPin.findViewById(R.id.pin_third_edittext);
        mPinForthDigitEditText = (EditText) llPin.findViewById(R.id.pin_forth_edittext);
        mPinFifthDigitEditText = (EditText) llPin.findViewById(R.id.pin_fifth_edittext);
        mPinSixthDigitEditText = (EditText) llPin.findViewById(R.id.pin_sixth_edittext);
        mPinHiddenEditText = (EditText) llPin.findViewById(R.id.pin_hidden_edittext);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        final int id = v.getId();
        switch (id) {
            case R.id.pin_first_edittext:
                if (hasFocus) {
                    setFocus(mPinHiddenEditText);
                    //showSoftKeyboard(mPinHiddenEditText);
                }
                break;

            case R.id.pin_second_edittext:
                if (hasFocus) {
                    setFocus(mPinHiddenEditText);
                    //showSoftKeyboard(mPinHiddenEditText);
                }
                break;

            case R.id.pin_third_edittext:
                if (hasFocus) {
                    setFocus(mPinHiddenEditText);
                    //showSoftKeyboard(mPinHiddenEditText);
                }
                break;

            case R.id.pin_forth_edittext:
                if (hasFocus) {
                    setFocus(mPinHiddenEditText);
                    //showSoftKeyboard(mPinHiddenEditText);
                }
                break;

            case R.id.pin_fifth_edittext:
                if (hasFocus) {
                    setFocus(mPinHiddenEditText);
                    //showSoftKeyboard(mPinHiddenEditText);
                }
                break;
            case R.id.pin_sixth_edittext:
                if (hasFocus) {
                    setFocus(mPinHiddenEditText);
                    //showSoftKeyboard(mPinHiddenEditText);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            final int id = v.getId();
            switch (id) {
                case R.id.pin_hidden_edittext:
                    if (keyCode == KeyEvent.KEYCODE_DEL) {
                        if (mPinHiddenEditText.getText().length() == 6)
                            mPinSixthDigitEditText.setText("");
                        else if (mPinHiddenEditText.getText().length() == 5)
                            mPinFifthDigitEditText.setText("");
                        else if (mPinHiddenEditText.getText().length() == 4)
                            mPinForthDigitEditText.setText("");
                        else if (mPinHiddenEditText.getText().length() == 3)
                            mPinThirdDigitEditText.setText("");
                        else if (mPinHiddenEditText.getText().length() == 2)
                            mPinSecondDigitEditText.setText("");
                        else if (mPinHiddenEditText.getText().length() == 1)
                            mPinFirstDigitEditText.setText("");

                        if (mPinHiddenEditText.length() > 0)
                            mPinHiddenEditText.setText(mPinHiddenEditText.getText().subSequence(0, mPinHiddenEditText.length() - 1));

                        return true;
                    }

                    break;

                default:
                    return false;
            }
        }

        return false;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        /*setDefaultPinBackground(mPinFirstDigitEditText);
        setDefaultPinBackground(mPinSecondDigitEditText);
        setDefaultPinBackground(mPinThirdDigitEditText);
        setDefaultPinBackground(mPinForthDigitEditText);
        setDefaultPinBackground(mPinFifthDigitEditText);
        setDefaultPinBackground(mPinSixthDigitEditText);*/

        if (s.length() == 0) {
            //setFocusedPinBackground(mPinFirstDigitEditText);
            mPinFirstDigitEditText.setText("");
        } else if (s.length() == 1) {
            //setFocusedPinBackground(mPinSecondDigitEditText);
            mPinFirstDigitEditText.setText(s.charAt(0) + "");
            mPinSecondDigitEditText.setText("");
            mPinThirdDigitEditText.setText("");
            mPinForthDigitEditText.setText("");
            mPinFifthDigitEditText.setText("");
        } else if (s.length() == 2) {
            //setFocusedPinBackground(mPinThirdDigitEditText);
            mPinSecondDigitEditText.setText(s.charAt(1) + "");
            mPinThirdDigitEditText.setText("");
            mPinForthDigitEditText.setText("");
            mPinFifthDigitEditText.setText("");
        } else if (s.length() == 3) {
            //setFocusedPinBackground(mPinForthDigitEditText);
            mPinThirdDigitEditText.setText(s.charAt(2) + "");
            mPinForthDigitEditText.setText("");
            mPinFifthDigitEditText.setText("");
        } else if (s.length() == 4) {
            //setFocusedPinBackground(mPinFifthDigitEditText);
            mPinForthDigitEditText.setText(s.charAt(3) + "");
            mPinFifthDigitEditText.setText("");
        } else if (s.length() == 5) {
            //setFocusedPinBackground(mPinSixthDigitEditText);
            mPinFifthDigitEditText.setText(s.charAt(4) + "");
            mPinSixthDigitEditText.setText("");
        } else if (s.length() == 6) {
            //setDefaultPinBackground(mPinSixthDigitEditText);
            mPinSixthDigitEditText.setText(s.charAt(5) + "");

            hideSoftKeyboard(mPinSixthDigitEditText);
        }
    }

    /**
     * Sets default PIN background.
     *
     * @param editText edit text to change
     *//*
    private void setDefaultPinBackground(EditText editText) {
        setViewBackground(editText, getResources().getDrawable(R.drawable.otp_small_underline));
    }*/

    /**
     * Sets focus on a specific EditText field.
     *
     * @param editText EditText to set focus on
     */
    public static void setFocus(EditText editText) {
        if (editText == null)
            return;

        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
    }

    /**
     * Sets focused PIN field background.
     *
     * @param editText edit text to change
     *//*
    private void setFocusedPinBackground(EditText editText) {
        setViewBackground(editText, getResources().getDrawable(R.drawable.otp_small_underline));
    }*/

    /**
     * Sets listeners for EditText fields.
     */
    private void setPINListeners() {
        mPinHiddenEditText.addTextChangedListener(this);

        mPinFirstDigitEditText.setOnFocusChangeListener(this);
        mPinSecondDigitEditText.setOnFocusChangeListener(this);
        mPinThirdDigitEditText.setOnFocusChangeListener(this);
        mPinForthDigitEditText.setOnFocusChangeListener(this);
        mPinFifthDigitEditText.setOnFocusChangeListener(this);
        mPinSixthDigitEditText.setOnFocusChangeListener(this);

        mPinFirstDigitEditText.setOnKeyListener(this);
        mPinSecondDigitEditText.setOnKeyListener(this);
        mPinThirdDigitEditText.setOnKeyListener(this);
        mPinForthDigitEditText.setOnKeyListener(this);
        mPinFifthDigitEditText.setOnKeyListener(this);
        mPinSixthDigitEditText.setOnKeyListener(this);
        mPinHiddenEditText.setOnKeyListener(this);
    }

    /**
     * Sets background of the view.
     * This method varies in implementation depending on Android SDK version.
     *
     * @param view       View to which set background
     * @param background Background to set to view
     */
    @SuppressWarnings("deprecation")
    public void setViewBackground(View view, Drawable background) {
        if (view == null || background == null)
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(background);
        } else {
            view.setBackgroundDrawable(background);
        }
    }

    /**
     * Shows soft keyboard.
     *
     * @param editText EditText which has focus
     */
    public void showSoftKeyboard(EditText editText) {
        if (editText == null)
            return;

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Service.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, 0);
    }

    private void clearOTP() {
        if (mPinHiddenEditText.getText().length() == 6) {
            mPinSixthDigitEditText.setText("");
            mPinFifthDigitEditText.setText("");
            mPinForthDigitEditText.setText("");
            mPinThirdDigitEditText.setText("");
            mPinSecondDigitEditText.setText("");
            mPinFirstDigitEditText.setText("");
        } else if (mPinHiddenEditText.getText().length() == 5) {
            mPinFifthDigitEditText.setText("");
            mPinForthDigitEditText.setText("");
            mPinThirdDigitEditText.setText("");
            mPinSecondDigitEditText.setText("");
            mPinFirstDigitEditText.setText("");
        } else if (mPinHiddenEditText.getText().length() == 4) {
            mPinForthDigitEditText.setText("");
            mPinThirdDigitEditText.setText("");
            mPinSecondDigitEditText.setText("");
            mPinFirstDigitEditText.setText("");
        } else if (mPinHiddenEditText.getText().length() == 3) {
            mPinThirdDigitEditText.setText("");
            mPinSecondDigitEditText.setText("");
            mPinFirstDigitEditText.setText("");
        } else if (mPinHiddenEditText.getText().length() == 2) {
            mPinSecondDigitEditText.setText("");
            mPinFirstDigitEditText.setText("");
        } else if (mPinHiddenEditText.getText().length() == 1)
            mPinFirstDigitEditText.setText("");

        if (mPinHiddenEditText.length() > 0)
            // mPinHiddenEditText.setText(mPinHiddenEditText.getText().subSequence(0, mPinHiddenEditText.length() - 1));
            mPinHiddenEditText.setText("");
    }

}

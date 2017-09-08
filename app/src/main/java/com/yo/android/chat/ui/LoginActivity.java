package com.yo.android.chat.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.internal.LinkedTreeMap;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.orion.android.common.util.ConnectivityHelper;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.chat.ui.fragments.OTPFragment;
import com.yo.android.model.CountryCode;
import com.yo.android.model.Response;
import com.yo.android.ui.NewOTPActivity;
import com.yo.android.ui.PlainActivity;
import com.yo.android.ui.fragments.GeneralWebViewFragment;
import com.yo.android.util.Constants;
import com.yo.android.util.CountryCodeHelper;
import com.yo.android.util.Util;
import com.yo.android.vox.UserDetails;
import com.yo.android.vox.VoxFactory;
import com.yo.services.BackgroundServices;

import java.net.SocketTimeoutException;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends ParentActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "LoginActivity";
    private static final int READ_SMS_REQUEST_CODE_ASK_SMS_PERMISSIONSPERMISSIONS = 123;
    private static final String FRAGMENT_TAG = "OTPFragment";
    public static final String URL = BuildConfig.BASE_URL + "T&C/YO_PRIVACY_POLICY.html";


    @Bind(R.id.et_enter_phone)
    protected EditText mPhoneNumberView;


//    @Bind(R.id.spCountrySpinner)
//    protected NiceSpinner spCountrySpinner;
//

    @Bind(R.id.et_country_code)
    protected EditText mCountryCode;

    @Inject
    VoxFactory voxFactory;
    @Inject
    YoApi.YoService yoService;
    @Inject
    CountryCodeHelper mCountryCodeHelper;
    @Inject
    ConnectivityHelper mHelper;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final int SELECTED_OK = 101;
    private List<CountryCode> mList;
    private MenuItem searchMenuItem;
    private EventBus bus = EventBus.getDefault();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        bus.register(this);
        if (getIntent().getBooleanExtra(Constants.SESSION_EXPIRE, false)) {
            //Toast.makeText(this, "YoApp session expired.", Toast.LENGTH_LONG).show();
            Toast.makeText(this, getString(R.string.logged_in_another_device), Toast.LENGTH_LONG).show();
        }
        checkForPermissions();
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    mLog.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // mLog.d(TAG, "onAuthStateChanged:signed_out");
                    firebaseAuth.signOut();
                    if (mAuthListener != null) {
                        mAuth.removeAuthStateListener(mAuthListener);
                    }
                }
            }
        };
        mList = mCountryCodeHelper.readCodesFromAssets();
        String str = mCountryCodeHelper.getCountryZipCode(this);
        if (TextUtils.isEmpty(str)) {
            str = "sg";
        }
        int pos = 0;
        for (CountryCode countryCode : mList) {
            if (countryCode.getCountryID().equalsIgnoreCase(str)) {
                pos = mList.indexOf(countryCode);
                preferenceEndPoint.saveStringPreference(Constants.COUNTRY_CODE_FROM_SIM, countryCode.getCountryCode());
                preferenceEndPoint.saveStringPreference(Constants.COUNTRY_DISPLAY_NAME, countryCode.getCountryName());
                break;
            }
        }
        String simCountryCode = preferenceEndPoint.getStringPreference(Constants.COUNTRY_CODE_FROM_SIM);
        String simCountryName = preferenceEndPoint.getStringPreference(Constants.COUNTRY_DISPLAY_NAME);
        mCountryCode.setText("+" + simCountryCode + " " + simCountryName);
        //spCountrySpinner.attachDataSource(mList);

//         spCountrySpinner.setSelectedIndex(pos);

        //spCountrySpinner.setOnItemSelectedListener(this);

        mCountryCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.hideKeyboard(LoginActivity.this,view);
                Intent intent = new Intent(LoginActivity.this, CountryCodeActivity.class);
                startActivityForResult(intent, SELECTED_OK);

            }
        });

        mCountryCode.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Util.hideKeyboard(LoginActivity.this,v);
                return false;
            }
        });

        mPhoneNumberView.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    //do here
                    Util.hideKeyboard(LoginActivity.this, mPhoneNumberView);
                    attemptLogin();
                    return true;
                }

                return false;
            }
        });
    }

    private void checkForPermissions() {
        ActivityCompat.requestPermissions(LoginActivity.this, new String[]{"android.permission.RECEIVE_SMS"}, READ_SMS_REQUEST_CODE_ASK_SMS_PERMISSIONSPERMISSIONS);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    @OnClick(R.id.email_sign_in_button)
    public void attemptLogin() {
        // Reset errors.
        mPhoneNumberView.setError(null);
        // Store values at the time of the login attempt.
        // String countryCode=mCountryCode.getText().toString();
        String phoneNumber = mPhoneNumberView.getText().toString().trim();


        boolean cancel = false;
        View focusView = null;

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber swissNumberProto = null;


        String selectedCountryCode = preferenceEndPoint.getStringPreference(Constants.COUNTRY_CODE_FROM_SIM);
        String country = preferenceEndPoint.getStringPreference(Constants.COUNTRY_ID);


        try {
            swissNumberProto = phoneUtil.parse(selectedCountryCode + phoneNumber, country.toUpperCase());

            android.util.Log.e("Login", "Country code " + swissNumberProto.toString());
        } catch (NumberParseException e) {
            System.err.println("NumberParseException was thrown: " + e.toString());
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(phoneNumber)) {
            Util.hideKeyboard(this, getCurrentFocus());
            focusView = mPhoneNumberView;
            cancel = true;
            mToastFactory.showToast(getResources().getString(R.string.empty_fields));
        } else if (TextUtils.isEmpty(phoneNumber)) {
            Util.hideKeyboard(this, getCurrentFocus());
            focusView = mPhoneNumberView;
            mToastFactory.showToast(getResources().getString(R.string.enter_mobile_number));

        } else if (swissNumberProto != null && !(phoneUtil.isValidNumber(swissNumberProto))) {
            Util.hideKeyboard(this, getCurrentFocus());
            focusView = mPhoneNumberView;
            mToastFactory.showToast(getResources().getString(R.string.enter_mobile_number_error));
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
        } else {
            showMessageDialog(phoneNumber);

        }
    }

    @OnClick(R.id.tc_link)
    public void termAndConditions() {

        Bundle args = new Bundle();
        args.putString(GeneralWebViewFragment.KEY_URL, URL);
        PlainActivity.start(this, Constants.TERMS_CONDITIONS, args);

    }

    private void performLogin(String phoneNumber) {
        if (!mHelper.isConnected()) {
            mToastFactory.showToast(getResources().getString(R.string.connectivity_network_settings));
            return;
        }
        //Add subscriber
        String countryCode = preferenceEndPoint.getStringPreference(Constants.COUNTRY_CODE_FROM_SIM);
        String yoUser = phoneNumber;
        UserDetails userDetails = voxFactory.newAddSubscriber(yoUser, yoUser);
        //Debug
        String action = voxFactory.addSubscriber(yoUser, phoneNumber, countryCode);
        //  mLog.e(TAG, "Request for adding vox api: %s", action);
            /*voxService.getData(userDetails).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
*/
        callLoginService(phoneNumber);

    }

    public void callLoginService(final String phoneNumber) {
        showProgressDialog();
        String countryCode = preferenceEndPoint.getStringPreference(Constants.COUNTRY_CODE_FROM_SIM);
        String type = BuildConfig.ORIGINAL_SMS_VERIFICATION ? "original" : "dummy";
        //yoService.loginUserAPI(phoneNumber, type, countryCode).enqueue(new Callback<Response>() {
        yoService.loginUserAPI(countryCode + phoneNumber, type).enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                try {
                    dismissProgressDialog();
                    if (response.isSuccessful()) {
                        Response response1 = response.body();
                        if (response1 != null) {
                            boolean isNewUser = (boolean) ((LinkedTreeMap) response1.getData()).get("isNewUser");
                            String userId = (String) ((LinkedTreeMap) response1.getData()).get("id");
                            preferenceEndPoint.saveStringPreference(Constants.USER_ID, userId);
                            preferenceEndPoint.saveBooleanPreference("isNewUser", isNewUser);
                            boolean balanceAdded = (boolean) ((LinkedTreeMap) response1.getData()).get("balanceAdded");
                            preferenceEndPoint.saveBooleanPreference("balanceAdded", balanceAdded);
                        }
                        if (!BuildConfig.NEW_OTP_SCREEN) {
                            OTPFragment otpFragment = new OTPFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString(Constants.PHONE_NUMBER, phoneNumber);
                            otpFragment.setArguments(bundle);
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.add(android.R.id.content, otpFragment, FRAGMENT_TAG);
                            transaction.disallowAddToBackStack();
                            transaction.commit();
                        } else {
                            Intent intent = new Intent(LoginActivity.this, NewOTPActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString(Constants.PHONE_NUMBER, phoneNumber);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    } else {
                        mToastFactory.showToast("Please enter valid phone number.");
                    }
                } catch (Exception e) {

                }

            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                if (!mHelper.isConnected() || t instanceof SocketTimeoutException) {
                    mToastFactory.showToast(getResources().getString(R.string.connectivity_network_settings));
                } else {
                    mToastFactory.showToast(t.getLocalizedMessage());
                }
                dismissProgressDialog();
            }
        });

    }

    public void showMessageDialog(final String phoneNumber) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        final View view = layoutInflater.inflate(R.layout.custom_dialog, null);
        builder.setView(view);

        TextView textView = (TextView) view.findViewById(R.id.dialog_content);
        textView.setText(String.format(getResources().getString(R.string.Dialog_text), phoneNumber));


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
                performLogin(phoneNumber);
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
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        preferenceEndPoint.saveStringPreference(Constants.COUNTRY_CODE_FROM_SIM, mList.get(position).getCountryCode());
        preferenceEndPoint.saveStringPreference(Constants.COUNTRY_ID, mList.get(position).getCountryID());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECTED_OK && resultCode == RESULT_OK && data != null) {
            if (data.hasExtra("COUNTRY_CODE")) {
                String countryCode = data.getStringExtra("COUNTRY_CODE");
                String countryName = data.getStringExtra("COUNTRY_NAME");
                String countryId = data.getStringExtra("COUNTRY_ID");
                mCountryCode.setText("+" + countryCode + " " + countryName);
                preferenceEndPoint.saveStringPreference(Constants.COUNTRY_CODE_FROM_SIM, countryCode);
                preferenceEndPoint.saveStringPreference(Constants.COUNTRY_ID, countryId);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }

    public void onEventMainThread(Object action) {
        if (action.equals(Constants.FINISH_LOGIN_ACTIVITY_ACTION)) {
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == READ_SMS_REQUEST_CODE_ASK_SMS_PERMISSIONSPERMISSIONS) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //start audio recording or whatever you planned to do
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, Manifest.permission.RECEIVE_SMS)) {
                    //Show an explanation to the user *asynchronously*
                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
                    builder.setMessage("This permission is important to read sms.")
                            .setTitle("Important permission required");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.RECEIVE_SMS}, READ_SMS_REQUEST_CODE_ASK_SMS_PERMISSIONSPERMISSIONS);
                        }
                    });
                    ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.RECEIVE_SMS}, READ_SMS_REQUEST_CODE_ASK_SMS_PERMISSIONSPERMISSIONS);
                } else {
                    //Never ask again and handle your app without permission.
                }
            }

        }
    }
}


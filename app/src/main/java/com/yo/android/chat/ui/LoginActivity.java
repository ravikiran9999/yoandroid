package com.yo.android.chat.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.internal.LinkedTreeMap;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.chat.ui.fragments.OTPFragment;
import com.yo.android.model.CountryCode;
import com.yo.android.model.Response;
import com.yo.android.ui.BaseActivity;
import com.yo.android.util.Constants;
import com.yo.android.util.CountryCodeHelper;
import com.yo.android.vox.UserDetails;
import com.yo.android.vox.VoxApi;
import com.yo.android.vox.VoxFactory;

import org.angmarch.views.NiceSpinner;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BaseActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "LoginActivity";
    private static final int REQUEST_READ_CONTACTS = 0;
    private static final String FRAGMENT_TAG = "OTPFragment";

    @Bind(R.id.et_enter_phone)
    protected EditText mPhoneNumberView;
    @Bind(R.id.et_re_enter_phone)
    protected EditText mReEnterPhoneNumberView;
    @Bind(R.id.spCountrySpinner)
    protected NiceSpinner spCountrySpinner;


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    @Inject
    VoxApi.VoxService voxService;
    @Inject
    VoxFactory voxFactory;
    @Inject
    YoApi.YoService yoService;
    @Inject
    CountryCodeHelper mCountryCodeHelper;
    private List<CountryCode> mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    mLog.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    mLog.d(TAG, "onAuthStateChanged:signed_out");
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
                break;
            }
        }
        spCountrySpinner.attachDataSource(mList);
        spCountrySpinner.setSelectedIndex(pos);
        spCountrySpinner.setOnItemSelectedListener(this);
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
        String phoneNumber = mPhoneNumberView.getText().toString().trim();
        String reEnterPhone = mReEnterPhoneNumberView.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid email address.
        if (TextUtils.isEmpty(phoneNumber)) {
            focusView = mPhoneNumberView;
            cancel = true;
        } else if (TextUtils.isEmpty(reEnterPhone)) {
            focusView = mReEnterPhoneNumberView;
            cancel = true;
        } else if (!phoneNumber.equals(reEnterPhone)) {
            mToastFactory.showToast("Phone numbers should match");
            focusView = mPhoneNumberView;
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
        } else {
            //Add subscriber
            String countryCode = preferenceEndPoint.getStringPreference(Constants.COUNTRY_CODE_FROM_SIM);
            //TODO: Revathi will do that
            String yoUser = phoneNumber;
            UserDetails userDetails = voxFactory.newAddSubscriber(yoUser, yoUser);
            //Debug
            String action = voxFactory.addSubscriber(yoUser, phoneNumber, countryCode);
            mLog.e(TAG, "Request for adding vox api: %s", action);
            voxService.getData(userDetails).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });

            callLoginService(phoneNumber);

        }
    }

    public void callLoginService(final String phoneNumber) {
        showProgressDialog();
        String countryCode = preferenceEndPoint.getStringPreference(Constants.COUNTRY_CODE_FROM_SIM);
        String type = BuildConfig.ORIGINAL_SMS_VERIFICATION ? "original" : "dummy";
        //yoService.loginUserAPI(phoneNumber, type, countryCode).enqueue(new Callback<Response>() {
        yoService.loginUserAPI(countryCode + phoneNumber, type).enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                dismissProgressDialog();
                if (response.isSuccessful()) {
                    Response response1 = response.body();
                    if (response1 != null) {
                        boolean isNewUser = (boolean) ((LinkedTreeMap) response1.getData()).get("isNewUser");
                        preferenceEndPoint.saveBooleanPreference("isNewUser", isNewUser);
                    }
                    OTPFragment otpFragment = new OTPFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.PHONE_NUMBER, phoneNumber);
                    otpFragment.setArguments(bundle);
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.add(android.R.id.content, otpFragment, FRAGMENT_TAG);
                    transaction.disallowAddToBackStack();
                    transaction.commit();
                } else {
                    mToastFactory.showToast("Please check valid phone number.");
                }

            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                dismissProgressDialog();
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
        preferenceEndPoint.saveStringPreference(Constants.COUNTRY_CODE_FROM_SIM, mList.get(position).getCountryCode()/*(CountryCode) spCountrySpinner.getSelectedItem()).getCountryCode()*/);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}


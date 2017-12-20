package com.yo.android.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.orion.android.common.util.ConnectivityHelper;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.model.UserProfileInfo;
import com.yo.android.ui.AccountDetailsActivity;
import com.yo.android.util.Constants;


import org.pjsip.pjsua2.Account;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Sindhura on 11/22/2016.
 */
public class AccountDetailsFragment extends BaseFragment {

    @Bind(R.id.account_status_card)
    protected TextView accountStatus;

    @Bind(R.id.account_name_card)
    protected TextView accountName;

    @Bind(R.id.account_phone_number_card)
    protected TextView accountPhoneNumber;

    @Bind(R.id.account_dob_card)
    protected TextView accountDOB;

    @Bind(R.id.account_email_card)
    protected TextView accountEmail;

    @Bind(R.id.toggle)
    protected RadioGroup genderToggle;

    @Bind(R.id.male)
    protected RadioButton maleRadio;

    @Bind(R.id.female)
    protected RadioButton femaleRadio;

    @Inject
    YoApi.YoService yoService;

    @Inject
    ConnectivityHelper mHelper;

    private String genderText = "";

    private String withoutCountryCode;

    public static final String dobHint = "dd-mm-yyyy";

    public static final String emailHint = "Enter Email Id";


    public int tokenExpireCount = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.account_details_layout, container, false);
        return view;


    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        setHasOptionsMenu(true);

    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        accountStatus.setText(preferenceEndPoint.getStringPreference(Constants.DESCRIPTION, "Available"));
        accountName.setText(preferenceEndPoint.getStringPreference(Constants.FIRST_NAME, ""));
        String email = preferenceEndPoint.getStringPreference(Constants.EMAIL, emailHint);
        if (email.equalsIgnoreCase("")) {
            email = emailHint;
        }
        setTextColor(email, accountEmail, emailHint);
        accountEmail.setText(email);
        genderText = preferenceEndPoint.getStringPreference(Constants.GENDER, "");
        if (genderText.equalsIgnoreCase(getString(R.string.male))) {
            genderToggle.check(R.id.male);
        } else if (genderText.equalsIgnoreCase(getString(R.string.female))) {
            genderToggle.check(R.id.female);
        }

        genderToggle.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                if (id == R.id.male) {
                    genderText = getString(R.string.male);
                } else if (id == R.id.female) {
                    genderText = getString(R.string.female);
                }
            }
        });
        if (isValidDate(preferenceEndPoint.getStringPreference(Constants.DOB_TEMP, ""))) {
            saveDOBProperly();
        }
        accountPhoneNumber.setText(preferenceEndPoint.getStringPreference(Constants.PHONE_NO));
        String dob = preferenceEndPoint.getStringPreference(Constants.DOB_TEMP, dobHint);
        if (dob.equalsIgnoreCase("")) {
            dob = dobHint;
        }
        setTextColor(dob, accountDOB, dobHint);
        accountDOB.setText(dob);
    }

    @OnClick(R.id.account_status_card)
    protected void accountStatusClick() {
        String title = String.format(getString(R.string.add_new), getString(R.string.status_title));
        callEditFragment(title, preferenceEndPoint.getStringPreference(Constants.DESCRIPTION, ""), Constants.DESCRIPTION);
    }

    @OnClick(R.id.account_name_card)
    protected void accountNameClick() {
        String title = String.format(getString(R.string.edit_details), getString(R.string.name));
        callEditFragment(title, preferenceEndPoint.getStringPreference(Constants.FIRST_NAME, ""), Constants.FIRST_NAME);
    }

    @OnClick(R.id.account_dob_card)
    protected void accountDOBClick() {
        String title = String.format(getString(R.string.edit_details), getString(R.string.dob));
        callEditFragment(title, preferenceEndPoint.getStringPreference(Constants.DOB_TEMP, "dd-mm-yyyy"), Constants.DOB_TEMP);
    }

    @OnClick(R.id.account_email_card)
    protected void accountEmailClick() {
        String title = String.format(getString(R.string.edit_details), getString(R.string.email_id));
        callEditFragment(title, preferenceEndPoint.getStringPreference(Constants.EMAIL, ""), Constants.EMAIL);
    }

    private void callEditFragment(final String title, final String edit, final String key) {
        AccountDetailsEditFragment accountDetailsFragment = new AccountDetailsEditFragment();
        Bundle bundle = new Bundle();
        bundle.putString(AccountDetailsEditFragment.TITLE, title);
        bundle.putString(AccountDetailsEditFragment.EDIT, edit);
        bundle.putString(AccountDetailsEditFragment.KEY, key);
        bundle.putString(AccountDetailsEditFragment.COUNTRY_COODE, getCountryCode(preferenceEndPoint.getStringPreference(Constants.PHONE_NO)));
        accountDetailsFragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, accountDetailsFragment, "FRAGMENT")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.getItem(0);
        item.setVisible(true);
    }

    public void saveDetails() {


        if (!mHelper.isConnected()) {
            mToastFactory.showToast(getResources().getString(R.string.connectivity_network_settings));
            return;
        }

        showProgressDialog();
        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        String access = "Bearer " + preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
        RequestBody description =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), accountStatus.getText().toString());
        RequestBody firstName =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), accountName.getText().toString());
        RequestBody phoneNo =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), preferenceEndPoint.getStringPreference(Constants.PHONE_NO_TEMP));
        RequestBody email =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), accountEmail.getText().toString());
        if (accountEmail.getText().toString().equalsIgnoreCase(emailHint)) {
            email = null;
        }
        RequestBody dob =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), accountDOB.getText().toString());
        if (accountDOB.getText().toString().equalsIgnoreCase(dobHint)) {
            dob = null;
        }
        RequestBody gender =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), genderText);
        yoService.updateProfile(userId, access, description, firstName, phoneNo, email, dob, gender, null, null, null).enqueue(new Callback<UserProfileInfo>() {
            @Override
            public void onResponse(Call<UserProfileInfo> call, Response<UserProfileInfo> response) {
                dismissProgressDialog();
                try {
                    if (response.body() != null) {
                        saveUserProfileValues(response.body());
                        getActivity().finish();
                    } else {
                        mToastFactory.showToast(getString(R.string.failed_update));
                    }
                } finally {

                }
            }

            @Override
            public void onFailure(Call<UserProfileInfo> call, Throwable t) {
                dismissProgressDialog();
                mToastFactory.showToast(getString(R.string.failed_update));
            }
        });
    }

    public void setUserInfoDetails() {
        accountStatus.setText(preferenceEndPoint.getStringPreference(Constants.DESCRIPTION, ""));
        accountName.setText(preferenceEndPoint.getStringPreference(Constants.FIRST_NAME, ""));
        accountPhoneNumber.setText(preferenceEndPoint.getStringPreference(Constants.PHONE_NO_TEMP, ""));
        String dob = preferenceEndPoint.getStringPreference(Constants.DOB_TEMP, dobHint);
//        String accUserName = accountName.getText().toString();
//        if (TextUtils.isEmpty(accUserName)) {
//            Toast.makeText(getActivity(), getResources().getString(R.string.acc_name), Toast.LENGTH_LONG).show();
//        }
        if (dob.equalsIgnoreCase("")) {
            dob = dobHint;
        }
        setTextColor(dob, accountDOB, dobHint);
        accountDOB.setText(dob);
        String email = preferenceEndPoint.getStringPreference(Constants.EMAIL, emailHint);
        if (email.equalsIgnoreCase("")) {
            email = emailHint;
        }
        setTextColor(email, accountEmail, emailHint);
        accountEmail.setText(email);

    }

    private void saveUserProfileValues(final UserProfileInfo response) {
        String avatar = response.getAvatar();
        String email = response.getEmail();
        String description = response.getDescription();
        String dob = response.getDob();
        String gender = response.getGender();
        String firstName = response.getFirstName();
        String phoneNo = response.getPhoneNumber();
        preferenceEndPoint.saveStringPreference(Constants.USER_AVATAR, avatar);
        preferenceEndPoint.saveStringPreference(Constants.EMAIL, email);
        preferenceEndPoint.saveStringPreference(Constants.DESCRIPTION, description);
        preferenceEndPoint.saveStringPreference(Constants.DOB_TEMP, dob);
        preferenceEndPoint.saveStringPreference(Constants.GENDER, gender);
        preferenceEndPoint.saveStringPreference(Constants.FIRST_NAME, firstName);
        preferenceEndPoint.saveStringPreference(Constants.PHONE_NO, phoneNo);
        preferenceEndPoint.saveStringPreference(Constants.AVATAR_TEMP, avatar);
        preferenceEndPoint.saveStringPreference(Constants.EMAIL_TEMP, email);
        preferenceEndPoint.saveStringPreference(Constants.DESCRIPTION_TEMP, description);
        // preferenceEndPoint.saveStringPreference(Constants.DOB_TEMP, dob);
        preferenceEndPoint.saveStringPreference(Constants.GENDER_TEMP, gender);
        preferenceEndPoint.saveStringPreference(Constants.FIRST_NAME_TEMP, firstName);
        preferenceEndPoint.saveStringPreference(Constants.PHONE_NO_TEMP, phoneNo);
    }

    private void saveDOBProperly() {
        String dob = preferenceEndPoint.getStringPreference(Constants.DOB_TEMP, "");
        if (dob != null && dob.length() > 0) {
            DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");
            String inputDateStr = dob;
            Date date = null;
            try {
                date = inputFormat.parse(inputDateStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String outputDateStr = outputFormat.format(date);
            //preferenceEndPoint.saveStringPreference(Constants.DOB, outputDateStr);
            preferenceEndPoint.saveStringPreference(Constants.DOB_TEMP, outputDateStr);
        }
    }

    public static boolean isValidDate(String inDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(inDate.trim());
        } catch (ParseException pe) {
            return false;
        }
        return true;
    }

    private String getCountryCode(String phone) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        int countryCode = 0;
        try {
            // phone must begin with '+'
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(phone, "");
            countryCode = numberProto.getCountryCode();
        } catch (NumberParseException e) {
            System.err.println("NumberParseException was thrown: " + e.toString());
        }
        if (countryCode != 0) {
            return "+" + countryCode;
        }
        return String.valueOf(countryCode);
    }

    private void setPhoneNumber(final String phone) {
        String countryCode = getCountryCode(phone);
        if (!countryCode.equalsIgnoreCase("0")) {
            String number = phone.substring(countryCode.length());
            withoutCountryCode = number;
            accountPhoneNumber.setText(Html.fromHtml(String.format(getString(R.string.phone_text_field), countryCode, number)));
        } else {
            accountPhoneNumber.setText(phone);
        }
    }

    private void setTextColor(final String text, final TextView textView, final String hint) {
        if (text.equalsIgnoreCase(hint)) {
            textView.setTextColor(getResources().getColor(R.color.gray));
        } else {
            textView.setTextColor(getResources().getColor(R.color.black));
        }
    }

}

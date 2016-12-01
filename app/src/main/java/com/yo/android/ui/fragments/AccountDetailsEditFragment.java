package com.yo.android.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yo.android.R;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.ui.AccountDetailsActivity;
import com.yo.android.util.Constants;

/**
 * Created by Sindhura on 11/23/2016.
 */
public class AccountDetailsEditFragment extends BaseFragment implements View.OnClickListener {

    private String title;

    private String edit;

    private String key;

    private EditText editProfile;

    private EditText editBirth;

    private TextView cancelBtn;

    private TextView okBtn;

    private String countryCode;

    public AccountDetailsEditFragment(final String title, final String edit, final String key, final String countryCode) {
        this.title = title;
        this.edit = edit;
        this.key = key;
        this.countryCode = countryCode;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.account_details_edit_layout, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        ((AccountDetailsActivity) getActivity()).getSupportActionBar().setTitle(title);
        ((AccountDetailsActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AccountDetailsActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        editProfile = (EditText) view.findViewById(R.id.edit_profile);
        editBirth = (EditText) view.findViewById(R.id.birth);
        cancelBtn = (TextView) view.findViewById(R.id.cancel_edit);
        okBtn = (TextView) view.findViewById(R.id.ok_edit);
        cancelBtn.setOnClickListener(this);
        okBtn.setOnClickListener(this);
        editProfile.setText(edit);
        if (key.equalsIgnoreCase(Constants.DOB_TEMP)) {
            editBirth.setVisibility(View.VISIBLE);
            editBirth.setText(edit);
            editBirth.setOnClickListener(this);
            editProfile.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.getItem(0);
        item.setVisible(false);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancel_edit:
                dismissKeyboard();
                getActivity().onBackPressed();
                break;
            case R.id.ok_edit:
                dismissKeyboard();
                saveEditedValue();
                break;
            case R.id.birth:
                DialogFragment newFragment = new SelectDateFragment(editBirth, true);
                newFragment.show(getActivity().getSupportFragmentManager(), "DatePicker");
                break;
        }
    }

    /**
     * Save Edited Values in shared preferences
     */
    private void saveEditedValue() {
        if (key.equalsIgnoreCase(Constants.DOB_TEMP)) {
            preferenceEndPoint.saveStringPreference(key, editBirth.getText().toString());
            getActivity().onBackPressed();
        } else {
            String text = editProfile.getText().toString();
            if (key.equalsIgnoreCase(Constants.PHONE_NO_TEMP) && !isValidMobile(text)) {
                mToastFactory.showToast(getString(R.string.valid_phone));
            } else if (key.equalsIgnoreCase(Constants.EMAIL_TEMP) && !isValidMail(text)) {
                mToastFactory.showToast(getString(R.string.valid_email));
            } else {
                if (!TextUtils.isEmpty(text)) {
                    if (key.equalsIgnoreCase(Constants.PHONE_NO_TEMP)) {
                        text = countryCode + text;
                    }
                    preferenceEndPoint.saveStringPreference(key, text.trim());
                    getActivity().onBackPressed();
                } else {
                    mToastFactory.showToast(getString(R.string.empty_field));
                }
            }
        }
    }

    /**
     * Dismiss Keyboard
     */
    private void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(editProfile.getWindowToken(), 0);
        }
    }

    /**
     * Validate phone number
     * @param phone the Phone Number
     * @return
     */
    private boolean isValidMobile(String phone) {
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }

    /**
     * Validate Email ID
     * @param email the email address
     * @return
     */
    private boolean isValidMail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}

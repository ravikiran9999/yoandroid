package com.yo.android.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

import com.yo.android.R;
import com.yo.android.ui.fragments.AccountDetailsEditFragment;
import com.yo.android.ui.fragments.AccountDetailsFragment;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

/**
 * Created by Sindhura on 11/22/2016.
 */
public class AccountDetailsActivity extends BaseActivity {

    public static final String FRAGMENT = "fragment";
    private AccountDetailsFragment accountDetailsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getString(R.string.account_details));
        accountDetailsFragment = new AccountDetailsFragment();
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, accountDetailsFragment, FRAGMENT)
                .commit();
        enableBack();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_more_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(android.R.id.content);
            if (currentFragment instanceof AccountDetailsEditFragment) {
                AccountDetailsEditFragment fragment = (AccountDetailsEditFragment) currentFragment;
                boolean flag = Util.hideKeyboard(this, fragment.getEditProfile());
                if (!flag) {
                    onBackPressed();
                }
            }else{
                onBackPressed();
            }

        } else if (item.getItemId() == R.id.menu_save_settings) {
            showConfirmationDialog();
        }
        return true;
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage(R.string.save_changes)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ((AccountDetailsFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT)).saveDetails();
                    }
                })
                .setNegativeButton(R.string.cancel, null);
        // Create the AlertDialog object and return it
        builder.create().show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getSupportFragmentManager().findFragmentByTag(FRAGMENT) instanceof AccountDetailsFragment) {
            getSupportActionBar().setTitle(getString(R.string.account_details));
            ((AccountDetailsFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT)).setUserInfoDetails();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        preferenceEndPoint.saveStringPreference(Constants.DESCRIPTION_TEMP, preferenceEndPoint.getStringPreference(Constants.DESCRIPTION));
        preferenceEndPoint.saveStringPreference(Constants.FIRST_NAME_TEMP, preferenceEndPoint.getStringPreference(Constants.FIRST_NAME));
        preferenceEndPoint.saveStringPreference(Constants.PHONE_NO_TEMP, preferenceEndPoint.getStringPreference(Constants.PHONE_NO));
        preferenceEndPoint.saveStringPreference(Constants.DOB_TEMP, preferenceEndPoint.getStringPreference(Constants.DOB));
        preferenceEndPoint.saveStringPreference(Constants.EMAIL_TEMP, preferenceEndPoint.getStringPreference(Constants.EMAIL));
    }
}

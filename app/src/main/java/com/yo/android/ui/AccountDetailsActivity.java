package com.yo.android.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.yo.android.R;
import com.yo.android.ui.fragments.AccountDetailsFragment;
import com.yo.android.util.Constants;

/**
 * Created by Sindhura on 11/22/2016.
 */
public class AccountDetailsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Account Details");
        AccountDetailsFragment accountDetailsFragment = new AccountDetailsFragment();
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, accountDetailsFragment, "FRAGMENT")
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
            onBackPressed();
        } else if (item.getItemId() == R.id.menu_save_settings) {
            showConfirmationDialog();
        }
        return true;
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage("Are you sure you want to save the changes?")
                .setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ((AccountDetailsFragment) getSupportFragmentManager().findFragmentByTag("FRAGMENT")).saveDetails();
                    }
                })
                .setNegativeButton("CANCEL", null);
        // Create the AlertDialog object and return it
        builder.create().show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getSupportFragmentManager().findFragmentByTag("FRAGMENT") instanceof AccountDetailsFragment) {
            getSupportActionBar().setTitle("Account Details");
            ((AccountDetailsFragment) getSupportFragmentManager().findFragmentByTag("FRAGMENT")).setUserInfoDetails();
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

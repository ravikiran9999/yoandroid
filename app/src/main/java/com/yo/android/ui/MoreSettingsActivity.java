package com.yo.android.ui;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.firebase.client.utilities.Utilities;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.orion.android.common.util.ToastFactory;
import com.orion.android.common.util.ToastFactoryImpl;
import com.yo.android.R;
import com.yo.android.model.UserProfileInfo;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by kalyani on 25/7/16.
 */
public class MoreSettingsActivity extends BaseActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Bind(R.id.user_name_edt)
    EditText userNameText;
    @Bind(R.id.mobile_number_txt)
    TextView mobileNumberText;
    @Bind(R.id.status_edt)
    EditText statusEdt;
    @Bind(R.id.share_lint_btn)
    TextView shareLinkBtn;
    @Bind(R.id.sync_toggle)
    Switch syncContactsSwitch;
    @Bind(R.id.notification_toggle)
    Switch notificationSwitch;
    @Bind(R.id.account_privacy_toggle)
    Switch accountPrivacySwitch;
    @Inject
    @Named("login")
    PreferenceEndPoint preferenceEndPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);
        ButterKnife.bind(this);
        enableBack();
        preferenceEndPoint.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        setDataFromPreferences();
    }

    private void setDataFromPreferences() {
        String phone = preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER);
        String userName = preferenceEndPoint.getStringPreference(Constants.USER_NAME);
        String mStatus = preferenceEndPoint.getStringPreference(Constants.USER_STATUS);
        userNameText.setText(userName);
        mobileNumberText.setText(phone);
        statusEdt.setText(mStatus + "");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        preferenceEndPoint.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (Constants.USER_NAME.equals(key)) {
            userNameText.setText(preferenceEndPoint.getStringPreference(Constants.USER_NAME));
        } else if (Constants.PHONE_NUMBER.equals(key)) {
            mobileNumberText.setText(preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER));
        } else if (Constants.USER_STATUS.equals(key)) {
            statusEdt.setText(preferenceEndPoint.getStringPreference(Constants.USER_STATUS));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_more_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @OnClick(R.id.share_lint_btn)
    public void shareLinkAction() {
        String url = "Checkout YoApp for your smart phone. Download it today from https://www.yoapp.com";
        Util.shareArticle(shareLinkBtn, url);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        final MenuItem menuItem = item;
        if (menuItem.getItemId() == R.id.menu_save_settings) {
            //do nothing..
            dismissProgressDialog();
            if (isValid()) {
                updateSettings();
            } else {
                mToastFactory.showToast(R.string.username_empty);
            }
        }
        return true;
    }

    private boolean isValid() {
        String mName = userNameText.getText().toString().trim();
        if (TextUtils.isEmpty(mName)) {
            return false;
        }
        return true;
    }

    private void updateSettings() {

        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);

        String descriptionString = statusEdt.getText().toString();
        RequestBody description =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), descriptionString);

        String userName = userNameText.getText().toString();
        RequestBody firstName =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), userName);

        yoService.updateProfile(userId, description, firstName, null).enqueue(new Callback<UserProfileInfo>() {
            @Override
            public void onResponse(Call<UserProfileInfo> call, Response<UserProfileInfo> response) {
                dismissProgressDialog();
                if (response.body() != null) {
                    preferenceEndPoint.saveStringPreference(Constants.USER_NAME, response.body().getFirstName());
                    preferenceEndPoint.saveStringPreference(Constants.USER_STATUS, response.body().getDescription());
                    preferenceEndPoint.saveStringPreference(Constants.USER_AVATAR, response.body().getAvatar());
                }
            }

            @Override
            public void onFailure(Call<UserProfileInfo> call, Throwable t) {
                dismissProgressDialog();
            }
        });
    }
}

package com.yo.android.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.orion.android.common.util.ConnectivityHelper;
import com.yo.android.R;
import com.yo.android.ui.fragments.GeneralWebViewFragment;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.yo.android.chat.ui.LoginActivity.URL;

public class MoreSettingsActivity extends BaseActivity {

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

    @Inject
    ConnectivityHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);
        ButterKnife.bind(this);
        enableBack();
        setDataFromPreferences();
    }

    private void setDataFromPreferences() {
        syncContactsSwitch.setChecked(preferenceEndPoint.getBooleanPreference(Constants.SYNCE_CONTACTS));
        notificationSwitch.setChecked(preferenceEndPoint.getBooleanPreference(Constants.NOTIFICATION_ALERTS));
        syncContactsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                preferenceEndPoint.saveBooleanPreference(Constants.SYNCE_CONTACTS, isChecked);
            }
        });
        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                preferenceEndPoint.saveBooleanPreference(Constants.NOTIFICATION_ALERTS, isChecked);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @OnClick(R.id.share_lint_btn)
    public void shareLinkAction() {
        String url = getString(R.string.invite_link);
        Util.shareIntent(shareLinkBtn, url, "Sharing Link");
    }

    @OnClick(R.id.tc_link)
    public void termAndConditions() {

        Bundle args = new Bundle();
        args.putString(GeneralWebViewFragment.KEY_URL, URL);
        PlainActivity.start(this, Constants.TERMS_CONDITIONS, args);

    }
}

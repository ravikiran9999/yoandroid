package com.yo.android.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.yo.android.R;
import com.yo.android.adapters.AppContactsListAdapter;
import com.yo.android.adapters.ProfileMembersAdapter;
import com.yo.android.util.Constants;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by kalyani on 25/7/16.
 */
public class UserProfileActivity extends BaseActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Bind(R.id.profile_image)
    ImageView profileImage;
    @Bind(R.id.profile_call)
    ImageView profileCall;
    @Bind(R.id.profile_message)
    ImageView profileMsg;
    @Bind(R.id.profile_name)
    TextView profileName;
    @Bind(R.id.profile_number)
    TextView profileNumber;


    private ProfileMembersAdapter profileMembersAdapter;
    private ListView membersList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        ButterKnife.bind(this);
        enableBack();
        preferenceEndPoint.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        String phone = getIntent().getStringExtra(Constants.OPPONENT_PHONE_NUMBER);
        setDataFromPreferences(phone);
        membersList = (ListView)findViewById(R.id.members);
        profileMembersAdapter = new ProfileMembersAdapter(getApplicationContext());
        membersList.setAdapter(profileMembersAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        preferenceEndPoint.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

    }

    private void setDataFromPreferences(String phone) {
        //String phone = preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER);
        //String userName = preferenceEndPoint.getStringPreference(Constants.USER_NAME);
        String avatar = preferenceEndPoint.getStringPreference(Constants.USER_AVATAR);
        if (!TextUtils.isEmpty(avatar)) {
            Picasso.with(this)
                    .load(avatar)
                    .into(profileImage);
        }
        //profileName.setText(userName);
        profileNumber.setText(phone);
        getSupportActionBar().setTitle(phone);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (Constants.USER_NAME.equals(key)) {
            String mUserName = preferenceEndPoint.getStringPreference(Constants.USER_NAME);
            profileName.setText(mUserName);
            getSupportActionBar().setTitle(mUserName);
        } else if (Constants.PHONE_NUMBER.equals(key)) {
            profileName.setText(preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER));
        } else if (Constants.USER_AVATAR.equals(key)) {
            String image = preferenceEndPoint.getStringPreference(Constants.USER_AVATAR);
            if (!TextUtils.isEmpty(image)) {
                Picasso.with(this)
                        .load(image)
                        .into(profileImage);
            }
        }
    }

    @OnClick(R.id.profile_call)
    public void callUser() {
        //do nothing...
    }

    @OnClick(R.id.profile_message)
    public void messageUser() {
        finish();
    }


}

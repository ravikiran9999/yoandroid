package com.yo.android.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.yo.android.R;
import com.yo.android.adapters.ProfileMembersAdapter;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.model.Contact;
import com.yo.android.util.Constants;
import com.yo.android.voip.OutGoingCallActivity;

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
    private Contact contact;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        ButterKnife.bind(this);
        enableBack();
        preferenceEndPoint.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(Constants.CONTACT)) {
                contact = getIntent().getParcelableExtra(Constants.CONTACT);
                setDataFromPreferences();
            }
        }
        membersList = (ListView) findViewById(R.id.members);
        profileMembersAdapter = new ProfileMembersAdapter(getApplicationContext());
        membersList.setAdapter(profileMembersAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        preferenceEndPoint.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

    }

    private void setDataFromPreferences() {
        //String phone = preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER);
        //String userName = preferenceEndPoint.getStringPreference(Constants.USER_NAME);
        //String avatar = preferenceEndPoint.getStringPreference(Constants.USER_AVATAR);
        if(contact!=null) {
            if (!TextUtils.isEmpty(contact.getImage())) {
                Picasso.with(this)
                        .load(contact.getImage())
                        .placeholder(R.drawable.img_placeholder_profile)
                        .into(profileImage);
            }
            //profileName.setText(userName);
            profileName.setText(contact.getName());
            profileNumber.setText(contact.getPhoneNo());
            getSupportActionBar().setTitle(contact.getPhoneNo());
            if (contact.getYoAppUser()) {
                profileMsg.setImageResource(R.mipmap.ic_profile_chat);
            } else {
                profileMsg.setImageResource(R.drawable.ic_invitefriends);
            }
        }
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
        if (contact != null && contact.getPhoneNo() != null) {
            Intent intent = new Intent(this, OutGoingCallActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(OutGoingCallActivity.CALLER_NO, contact.getPhoneNo());
            startActivity(intent);
        }

    }

    @OnClick(R.id.profile_message)
    public void messageUser() {
        if (contact != null && contact.getYoAppUser()) {
            navigateToChatScreen();
        }else {
            Toast.makeText(this, "Invite friends need to implement.", Toast.LENGTH_SHORT).show();
        }

    }

    private void navigateToChatScreen() {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.CONTACT, contact);
        intent.putExtra(Constants.TYPE, Constants.CONTACT);
        startActivity(intent);

    }


}

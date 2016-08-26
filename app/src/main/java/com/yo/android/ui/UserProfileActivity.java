package com.yo.android.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//import com.squareup.picasso.Picasso;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yo.android.R;
import com.yo.android.adapters.ProfileMembersAdapter;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.model.Contact;
import com.yo.android.pjsip.SipHelper;
import com.yo.android.util.Constants;

import javax.inject.Inject;

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
    private String opponentNo;
    private String opponentImg;
    private boolean fromChatRooms;

    @Inject
    ContactsSyncManager mContactsSyncManager;


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
            } else if (intent.hasExtra(Constants.FROM_CHAT_ROOMS)) {
                fromChatRooms = true;
                if (intent.hasExtra(Constants.OPPONENT_PHONE_NUMBER)) {
                    opponentNo = intent.getStringExtra(Constants.OPPONENT_PHONE_NUMBER);
                }
                if (intent.hasExtra(Constants.OPPONENT_CONTACT_IMAGE)) {
                    opponentImg = intent.getStringExtra(Constants.OPPONENT_CONTACT_IMAGE);
                }
                contact = new Contact();
                contact.setPhoneNo(opponentNo);
                contact.setVoxUserName(opponentNo);
                contact.setImage(opponentImg);
                contact.setYoAppUser(true);
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
        if (contact != null) {
            if (fromChatRooms) {
                getSupportActionBar().setTitle(getResources().getString(R.string.profile));
            } else {
                getSupportActionBar().setTitle(contact.getName());
            }
            if (!TextUtils.isEmpty(contact.getImage())) {

                Glide.with(this)
                        .load(contact.getImage())
                        .placeholder(R.drawable.img_placeholder_profile)
                        .fitCenter()
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(profileImage);
            }
            profileName.setText(contact.getName());
            Contact mContact = mContactsSyncManager.getContactByVoxUserName(contact.getVoxUserName());
            if(mContact!=null){
                 if(mContact.getName()!=null){
                     profileNumber.setText(mContact.getPhoneNo());

                 }else if(mContact.getPhoneNo() !=null){
                     profileNumber.setText(mContact.getPhoneNo());

                 }
            }else {
                profileNumber.setText(contact.getPhoneNo());
            }
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

                Glide.with(this)
                        .load(image)
                        .fitCenter()
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(profileImage);
            }
        }
    }

    @OnClick(R.id.profile_call)
    public void callUser() {
        //do nothing...
        if (contact != null && contact.getVoxUserName() != null) {
            SipHelper.makeCall(this, contact.getVoxUserName());
        }
    }

    @OnClick(R.id.profile_message)
    public void messageUser() {
        if (contact != null && contact.getYoAppUser()) {
            if (fromChatRooms) {
                finish();
            } else {
                navigateToChatScreen(contact);
            }
        } else {
            Toast.makeText(this, "Invite friends need to implement.", Toast.LENGTH_SHORT).show();
        }

    }

    private void navigateToChatScreen(Contact contact) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.CONTACT, contact);
        intent.putExtra(Constants.TYPE, Constants.CONTACT);
        startActivity(intent);

    }


}

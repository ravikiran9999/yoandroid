package com.yo.android.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.yo.android.R;
import com.yo.android.adapters.ProfileMembersAdapter;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.model.Contact;
import com.yo.android.model.GroupMembers;
import com.yo.android.model.RoomInfo;
import com.yo.android.model.UserProfile;
import com.yo.android.pjsip.SipHelper;
import com.yo.android.util.Constants;
import com.yo.android.util.FireBaseHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by kalyani on 25/7/16.
 */
public class UserProfileActivity extends BaseActivity implements SharedPreferences.OnSharedPreferenceChangeListener, ValueEventListener {

    private static final String TAG = UserProfileActivity.class.getSimpleName();
    @Bind(R.id.profile_image)
    CircleImageView profileImage;
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
    private String opponentName;
    private String opponentImg;
    private boolean fromChatRooms;
    private Firebase authReference;
    private List<GroupMembers> groupMembersList;
    private String roomName;

    HashMap<String, GroupMembers> groupMembersHashMap;
    @Inject
    ContactsSyncManager mContactsSyncManager;

    @Inject
    FireBaseHelper fireBaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.profile_background));
        ButterKnife.bind(this);
        enableBack();
        groupMembersList = new ArrayList<>();
        groupMembersHashMap = new HashMap<>();
        mLog.e(TAG, "Firebase token reading from pref " + preferenceEndPoint.getStringPreference(Constants.FIREBASE_TOKEN));

        authReference = fireBaseHelper.authWithCustomToken(this, preferenceEndPoint.getStringPreference(Constants.FIREBASE_TOKEN));
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
                if (intent.hasExtra(Constants.OPPONENT_NAME)) {
                    opponentName = intent.getStringExtra(Constants.OPPONENT_NAME);
                }

                roomName = intent.getStringExtra(Constants.GROUP_NAME);
                contact = new Contact();
                contact.setPhoneNo(opponentNo);
                contact.setName(opponentName);
                contact.setVoxUserName(opponentNo);
                contact.setImage(opponentImg);
                contact.setYoAppUser(true);
                setDataFromPreferences();

                if (intent.hasExtra(Constants.CHAT_ROOM_ID)) {
                    String firebaseRoomId = intent.getStringExtra(Constants.CHAT_ROOM_ID);
                    Firebase roomInfo = authReference.child(Constants.ROOMS).child(firebaseRoomId).child(Constants.ROOM_INFO);
                    if (roomName != null) {
                        roomInfo.addListenerForSingleValueEvent(this);
                        roomInfo.keepSynced(true);
                        profileCall.setVisibility(View.GONE);
                    }
                }
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
        if (contact != null) {
            if (fromChatRooms) {
                getSupportActionBar().setTitle(getResources().getString(R.string.profile));
            } else {
                getSupportActionBar().setTitle(contact.getName());
            }

            if (roomName != null) {
                Glide.with(this)
                        .load(contact.getImage())
                        .placeholder(R.drawable.chat_group)
                        .dontAnimate()
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(profileImage);
            } else {
                Glide.with(this)
                        .load(contact.getImage())
                        .placeholder(R.drawable.dynamic_profile)
                        .crossFade()
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(profileImage);
            }

            Contact mContact = mContactsSyncManager.getContactByVoxUserName(contact.getVoxUserName());

            if (mContact != null) {

                if (mContact.getName() != null && !mContact.getName().replaceAll("\\s+", "").equalsIgnoreCase(mContact.getPhoneNo())) {
                    profileName.setText(mContact.getName());
                } else {
                    profileName.setVisibility(View.GONE);
                }
                if (mContact.getPhoneNo() != null) {
                    String numberWithCountryCode = "+" + mContact.getCountryCode().concat(mContact.getPhoneNo());
                    profileNumber.setText(numberWithCountryCode);
                } else {
                    profileNumber.setVisibility(View.GONE);
                }
            } else if (contact != null) {

                if (contact.getName() != null && !contact.getName().replaceAll("\\s+", "").equalsIgnoreCase(contact.getPhoneNo())) {
                    profileName.setText(contact.getName());
                } else {
                    profileName.setVisibility(View.GONE);
                }
                if (contact.getPhoneNo() != null) {
                    String numberWithCountryCode = "+" + mContact.getCountryCode().concat(mContact.getPhoneNo());
                    profileNumber.setText(numberWithCountryCode);
                } else {
                    profileNumber.setVisibility(View.GONE);
                }
            } else {
                profileNumber.setVisibility(View.GONE);
                profileName.setVisibility(View.GONE);
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
                        .placeholder(R.drawable.dynamic_profile)
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

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {

        RoomInfo roomInfo = dataSnapshot.getValue(RoomInfo.class);
        if (roomInfo.getName() != null) {
            Firebase membersReference = dataSnapshot.getRef().getParent().child(Constants.MEMBERS);
            membersReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot dataSnapshotUser : dataSnapshot.getChildren()) {

                        GroupMembers groupMembers = new GroupMembers();
                        groupMembers.setAdmin(dataSnapshotUser.getValue().toString());
                        groupMembers.setUserId(dataSnapshotUser.getKey());

                        groupMembersHashMap.put(dataSnapshotUser.getKey(), groupMembers);

                        Firebase membersProfileReference = dataSnapshot.getRef().getRoot().child(Constants.USERS).child(dataSnapshotUser.getKey()).child(Constants.PROFILE);
                        membersProfileReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                                for (Map.Entry m : groupMembersHashMap.entrySet()) {
                                    if (dataSnapshot.getRef().getParent().getKey().equals(m.getKey())) {
                                        GroupMembers groupMembers = (GroupMembers) m.getValue();
                                        groupMembers.setUserProfile(userProfile);
                                        groupMembersList.add(groupMembers);
                                    }
                                }
                                profileMembersAdapter.addItems(groupMembersList);
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {
                                firebaseError.getMessage();
                            }
                        });
                    }

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {
        firebaseError.getMessage();
    }
}

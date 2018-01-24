package com.yo.android.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.gson.Gson;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.adapters.GroupActionAdapter;
import com.yo.android.adapters.ProfileMembersAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.chat.ui.NonScrollListView;
import com.yo.android.helpers.Helper;
import com.yo.android.model.Contact;
import com.yo.android.model.GroupAction;
import com.yo.android.model.GroupMembers;
import com.yo.android.model.GroupSubject;
import com.yo.android.model.RoomInfo;
import com.yo.android.model.UserProfile;
import com.yo.android.model.UserProfileInfo;
import com.yo.android.pjsip.SipHelper;
import com.yo.android.ui.uploadphoto.ImagePickHelper;
import com.yo.android.util.Constants;
import com.yo.android.util.FireBaseHelper;
import com.yo.android.util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.yo.android.ui.EditGroupSubjectActivity.GROUP_SUBJECT;

public class UserProfileActivity extends BaseActivity implements SharedPreferences.OnSharedPreferenceChangeListener, ValueEventListener, AdapterView.OnItemClickListener {

    private static final String TAG = UserProfileActivity.class.getSimpleName();
    private static final int WRITE_CONTACT_PERMISSIONS = 50;
    private static final int PICK_CONTACT_REQUEST = 101;
    private static final int GROUP_SUBJECT_REQUEST = 102;

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
    @Bind(R.id.profile_name_title)
    TextView profileNameTitle;
    @Bind(R.id.number_title)
    TextView numberTitle;
    @Bind(R.id.name_card_view)
    CardView cardView;
    @Bind(R.id.edit_subject)
    ImageView editSubjectView;

    private ProfileMembersAdapter profileMembersAdapter;
    private NonScrollListView membersList;
    private Contact contact;
    private String opponentNo;
    private String voxUserName;
    private String opponentName;
    private String opponentImg;
    private boolean fromChatRooms;
    private Firebase authReference;
    private List<GroupMembers> groupMembersList;
    private String roomName;
    private String value;

    HashMap<String, GroupMembers> groupMembersHashMap;

    @Inject
    ContactsSyncManager mContactsSyncManager;
    @Inject
    FireBaseHelper fireBaseHelper;
    @Inject
    ImagePickHelper cameraIntent;
    @Inject
    YoApi.YoService yoService;


    public static void start(Activity activity, String opponentNumberTrim, String opponentNumber, String opponentImg, String opponentName, String fromChat, String chatRoomId) {
        Intent intent = createIntent(activity, opponentNumberTrim, opponentNumber, opponentImg, opponentName, fromChat, chatRoomId);
        activity.startActivity(intent);
    }

    public static void startGroup(Activity activity, String childRoomId, String roomType, String opponentImg, String opponentName, String fromChat) {
        Intent intent = createGroupIntent(activity, childRoomId, roomType, opponentImg, opponentName, fromChat);
        activity.startActivity(intent);
    }

    private static Intent createIntent(Activity activity, String opponentNumberTrim, String opponentNumber, String opponentImg, String opponentName, String fromChat, String chatRoomId) {
        Intent intent = new Intent(activity, UserProfileActivity.class);
        intent.putExtra(Constants.OPPONENT_NAME, opponentName);
        intent.putExtra(Constants.OPPONENT_CONTACT_IMAGE, opponentImg);
        intent.putExtra(Constants.OPPONENT_PHONE_NUMBER, opponentNumberTrim);
        intent.putExtra(Constants.VOX_USER_NAME, opponentNumber);
        intent.putExtra(Constants.FROM_CHAT_ROOMS, fromChat);
        intent.putExtra(Constants.CHAT_ROOM_ID, chatRoomId);
        return intent;
    }

    private static Intent createGroupIntent(Activity activity, String chatRoomId, String roomType, String opponentImg, String opponentName, String fromChat) {
        Intent intent = new Intent(activity, UserProfileActivity.class);
        intent.putExtra(Constants.GROUP_NAME, roomType);
        intent.putExtra(Constants.OPPONENT_NAME, opponentName);
        intent.putExtra(Constants.OPPONENT_CONTACT_IMAGE, opponentImg);
        intent.putExtra(Constants.CHAT_ROOM_ID, chatRoomId);
        intent.putExtra(Constants.FROM_CHAT_ROOMS, fromChat);

        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.profile_background));
            getSupportActionBar().setElevation(0);
        }
        ButterKnife.bind(this);
        enableBack();
        initCircularView();


        groupMembersList = new ArrayList<>();
        groupMembersHashMap = new HashMap<>();
        mLog.e(TAG, "Firebase token reading from pref " + preferenceEndPoint.getStringPreference(Constants.FIREBASE_TOKEN));

        authReference = fireBaseHelper.authWithCustomToken(this, preferenceEndPoint.getStringPreference(Constants.FIREBASE_TOKEN), null);
        preferenceEndPoint.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        membersList = (NonScrollListView) findViewById(R.id.members);
        profileMembersAdapter = new ProfileMembersAdapter(this);
        membersList.setAdapter(profileMembersAdapter);
        membersList.setOnItemClickListener(this);

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
                if (intent.hasExtra(Constants.VOX_USER_NAME)) {
                    voxUserName = intent.getStringExtra(Constants.VOX_USER_NAME);
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
                String names = roomName == null ? opponentName : roomName;
                contact.setName(names);
                contact.setNexgieUserName(voxUserName);
                contact.setImage(opponentImg);
                contact.setYoAppUser(true);

                setDataFromPreferences();

                // Group name
                if (intent.hasExtra(Constants.CHAT_ROOM_ID)) {
                    String firebaseRoomId = intent.getStringExtra(Constants.CHAT_ROOM_ID);
                    if (firebaseRoomId != null) {
                        if (roomName != null) {
                            Firebase roomInfo = authReference.child(Constants.ROOMS).child(firebaseRoomId).child(Constants.ROOM_INFO);
                            roomInfo.addListenerForSingleValueEvent(this);
                            roomInfo.keepSynced(true);
                            //new GroupMembersTask().execute(roomName, firebaseRoomId);
                            profileCall.setVisibility(View.GONE);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearGlideMemory(this);
        preferenceEndPoint.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

    }

    private void initCircularView() {
        profileImage.setBorderColor(getResources().getColor(R.color.white));
        profileImage.setBorderWidth(5);
    }

    private void setDataFromPreferences() {
        if (contact != null) {

            getSupportActionBar().setTitle(getResources().getString(R.string.profile));
            Contact mContact = mContactsSyncManager.getContactByVoxUserName(contact.getNexgieUserName());
            if (roomName != null) {
                Glide.with(this)
                        .load(contact.getImage())
                        .placeholder(R.drawable.chat_group)
                        .dontAnimate()
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(profileImage);
                membersList.setVisibility(View.VISIBLE);
                profileNumber.setVisibility(View.GONE);
            } else {
                if (!TextUtils.isEmpty(contact.getImage())) {
                    Glide.with(this)
                            .load(contact.getImage())
                            .placeholder(R.drawable.dynamic_profile)
                            .crossFade()
                            .dontAnimate()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(profileImage);
                } else if (mContact != null && !TextUtils.isDigitsOnly(mContact.getName())) {
                    Drawable drawable = Util.showFirstLetter(this, mContact.getName());
                    profileImage.setImageDrawable(drawable);
                }
                membersList.setVisibility(View.GONE);
                profileNumber.setVisibility(View.VISIBLE);
            }
            String name = roomName == null ? getString(R.string.name) : getString(R.string.group_name);
            String title = roomName == null ? getString(R.string.prompt_phone_number) : getString(R.string.participants);
            numberTitle.setText(title);

            if (mContact != null) {
                String numberTrim = numberFromNexgeFormat(mContact.getNexgieUserName(), mContact.getPhoneNo());
                String mName = mContact.getName().replaceAll("\\s+", "");
                if (name.equalsIgnoreCase(getString(R.string.group_name))) {
                    profileNameTitle.setText(name);
                    profileName.setText(contact.getName());
                    editSubjectView.setVisibility(View.VISIBLE);
                } else if (mContact.getName() != null && !TextUtils.isEmpty(mContact.getName()) && !isSame(mName, numberTrim)) {
                    cardView.setVisibility(View.VISIBLE);
                    profileNameTitle.setText(name);
                    profileName.setText(checkPlusSign(mContact.getName()));
                    editSubjectView.setVisibility(View.GONE);
                } else {
                    cardView.setVisibility(View.GONE);
                    editSubjectView.setVisibility(View.GONE);
                }
                if (mContact.getPhoneNo() != null) {
                    if (mContact.getCountryCode() != null) {
                        removeYoUserFromPhoneNumber("+" + mContact.getCountryCode(), mContact.getPhoneNo(), profileNumber);
                    } else {
                        removeYoUserFromPhoneNumber(null, mContact.getPhoneNo(), profileNumber);
                    }
                } else {
                    profileNumber.setVisibility(View.GONE);
                }
            } else if (contact != null) {
                String numberTrim = numberFromNexgeFormat(contact.getNexgieUserName(), contact.getPhoneNo());
                String mName = contact.getName().replaceAll("\\s+", "");
                if (name.equalsIgnoreCase(getString(R.string.group_name))) {
                    profileNameTitle.setText(name);
                    profileName.setText(contact.getName());
                    editSubjectView.setVisibility(View.VISIBLE);
                } else if (contact.getName() != null && !TextUtils.isEmpty(contact.getName()) && !isSame(mName, numberTrim)) {
                    cardView.setVisibility(View.VISIBLE);
                    profileNameTitle.setText(name);
                    profileName.setText(checkPlusSign(contact.getName()));
                    editSubjectView.setVisibility(View.GONE);
                } else {
                    cardView.setVisibility(View.GONE);
                    editSubjectView.setVisibility(View.GONE);
                }
                if (contact.getPhoneNo() != null) {
                    if (contact.getCountryCode() != null) {
                        removeYoUserFromPhoneNumber("+" + contact.getCountryCode(), contact.getPhoneNo(), profileNumber);
                    } else {
                        removeYoUserFromPhoneNumber(null, contact.getPhoneNo(), profileNumber);
                    }
                } else {
                    profileNumber.setVisibility(View.GONE);
                }
            } else {
                profileNumber.setVisibility(View.GONE);
                profileName.setVisibility(View.GONE);
            }
            if (contact.isYoAppUser()) {
                profileMsg.setImageResource(R.mipmap.ic_profile_chat);
            } else {
                profileMsg.setImageResource(R.drawable.ic_invitefriends);
            }
        }
    }

    private void removeYoUserFromPhoneNumber(String countrycode, String phoneNo, TextView profileNumber) {
        String phoneNumber = phoneNo;
        if (phoneNumber != null && phoneNumber.contains(Constants.YO_USER)) {
            // Todo this logic is not executed
            /*try {
                phoneNumber = phoneNumber.substring(phoneNumber.indexOf(Constants.YO_USER) + 6, phoneNumber.length() - 1);
                String finalNumber = countrycode == null ? phoneNumber : countrycode + phoneNumber;
                profileNumber.setText(finalNumber);
            } catch (StringIndexOutOfBoundsException e) {
            }*/
        } else if (phoneNumber != null) {
            String finalNumber = countrycode == null ? phoneNumber : countrycode + phoneNumber;
            profileNumber.setText(checkPlusSign(finalNumber));
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
        if (contact != null && contact.getNexgieUserName() != null) {
            SipHelper.makeCall(this, contact.getNexgieUserName(), false);
        }
    }

    @OnClick(R.id.profile_message)
    public void messageUser() {
        if (contact != null && contact.isYoAppUser()) {
            if (fromChatRooms) {
                finish();
            } else {
                navigateToChatScreen(contact);
            }
        } else {
            Toast.makeText(this, "Invite friends need to implement.", Toast.LENGTH_SHORT).show();
        }
    }

    /*@OnClick(R.id.profile_image)
    void changeProfilePic() {
        cameraIntent.showDialog();
    }*/

    @OnClick(R.id.edit_subject)
    public void editSubject() {
        Intent subjectIntent = new Intent(this, EditGroupSubjectActivity.class);
        subjectIntent.putExtra(GROUP_SUBJECT, profileName.getText().toString());
        startActivityForResult(subjectIntent, GROUP_SUBJECT_REQUEST);
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
                                String nameFromNumber = mContactsSyncManager.getContactNameByPhoneNumber(userProfile.getPhoneNumber());
                                if (userProfile != null && !TextUtils.isEmpty(userProfile.getMobileNumber()) && userProfile.getPhoneNumber().equalsIgnoreCase(preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER))) {
                                    userProfile.setFullName(getString(R.string.you));
                                } else if (userProfile != null && !TextUtils.isEmpty(userProfile.getPhoneNumber())) {
                                    if (!TextUtils.isEmpty(nameFromNumber)) {
                                        userProfile.setFullName(nameFromNumber);
                                    } else {
                                        userProfile.setFullName(userProfile.getMobileNumber());
                                    }
                                }
                                for (Map.Entry m : groupMembersHashMap.entrySet()) {
                                    if (dataSnapshot.getRef().getParent().getKey().equals(m.getKey())) {
                                        GroupMembers groupMembers = (GroupMembers) m.getValue();
                                        groupMembers.setUserProfile(userProfile);
                                        groupMembersList.add(groupMembers);
                                    }
                                }
                                loadAlphabetOrder(groupMembersList);
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

    private void loadAlphabetOrder(@NonNull List<GroupMembers> list) {
        try {
            Collections.sort(list, new Comparator<GroupMembers>() {
                @Override
                public int compare(GroupMembers lhs, GroupMembers rhs) {
                    return lhs.getUserProfile().getFullName().toLowerCase().compareTo(rhs.getUserProfile().getFullName().toLowerCase());
                }
            });
            for (GroupMembers groupMembers : list) {
                if (groupMembers.getUserProfile().getFullName().equalsIgnoreCase(getString(R.string.you))) {
                    list.add(list.size(), groupMembers);
                    list.remove(groupMembers);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        profileMembersAdapter.addItems(list);
    }

    private String checkPlusSign(String phoneNumber) {
        if (TextUtils.isDigitsOnly(phoneNumber)) {
            return phoneNumber.startsWith("+") ? phoneNumber : String.format(getResources().getString(R.string.plus_number), phoneNumber);
        } else {
            return phoneNumber;
        }
    }

    private boolean isSame(String name, String phoneNumber) {
        if (TextUtils.isDigitsOnly(name) && TextUtils.isDigitsOnly(phoneNumber)) {
            return name.equalsIgnoreCase(phoneNumber);
        } else if (TextUtils.isDigitsOnly(phoneNumber) && !phoneNumber.startsWith("+")) {
            return name.equalsIgnoreCase(String.format(getResources().getString(R.string.plus_number), phoneNumber));
        } else if (TextUtils.isDigitsOnly(name) && !name.startsWith("+")) {
            return String.format(getResources().getString(R.string.plus_number), name).equalsIgnoreCase(phoneNumber);
        } else {
            return name.equalsIgnoreCase(phoneNumber);
        }
    }

    private String numberFromNexgeFormat(String nexgeFormat, String phoneNumber) {
        String number = nexgeFormat != null ? nexgeFormat : phoneNumber;
        return Util.numberFromNexgeFormat(number);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        UserProfile userProfile = groupMembersList.get(position).getUserProfile();
        GroupActionAdapter userAdapter = new GroupActionAdapter(this);
        userAdapter.addItems(createAdapter(userProfile));
        userActionFromGroup(userAdapter);
    }

    public void userActionFromGroup(final GroupActionAdapter displayUserAdapter) {
        // Creating and Building the Dialog
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setAdapter(displayUserAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strName = displayUserAdapter.getItem(which).getType();
                value = displayUserAdapter.getItem(which).getValue();
                if (strName.startsWith(Constants.MESSAGE)) {
                    UserProfile mValue = new Gson().fromJson(value, UserProfile.class);
                    Contact mContact = contactMapper(mValue);
                    navigateToChatScreen(mContact);

                } else if (strName.startsWith(Constants.CALL)) {
                    SipHelper.makeCall(UserProfileActivity.this, value, false);
                } else if (strName.startsWith(Constants.Add_CONTACT)) {
                    checkForPermissions();
                }
            }
        });
        builderSingle.show();
    }

    private ArrayList<GroupAction> createAdapter(UserProfile mUserProfile) {
        String selectedUser = mUserProfile.getFullName();
        String selectedNexgeUserName = mUserProfile.getNexgeUserName();
        ArrayList<GroupAction> arrayAdapter = new ArrayList<>();
        arrayAdapter.add(new GroupAction(Constants.MESSAGE, String.format(getString(R.string.format_message), selectedUser), new Gson().toJson(mUserProfile)));
        arrayAdapter.add(new GroupAction(Constants.CALL, String.format(getString(R.string.format_call), selectedUser), selectedNexgeUserName));
        if (selectedUser.startsWith("+") || TextUtils.isDigitsOnly(selectedUser.replaceAll("\\s+", ""))) {
            arrayAdapter.add(new GroupAction(Constants.Add_CONTACT, getString(R.string.format_add), selectedUser));
        }
        return arrayAdapter;
    }

    /**
     * Add contact to phonebook
     *
     * @param phoneNumber
     */
    private void addContacts(String phoneNumber) {

        Intent i = new Intent(Intent.ACTION_INSERT);
        i.setType(ContactsContract.Contacts.CONTENT_TYPE);
        i.putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber);
        if (Integer.valueOf(Build.VERSION.SDK) > 14)
            i.putExtra("finishActivityOnSaveCompleted", true); // Fix for 4.0.3 +
        startActivityForResult(i, PICK_CONTACT_REQUEST);
    }

    private Contact contactMapper(UserProfile userProfile) {
        contact = new Contact();
        contact.setPhoneNo(userProfile.getMobileNumber());
        contact.setName(userProfile.getFullName());
        contact.setNexgieUserName(userProfile.getNexgeUserName());
        contact.setImage(userProfile.getImage());
        contact.setYoAppUser(true);
        return contact;
    }


    // Image crop
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        }
        switch (requestCode) {
            case Helper.CROP_ACTIVITY:
                if (data != null && data.hasExtra(Helper.IMAGE_PATH)) {
                    Uri imagePath = Uri.parse(data.getStringExtra(Helper.IMAGE_PATH));
                    if (imagePath != null) {
                        if (mHelper != null && !mHelper.isConnected()) {
                            mToastFactory.showToast(getResources().getString(R.string.connectivity_network_settings));
                            return;
                        } else {
                            uploadFile(new File(imagePath.getPath()), preferenceEndPoint, yoService);
                        }

                    }
                }
                break;
            case Constants.ADD_IMAGE_CAPTURE:
                try {
                    String imagePath = ImagePickHelper.mFileTemp.getPath();
                    File file = new File(imagePath);
                    Uri uri = Uri.fromFile(file);
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        if (imagePath != null) {
                            Helper.setSelectedImage(this, imagePath, true, bitmap, true);
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    // mLog.w("MoreFragment", e);
                }
                break;

            case Constants.ADD_SELECT_PICTURE:
                if (data != null) {
                    try {
                        String imagePath = ImagePickHelper.getGalleryImagePath(this, data);
                        Helper.setSelectedImage(this, imagePath, true, null, false);
                    } catch (Exception e) {
                        mLog.w("MoreFragment", e);
                    }
                }
                break;

            case GROUP_SUBJECT_REQUEST:
                if (data != null) {
                    showProgressDialog();
                    final String subject = data.getStringExtra(GROUP_SUBJECT);
                    String firebaseRoomId = getIntent().getStringExtra(Constants.CHAT_ROOM_ID);
                    try {
                        authReference.child(Constants.ROOMS).child(firebaseRoomId).child(Constants.ROOM_INFO).child("name").setValue(subject, new Firebase.CompletionListener() {
                            @Override
                            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                if (firebaseError != null) {
                                    Toast.makeText(UserProfileActivity.this, getString(R.string.group_subject_error), Toast.LENGTH_LONG).show();
                                } else {
                                    profileName.setText(subject);
                                    EventBus.getDefault().post(new GroupSubject(subject));
                                }
                                dismissProgressDialog();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }

    //Tested and image update is working
    //Make a prompt for pick a image from gallery/camera
    public void uploadFile(final File file, final PreferenceEndPoint preferenceEndPoint, YoApi.YoService yoService) {

        if (preferenceEndPoint != null && yoService != null) {
            showProgressDialog();
            String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
            // create RequestBody instance from file
            RequestBody requestFile =
                    RequestBody.create(MediaType.parse("multipart/form-data"), file);

            // MultipartBody.Part is used to send also the actual file name
            MultipartBody.Part body =
                    MultipartBody.Part.createFormData("user[avatar]", file.getName(), requestFile);
            String access = "Bearer " + preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
            yoService.updateProfile(userId, access, null, null, null, null, null, null, null, null, body).enqueue(new Callback<UserProfileInfo>() {
                @Override
                public void onResponse(Call<UserProfileInfo> call, Response<UserProfileInfo> response) {
                    dismissProgressDialog();
                    try {
                        if (response.body() != null) {
                            preferenceEndPoint.saveStringPreference(Constants.USER_AVATAR, response.body().getAvatar());
                        }
                    } finally {
                        if (response != null && response.body() != null) {
                            try {
                                response = null;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //loadImage();
                }

                @Override
                public void onFailure(Call<UserProfileInfo> call, Throwable t) {
                    dismissProgressDialog();
                }
            });
        }
    }

    private void checkForPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{"android.permission.WRITE_CONTACTS"}, WRITE_CONTACT_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == WRITE_CONTACT_PERMISSIONS) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addContacts(value);
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CONTACTS)) {
                    //Show an explanation to the user *asynchronously*
                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
                    builder.setMessage(getString(R.string.add_contact_message))
                            .setTitle(getString(R.string.important_title_message));
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ActivityCompat.requestPermissions(UserProfileActivity.this, new String[]{Manifest.permission.WRITE_CONTACTS}, WRITE_CONTACT_PERMISSIONS);
                        }
                    });
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CONTACTS}, WRITE_CONTACT_PERMISSIONS);
                } else {
                    //Never ask again and handle your app without permission.
                }
            }

        }
    }

}
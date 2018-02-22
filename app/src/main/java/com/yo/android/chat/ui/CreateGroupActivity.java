package com.yo.android.chat.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.orion.android.common.util.ToastFactory;
import com.orion.android.common.util.ToastFactoryImpl;
import com.yo.android.R;
import com.yo.android.adapters.SelectedContactsAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.CompressImage;
import com.yo.android.chat.ui.fragments.ChatFragment;
import com.yo.android.model.Contact;
import com.yo.android.model.Room;
import com.yo.android.ui.BaseActivity;
import com.yo.android.ui.uploadphoto.ImagePickHelper;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class CreateGroupActivity extends BaseActivity implements View.OnClickListener, TextWatcher {

    private static final int REQUEST_SELECTED_CONTACTS = 3;

    private ArrayList<Contact> selectedContactsArrayList;
    private String mGroupName;
    public static List<Contact> ContactsArrayList;
    File imgFile;

    @Bind(R.id.imv_new_chat_group)
    ImageView groupImage;
    @Bind(R.id.et_new_chat_group_name)
    EditText groupName;
    @Bind(R.id.add_contact)
    TextView addContactIcon;
    @Bind(R.id.selected_contacts_list)
    ListView selectedList;

    @Inject
    @Named("login")
    PreferenceEndPoint loginPrefs;
    @Inject
    ImagePickHelper cameraIntent;
    @Inject
    ToastFactory mToastFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_chat_group);
        ButterKnife.bind(this);

        cameraIntent.setActivity(this);
        ContactsArrayList = new ArrayList<>();
        selectedContactsArrayList = new ArrayList<>();
        addContactIcon.setOnClickListener(this);
        //groupName.addTextChangedListener(this);
        enableBack();

        groupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (this != null) {
                    Util.hideKeyboard(CreateGroupActivity.this, getCurrentFocus());
                }
                cameraIntent.showDialog();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_create_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.create) {
            mGroupName = groupName.getText().toString();
            String temp = mGroupName;
            if (temp.trim().length() > 0) {
                createGroup(mGroupName);
            } else {
                Util.hideKeyboard(this, getCurrentFocus());
                mToastFactory.showToast(R.string.enter_group_name);
            }

        } else if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    // Navigate to GroupContactsActivity for contact selection
    @Override
    public void onClick(View v) {
        mGroupName = groupName.getText().toString();
        Intent intent = new Intent(this, GroupContactsActivity.class);
        intent.putExtra(Constants.GROUP_NAME, mGroupName);
        intent.putParcelableArrayListExtra(Constants.SELECTED_CONTACTS, selectedContactsArrayList);
        startActivityForResult(intent, REQUEST_SELECTED_CONTACTS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        RequestOptions requestOptions = new RequestOptions()
                .priority(Priority.IMMEDIATE)
                .error(getResources().getDrawable(R.drawable.image_creategroup))
                .centerCrop()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.NONE);

        switch (requestCode) {
            case REQUEST_SELECTED_CONTACTS:
                if (resultCode == RESULT_OK) {
                    selectedContactsArrayList = data.getParcelableArrayListExtra(Constants.SELECTED_CONTACTS);
                    SelectedContactsAdapter selectedContactsAdapter = new SelectedContactsAdapter(this, selectedContactsArrayList);
                    selectedList.setAdapter(selectedContactsAdapter);
                    selectedContactsAdapter.addItems(selectedContactsArrayList);
                }
                break;
            case Constants.ADD_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    try {
                        String imagePath = ImagePickHelper.mFileTemp.getPath();
                        imgFile = new File(imagePath);
                        Glide.with(this)
                                .load(imgFile.getAbsoluteFile())
                                .apply(requestOptions)
                                //.transition(withCrossFade())
                                .into(groupImage);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

            case Constants.ADD_SELECT_PICTURE: {
                if (data != null) {
                    try {
                        String imagePath = ImagePickHelper.getGalleryImagePath(this, data);
                        imgFile = new File(imagePath);
                        //new ImageLoader(groupImage, imgFile, this).execute();
                        Glide.with(this)
                                .load(imgFile.getAbsoluteFile())
                                .apply(requestOptions)
                                //.transition(withCrossFade())
                                .into(groupImage);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            default:
                break;
        }
    }

    // Group creation
    private void createGroup(String groupName) {

        if (!selectedContactsArrayList.isEmpty()) {
            showProgressDialog();

            List<String> selectedUsers = new ArrayList<>();
            for (int i = 0; i < selectedContactsArrayList.size(); i++) {
                String userId = selectedContactsArrayList.get(i).getId();
                selectedUsers.add(userId);
            }

            MultipartBody.Part body;

            if (imgFile == null) {
                body = null;
            } else { // create RequestBody instance from file
                new CompressImage(this).compressImage(imgFile.getPath(), Constants.YO_PROFILE_PIC);
                RequestBody requestFile =
                        RequestBody.create(MediaType.parse("multipart/form-data"), imgFile);

                // MultipartBody.Part is used to send also the actual file name
                body = MultipartBody.Part.createFormData("room[image]", imgFile.getName(), requestFile);

            }

            RequestBody groupDescription = RequestBody.create(MediaType.parse("room[group_name]"), groupName);

            String access = "Bearer " + preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
            yoService.createGroupAPI(access, selectedUsers, groupDescription, body).enqueue(new Callback<Room>() {

                @Override
                public void onResponse(Call<Room> call, Response<Room> response) {
                    try {
                        if (response.isSuccessful()) {
                            if (!ContactsArrayList.isEmpty()) {
                                ContactsArrayList.clear();
                            }
                            Intent intent = new Intent();
                            Room body1 = response.body();
                            long time = addTimeStamp(body1.getCreated_at());
                            body1.setTime(time);
                            intent.putExtra(Constants.ROOM, body1);
                            setResult(Activity.RESULT_OK, intent);
                            updateCache(body1);
                            finish();
                        } else {
                            mToastFactory.showToast(R.string.group_creation_error);
                        }
                        dismissProgressDialog();
                    } finally {
                        if(response != null && response.body() != null) {
                            try {
                                response = null;
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<Room> call, Throwable t) {
                    dismissProgressDialog();
                }
            });
        } else {
            Util.hideKeyboard(this, getCurrentFocus());
            mToastFactory.showToast(R.string.select_atleast_one_contact);
        }
    }

    private long addTimeStamp(String timeStamp) {
        String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS";
        DateFormat sdf = new SimpleDateFormat(DATE_FORMAT_PATTERN) {
            public Date parse(String source, ParsePosition pos) {
                return super.parse(source.replaceFirst(":(?=[0-9]{2}$)", ""), pos);
            }
        };
        try {
            Date mDate = sdf.parse(timeStamp);
            long timeInMilliseconds = mDate.getTime();
            return timeInMilliseconds;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void updateCache(final Room room) {
        String rooms = loginPrefs.getStringPreference(Constants.FIRE_BASE_ROOMS);
        ArrayList<String> roomsListdata = new Gson().fromJson(rooms,
                new TypeToken<ArrayList<String>>() {
                }.getType());
        roomsListdata.add(room.getFirebaseRoomId());
        String listString = new Gson().toJson(roomsListdata,
                new TypeToken<ArrayList<String>>() {
                }.getType());
        loginPrefs.saveStringPreference(Constants.FIRE_BASE_ROOMS, listString);
    }

    @Override
    protected void onDestroy() {
        clearGlideMemory(this);
        super.onDestroy();
        if (!ContactsArrayList.isEmpty()) {
            ContactsArrayList.clear();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // before text change
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (count == 1) {
            if (TextUtils.isDigitsOnly(s)) {
                groupName.setText("");
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
       // after text change
    }
}
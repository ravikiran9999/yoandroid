package com.yo.android.chat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.adapters.SelectedContactsAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.model.Contact;
import com.yo.android.model.Room;
import com.yo.android.ui.BaseActivity;
import com.yo.android.ui.UpdateProfileActivity;
import com.yo.android.ui.uploadphoto.ImageLoader;
import com.yo.android.ui.uploadphoto.ImagePickHelper;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.io.File;
import java.util.ArrayList;
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

public class CreateGroupActivity extends BaseActivity implements View.OnClickListener {

    private TextView groupName;
    private ListView selectedList;
    private ArrayList<Contact> selectedContactsArrayList;
    private String mGroupName;


    private static final int REQUEST_SELECTED_CONTACTS = 3;

    public static List<Contact> ContactsArrayList;
    File imgFile;

    @Inject
    @Named("login")
    PreferenceEndPoint loginPrefs;

    @Inject
    ImagePickHelper cameraIntent;

    @Bind(R.id.imv_new_chat_group)
    ImageView groupImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_chat_group);
        ButterKnife.bind(this);

        cameraIntent.setActivity(this);
        groupName = (TextView) findViewById(R.id.et_new_chat_group_name);
        TextView addContactIcon = (TextView) findViewById(R.id.add_contact);
        selectedList = (ListView) findViewById(R.id.selected_contacts_list);
        ContactsArrayList = new ArrayList<>();
        selectedContactsArrayList = new ArrayList<>();
        addContactIcon.setOnClickListener(this);
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
        getMenuInflater().inflate(R.menu.menu_create_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.create) {
            createGroup();
        } else if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        if (!groupName.getText().toString().equalsIgnoreCase("")) {

            mGroupName = groupName.getText().toString();
            Intent intent = new Intent(this, GroupContactsActivity.class);
            intent.putExtra(Constants.GROUP_NAME, mGroupName);
            startActivityForResult(intent, REQUEST_SELECTED_CONTACTS);
        } else {
            Toast.makeText(this, "Please enter group name", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

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
                        String imagePath = cameraIntent.mFileTemp.getPath();
                        imgFile = new File(imagePath);
                        new ImageLoader(groupImage, imgFile, this).execute();

                    } catch (Exception e) {
                    }
                }
                break;

            case Constants.ADD_SELECT_PICTURE: {
                if (data != null) {
                    try {
                        String imagePath = ImagePickHelper.getGalleryImagePath(this, data);
                        imgFile = new File(imagePath);
                        new ImageLoader(groupImage, imgFile, this).execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            default:
                break;
        }
    }

    private void createGroup() {

        if (selectedContactsArrayList.size() > 0) {
            showProgressDialog();

            List<String> selectedUsers = new ArrayList<>();
            for (int i = 0; i < selectedContactsArrayList.size(); i++) {
                String userId = selectedContactsArrayList.get(i).getId();
                selectedUsers.add(userId);
            }

            String access = preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
            yoService.createGroupAPI(access, selectedUsers, mGroupName).enqueue(new Callback<Room>() {
                @Override
                public void onResponse(Call<Room> call, Response<Room> response) {
                    response.body();
                    if (!ContactsArrayList.isEmpty()) {
                        ContactsArrayList.clear();
                    }
                    dismissProgressDialog();
                    finish();
                }

                @Override
                public void onFailure(Call<Room> call, Throwable t) {
                    dismissProgressDialog();
                }
            });
        } else {
            Toast.makeText(this, "Atleast one contact should be selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!ContactsArrayList.isEmpty()) {
            ContactsArrayList.clear();
        }
    }
}



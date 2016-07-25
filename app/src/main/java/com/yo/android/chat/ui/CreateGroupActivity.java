package com.yo.android.chat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.yo.android.util.Constants;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateGroupActivity extends BaseActivity implements View.OnClickListener {

    private TextView groupName;
    private ListView selectedList;
    private SelectedContactsAdapter selectedContactsAdapter;
    private ArrayList<Contact> contactArrayList;
    private String mGroupName;

    private static final int REQUEST_SELECTED_CONTACTS = 1;

    @Inject
    @Named("login")
    PreferenceEndPoint loginPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_chat_group);
        groupName = (TextView) findViewById(R.id.et_new_chat_group_name);
        TextView addContactIcon = (TextView) findViewById(R.id.add_contact);
        selectedList = (ListView) findViewById(R.id.selected_contacts_list);
        contactArrayList = new ArrayList<>();
        addContactIcon.setOnClickListener(this);
        enableBack();
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
        } else if(item.getItemId() == android.R.id.home) {
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
        if (requestCode == REQUEST_SELECTED_CONTACTS) {
            if (resultCode == RESULT_OK) {
                contactArrayList = data.getParcelableArrayListExtra(Constants.SELECTED_CONTACTS);
                selectedContactsAdapter = new SelectedContactsAdapter(this, contactArrayList);
                selectedList.setAdapter(selectedContactsAdapter);
                selectedContactsAdapter.addItems(contactArrayList);
            }
        }
    }

    private void createGroup() {

        if (contactArrayList.size() > 0) {
            showProgressDialog();

            List<String> selectedUsers = new ArrayList<>();
            for (int i = 0; i < contactArrayList.size(); i++) {
                String userId = contactArrayList.get(i).getId();
                selectedUsers.add(userId);
            }
            String access = loginPrefs.getStringPreference(YoApi.ACCESS_TOKEN);
            yoService.createGroupAPI(access, selectedUsers, mGroupName).enqueue(new Callback<Room>() {
                @Override
                public void onResponse(Call<Room> call, Response<Room> response) {
                    response.body();
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
}



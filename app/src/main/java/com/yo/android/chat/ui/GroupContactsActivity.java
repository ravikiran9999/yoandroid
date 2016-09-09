package com.yo.android.chat.ui;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.adapters.GroupContactsListAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.model.Contact;
import com.yo.android.ui.BaseActivity;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupContactsActivity extends BaseActivity {

    private GroupContactsListAdapter groupContactsListAdapter;
    private ListView listView;
    private String groupName;

    @Inject
    YoApi.YoService yoService;
    @Inject
    ContactsSyncManager mContactsSyncManager;

    @Inject
    @Named("login")
    PreferenceEndPoint loginPrefs;

    public GroupContactsActivity() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
        //groupName = getString(Constants.GROUP_NAME);
        setContentView(R.layout.fragment_yo_contacts);
        groupName = getIntent().getStringExtra(Constants.GROUP_NAME);
        listView = (ListView) findViewById(R.id.lv_app_contacts);

        groupContactsListAdapter = new GroupContactsListAdapter(this);
        listView.setAdapter(groupContactsListAdapter);

        if (CreateGroupActivity.ContactsArrayList.isEmpty()) {
            getYoAppUsers();
        } else {
            groupContactsListAdapter.addItems(CreateGroupActivity.ContactsArrayList);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_multiple_contacts, menu);
        Util.prepareContactsSearch(this, menu, groupContactsListAdapter, Constants.Yo_CONT_FRAG);
        Util.changeSearchProperties(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.done) {
            done();
        }
        return true;
    }

    private void getYoAppUsers() {
        showProgressDialog();

        mContactsSyncManager.loadContacts(new Callback<List<Contact>>() {
            @Override
            public void onResponse(Call<List<Contact>> call, Response<List<Contact>> response) {
                List<Contact> contactList = new ArrayList<>();
                if (response.body() != null) {
                    for (int i = 0; i < response.body().size(); i++) {
                        if (response.body().get(i).getYoAppUser()) {
                            contactList.add(response.body().get(i));
                        }
                    }
                    groupContactsListAdapter.addItems(contactList);
                    CreateGroupActivity.ContactsArrayList = contactList;
                }
                dismissProgressDialog();
            }

            @Override
            public void onFailure(Call<List<Contact>> call, Throwable t) {
                dismissProgressDialog();
            }
        });
    }

    @Override
    public void showProgressDialog() {
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissProgressDialog() {
        findViewById(R.id.progress).setVisibility(View.GONE);
    }

    private void done() {
        ArrayList<Contact> contactArrayList = new ArrayList<>();
        for (int i = 0; i < CreateGroupActivity.ContactsArrayList.size(); i++) {
            if (CreateGroupActivity.ContactsArrayList.get(i).isSelected()) {
                contactArrayList.add(CreateGroupActivity.ContactsArrayList.get(i));
            }
        }

        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(Constants.SELECTED_CONTACTS, contactArrayList);
        setResult(RESULT_OK, intent);
        finish();
    }
}

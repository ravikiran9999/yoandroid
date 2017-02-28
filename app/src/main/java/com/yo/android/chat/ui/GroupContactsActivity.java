package com.yo.android.chat.ui;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.adapters.GroupContactsListAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.helpers.Helper;
import com.yo.android.model.Contact;
import com.yo.android.ui.BaseActivity;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupContactsActivity extends BaseActivity {

    private GroupContactsListAdapter groupContactsListAdapter;
    @Bind(R.id.lv_app_contacts)
    ListView listView;
    @Bind(R.id.side_index)
    ListView layout;
    @Bind(R.id.no_contacts)
    TextView textView;
    private String groupName;
    private Menu mMenu;
    @Inject
    YoApi.YoService yoService;
    @Inject
    ContactsSyncManager mContactsSyncManager;

    @Inject
    @Named("login")
    PreferenceEndPoint loginPrefs;

    @Bind(R.id.no_search_results)
    protected TextView noSearchResult;

    List<Contact> contactsList = null;
    List<Contact> selectedContactsList = null;

    public GroupContactsActivity() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_yo_contacts);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        groupName = getIntent().getStringExtra(Constants.GROUP_NAME);
        groupContactsListAdapter = new GroupContactsListAdapter(this);
        listView.setAdapter(groupContactsListAdapter);
        selectedContactsList = getIntent().getParcelableArrayListExtra(Constants.SELECTED_CONTACTS);
        contactsList = CreateGroupActivity.ContactsArrayList;
        changeSelectedContactStatus(clearSelection(contactsList));

        if (contactsList.isEmpty()) {
            getYoAppUsers();
        } else {
            groupContactsListAdapter.addItems(contactsList);
            Helper.displayIndex(this, layout, contactsList, listView);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_multiple_contacts, menu);
        mMenu = menu;
        Util.changeSearchProperties(mMenu);
        return super.onCreateOptionsMenu(mMenu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Util.prepareContactsSearch(this, mMenu, groupContactsListAdapter, Constants.CONT_FRAG, noSearchResult);

        if (item.getItemId() == R.id.done) {
            done();
        } else {
            if (mMenu != null) {
                Util.changeMenuItemsVisibility(mMenu, R.id.menu_search, false);
                Util.registerSearchLister(this, mMenu);
            }
        }
        return super.onOptionsItemSelected(item);
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

                    if (contactList.isEmpty()) {
                        listView.setVisibility(View.GONE);
                        textView.setVisibility(View.VISIBLE);
                    } else {
                        loadInAlphabeticalOrder(contactList);
                    }
                } else {
                    listView.setVisibility(View.GONE);
                    textView.setVisibility(View.VISIBLE);
                }

                dismissProgressDialog();
            }

            @Override
            public void onFailure(Call<List<Contact>> call, Throwable t) {
                dismissProgressDialog();
                textView.setText(getString(R.string.no_contacts_found));
                textView.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
                mToastFactory.newToast(getString(R.string.room_id_not_created), Toast.LENGTH_SHORT);
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
        List<Contact> contacts = contactsList;
        for (int i = 0; i < contacts.size(); i++) {
            if (contacts.get(i).isSelected()) {
                contactArrayList.add(contacts.get(i));
            }
        }
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(Constants.SELECTED_CONTACTS, contactArrayList);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void changeSelectedContactStatus(List<Contact> mContactList) {

        for (Contact allContacts : mContactList) {
            for (Contact selectContact : selectedContactsList) {
                if (allContacts != null && selectContact != null && allContacts.getPhoneNo().equalsIgnoreCase(selectContact.getPhoneNo())) {
                    allContacts.setSelected(true);

                }
            }
        }
    }

    private List<Contact> clearSelection(@NonNull List<Contact> mContactList) {
        List<Contact> allContactList = new ArrayList<>();
        for (Contact contacts : mContactList) {
            contacts.setSelected(false);
            allContactList.add(contacts);
        }
        return allContactList;
    }

    private void loadInAlphabeticalOrder(List<Contact> contactList) {
        Collections.sort(contactList, new Comparator<Contact>() {
            @Override
            public int compare(Contact lhs, Contact rhs) {
                return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
            }
        });
        Helper.displayIndex(this, layout, contactList, listView);
        groupContactsListAdapter.addItems(contactList);
        CreateGroupActivity.ContactsArrayList.addAll(contactList);
        groupContactsListAdapter.notifyDataSetChanged();
    }
}
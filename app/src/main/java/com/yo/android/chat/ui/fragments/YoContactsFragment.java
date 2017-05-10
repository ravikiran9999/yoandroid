package com.yo.android.chat.ui.fragments;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.orion.android.common.util.ConnectivityHelper;
import com.yo.android.R;
import com.yo.android.adapters.AppContactsListAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.chat.ui.CreateGroupActivity;
import com.yo.android.helpers.Helper;
import com.yo.android.model.ChatMessage;
import com.yo.android.model.Contact;
import com.yo.android.model.Share;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class YoContactsFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    private static final int CREATE_GROUP_RESULT = 100;
    private ArrayList<ChatMessage> forwardChatMessages;
    private AppContactsListAdapter appContactsListAdapter;
    private List<Contact> tempList = new ArrayList<>();
    private Share share;
    private Menu menu;
    private Activity activity;

    @Bind(R.id.lv_app_contacts)
    ListView listView;
    @Bind(R.id.side_index)
    ListView layout;
    @Bind(R.id.no_search_results)
    TextView noResults;

    @Inject
    YoApi.YoService yoService;
    @Inject
    ContactsSyncManager mContactsSyncManager;
    @Inject
    ConnectivityHelper mHelper;

    private MenuItem collapseView;

    public YoContactsFragment() {
        // Required empty public constructor
    }

    public Menu getMenu() {
        return menu;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_yo_contacts, container, false);
        ButterKnife.bind(this, view);
        listView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity = getActivity();
        if (getArguments() != null && getArguments().getParcelableArrayList(Constants.CHAT_FORWARD) != null) {
            forwardChatMessages = getArguments().getParcelableArrayList(Constants.CHAT_FORWARD);
        } else if(getArguments() != null && getArguments().getParcelable(Constants.CHAT_SHARE) != null) {
            share = getArguments().getParcelable(Constants.CHAT_SHARE);
        }
        if (activity != null) {
            appContactsListAdapter = new AppContactsListAdapter(activity.getApplicationContext());
            listView.setAdapter(appContactsListAdapter);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Util.prepareContactsSearch(activity, menu, appContactsListAdapter, Constants.Yo_CONT_FRAG, noResults);

        if (item.getItemId() == android.R.id.home && activity != null) {
            activity.finish();
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mHelper.isConnected()) {
            getYoAppUsers();
        } else if (!mHelper.isConnected()) {
            List<Contact> cacheContactsList = mContactsSyncManager.getCachContacts();
            if (cacheContactsList != null && !cacheContactsList.isEmpty()) {
                loadInAlphabeticalOrder(cacheContactsList);
            } else {
                noResults.setText(getString(R.string.no_contacts_found));
                noResults.setVisibility(View.VISIBLE);
                mToastFactory.newToast(getString(R.string.room_id_not_created), Toast.LENGTH_SHORT);
            }

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_app_contacts, menu);
        this.menu = menu;
        collapseView = menu.findItem(R.id.menu_search);
        MenuItemCompat.setOnActionExpandListener(collapseView, new MenuItemCompat.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {

                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                appContactsListAdapter.addItems(tempList);
                return true;
            }
        });
        if (activity != null) {
            Util.changeSearchProperties(menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (collapseView != null) {
            collapseView.collapseActionView();
        }
        itemClick(position);
    }

    private void itemClick(int position) {
        Contact contact;
        try {
            if (appContactsListAdapter.temp != null && !appContactsListAdapter.temp.isEmpty()) {
                contact = appContactsListAdapter.temp.get(position); // selected contact on performing search
                appContactsListAdapter.temp.clear();
            } else {
                contact = (Contact) listView.getItemAtPosition(position);
            }

            if (position == 0 && contact.getNexgieUserName() == null && contact.getPhoneNo() == null && contact.getFirebaseRoomId() == null && activity != null) {
                startActivityForResult(new Intent(activity, CreateGroupActivity.class), CREATE_GROUP_RESULT);
            } else if (activity != null && contact.getNexgieUserName() != null && forwardChatMessages != null) {
                ChatActivity.start(activity, contact, forwardChatMessages);
            } else {
                ChatActivity.start(activity, contact, share);
            }
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    private void getYoAppUsers() {

        final List<Contact> contacts = mContactsSyncManager.getContacts();
        if (!contacts.isEmpty()) {
            loadInAlphabeticalOrder(contacts);
        } else if (contacts.isEmpty()) {
            showProgressDialog();
        }

        mContactsSyncManager.loadContacts(new Callback<List<Contact>>() {
            @Override
            public void onResponse(Call<List<Contact>> call, Response<List<Contact>> response) {
                loadInAlphabeticalOrder(response.body());
                dismissProgressDialog();
            }

            @Override
            public void onFailure(Call<List<Contact>> call, Throwable t) {
                dismissProgressDialog();
                if (contacts.isEmpty()) {
                    noResults.setText(getString(R.string.no_contacts_found));
                    noResults.setVisibility(View.VISIBLE);
                    mToastFactory.newToast(getString(R.string.room_id_not_created), Toast.LENGTH_SHORT);
                }
            }
        });
    }

    private void loadInAlphabeticalOrder(@NonNull List<Contact> contactList) {
        String newGroupTxt = "";
        if (activity != null) {
            newGroupTxt = activity.getResources().getString(R.string.new_group);
        }
        if (getArguments().getBoolean(Constants.IS_CHAT_FORWARD, false)) {
            List<Contact> yoList = new ArrayList<>();

            for (Contact contact : contactList) {
                if (contact.getYoAppUser()) {
                    yoList.add(contact);
                }
            }
            contactList = yoList;
        }
        Collections.sort(contactList, new Comparator<Contact>() {
            @Override
            public int compare(Contact lhs, Contact rhs) {
                return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
            }
        });

        if (activity != null) {
            Helper.displayIndex(activity, layout, contactList, listView);
        }
        ArrayList<String> stringArrayList = new ArrayList<>();
        for (Contact contact : contactList) {
            if (getArguments().getBoolean(Constants.IS_CHAT_FORWARD, false)) {
                if (contact.getYoAppUser()) {
                    stringArrayList.add(contact.getName());
                }
            } else {
                stringArrayList.add(contact.getName());
            }
        }
        if (getArguments() != null && !getArguments().getBoolean(Constants.IS_CHAT_FORWARD, false) && !contactList.isEmpty()) {

            if (!stringArrayList.contains(newGroupTxt)) {
                Contact createGroup = new Contact();
                createGroup.setName(newGroupTxt);
                createGroup.setNexgieUserName(null);
                createGroup.setPhoneNo(null);
                createGroup.setFirebaseRoomId(null);
                contactList.add(0, createGroup);
            } else {
                Contact contact = contactList.get(stringArrayList.indexOf(newGroupTxt));
                if (stringArrayList.contains(newGroupTxt)) {
                    contactList.remove(stringArrayList.indexOf(newGroupTxt));
                }
                contactList.add(0, contact);

            }
        } else if (getArguments().getBoolean(Constants.IS_CHAT_FORWARD, false) && stringArrayList.contains(newGroupTxt)) {
            contactList.remove(stringArrayList.indexOf(newGroupTxt));
        }
        tempList = contactList;
        appContactsListAdapter.addItems(contactList);
    }


    @Override
    public void showProgressDialog() {
        if (getView() != null) {
            getView().findViewById(R.id.progress).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && activity != null) {
            activity.finish();
        }
    }

    @Override
    public void dismissProgressDialog() {
        if (getView() != null) {
            getView().findViewById(R.id.progress).setVisibility(View.GONE);
        }
    }
}

package com.yo.android.chat.ui.fragments;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

import com.yo.android.R;
import com.yo.android.adapters.AppContactsListAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.chat.ui.CreateGroupActivity;
import com.yo.android.helpers.Helper;
import com.yo.android.model.ChatMessage;
import com.yo.android.model.Contact;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

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
    private ListView listView;
    private ListView layout;
    private List<Contact> tempList = new ArrayList<>();
    private Menu menu;

    @Inject
    YoApi.YoService yoService;
    @Inject
    ContactsSyncManager mContactsSyncManager;

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
        listView = (ListView) view.findViewById(R.id.lv_app_contacts);
        layout = (ListView) view.findViewById(R.id.side_index);
        listView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getArguments() != null) {
            forwardChatMessages = getArguments().getParcelableArrayList(Constants.CHAT_FORWARD);
        }

        appContactsListAdapter = new AppContactsListAdapter(getActivity().getApplicationContext());
        listView.setAdapter(appContactsListAdapter);
        getYoAppUsers();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            getActivity().finish();
        }
        return super.onOptionsItemSelected(item);

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
        Util.prepareContactsSearch(getActivity(), menu, appContactsListAdapter, Constants.Yo_CONT_FRAG);
        Util.changeSearchProperties(menu);
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
            } else {
                contact = (Contact) listView.getItemAtPosition(position);
            }

            if (position == 0 && contact.getVoxUserName() == null && contact.getPhoneNo() == null && contact.getFirebaseRoomId() == null) {
                startActivityForResult(new Intent(getActivity(), CreateGroupActivity.class), CREATE_GROUP_RESULT);
            } else {
                if (forwardChatMessages != null && contact != null && contact.getYoAppUser()) {
                    navigateToChatScreen(contact, forwardChatMessages);
                } else if (contact != null && contact.getYoAppUser()) {
                    navigateToChatScreen(contact);
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void navigateToChatScreen(Contact contact, ArrayList<ChatMessage> forward) {

        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(Constants.CONTACT, contact);
        intent.putParcelableArrayListExtra(Constants.CHAT_FORWARD, forward);
        intent.putExtra(Constants.TYPE, Constants.CONTACT);
        startActivity(intent);
        getActivity().finish();
    }

    private void navigateToChatScreen(Contact contact) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(Constants.CONTACT, contact);
        intent.putExtra(Constants.TYPE, Constants.CONTACT);
        startActivity(intent);
        getActivity().finish();
    }

    private void getYoAppUsers() {
        List<Contact> contacts = mContactsSyncManager.getContacts();
        if (!contacts.isEmpty()) {
            loadInAlphabeticalOrder(mContactsSyncManager.getContacts());
        } else if (contacts.isEmpty()) {
            showProgressDialog();

            mContactsSyncManager.loadContacts(new Callback<List<Contact>>() {
                @Override
                public void onResponse(Call<List<Contact>> call, Response<List<Contact>> response) {
                    loadInAlphabeticalOrder(mContactsSyncManager.getContacts());
                    dismissProgressDialog();
                }

                @Override
                public void onFailure(Call<List<Contact>> call, Throwable t) {
                    dismissProgressDialog();
                }
            });
        }

    }

    private void loadInAlphabeticalOrder(List<Contact> contactList) {
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
        Helper.displayIndex(getActivity(), layout, contactList, listView);
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

            if (!stringArrayList.contains(getResources().getString(R.string.new_group))) {
                Contact createGroup = new Contact();
                createGroup.setName(getResources().getString(R.string.new_group));
                createGroup.setVoxUserName(null);
                createGroup.setPhoneNo(null);
                createGroup.setFirebaseRoomId(null);
                contactList.add(0, createGroup);
            } else {
                Contact contact = contactList.get(stringArrayList.indexOf(getResources().getString(R.string.new_group)));
                if (stringArrayList.contains(getResources().getString(R.string.new_group))) {
                    contactList.remove(stringArrayList.indexOf(getResources().getString(R.string.new_group)));
                }
                contactList.add(0, contact);

            }
        } else if (getArguments().getBoolean(Constants.IS_CHAT_FORWARD, false) && stringArrayList.contains(getResources().getString(R.string.new_group))) {

            contactList.remove(stringArrayList.indexOf(getResources().getString(R.string.new_group)));
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
        if (resultCode == Activity.RESULT_OK) {
            getActivity().finish();
        }
    }

    @Override
    public void dismissProgressDialog() {
        if (getView() != null) {
            getView().findViewById(R.id.progress).setVisibility(View.GONE);
        }
    }
}

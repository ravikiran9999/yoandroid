package com.yo.android.chat.ui.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.yo.android.R;
import com.yo.android.adapters.AppContactsListAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.model.ChatMessage;
import com.yo.android.model.Contact;
import com.yo.android.model.Registration;
import com.yo.android.model.Room;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class YoContactsFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    private ArrayList<Registration> arrayOfUsers;
    private ArrayList<ChatMessage> forwardChatMessages;
    private AppContactsListAdapter appContactsListAdapter;
    private ListView listView;
    private Menu menu;

    @Inject
    YoApi.YoService yoService;
    @Inject
    ContactsSyncManager mContactsSyncManager;

    public Menu getMenu() {
        return menu;
    }

    public YoContactsFragment() {
        // Required empty public constructor
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
        listView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getYoAppUsers();

        if (getArguments() != null) {
            forwardChatMessages = getArguments().getParcelableArrayList(Constants.CHAT_FORWARD);
        }

        arrayOfUsers = new ArrayList<>();
        appContactsListAdapter = new AppContactsListAdapter(getActivity().getApplicationContext());
        listView.setAdapter(appContactsListAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_app_contacts, menu);
        this.menu = menu;
        Util.prepareContactsSearch(getActivity(), menu,appContactsListAdapter, Constants.Yo_CONT_FRAG);
        Util.changeSearchProperties(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Contact contact = (Contact) listView.getItemAtPosition(position);

        /*if (forwardChatMessages != null) {
            navigateToChatScreen(contact.getFirebaseRoomId(), opponentPhoneNumber, forwardChatMessages, contact.getId());

        } else if (contact.getFirebaseRoomId() != null) {
            navigateToChatScreen(contact.getFirebaseRoomId(), opponentPhoneNumber, yourPhoneNumber, null);
        } else {
            navigateToChatScreen("", opponentPhoneNumber, yourPhoneNumber, contact.getId());
        }*/

        if (forwardChatMessages != null) {
            navigateToChatScreen(contact, forwardChatMessages);

        } else {
            navigateToChatScreen(contact);
        }
    }

    private void navigateToChatScreen(String roomId, String opponentPhoneNumber, String yourPhoneNumber, String opponentId) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(Constants.CHAT_ROOM_ID, roomId);
        intent.putExtra(Constants.OPPONENT_PHONE_NUMBER, opponentPhoneNumber);
        intent.putExtra(Constants.OPPONENT_ID, opponentId);
        intent.putExtra(Constants.YOUR_PHONE_NUMBER, yourPhoneNumber);
        startActivity(intent);
        getActivity().finish();
    }

    /*private void navigateToChatScreen(String roomId, String opponentPhoneNumber, ArrayList<ChatMessage> forward, String opponentId) {
        *//*if (preferenceEndPoint.getStringPreference(Constants.CHAT_FORWARD) != null) {
            preferenceEndPoint.removePreference(Constants.CHAT_FORWARD);
        }*//*

        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(Constants.CHAT_ROOM_ID, roomId);
        intent.putExtra(Constants.OPPONENT_PHONE_NUMBER, opponentPhoneNumber);
        intent.putExtra(Constants.OPPONENT_ID, opponentId);
        intent.putParcelableArrayListExtra(Constants.CHAT_FORWARD, forward);
        startActivity(intent);
        getActivity().finish();
    }*/

    private void navigateToChatScreen(Contact contact,  ArrayList<ChatMessage> forward) {

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
                    appContactsListAdapter.addItems(contactList);
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
        if (getView() != null) {
            getView().findViewById(R.id.progress).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void dismissProgressDialog() {
        if (getView() != null) {
            getView().findViewById(R.id.progress).setVisibility(View.GONE);
        }
    }
}

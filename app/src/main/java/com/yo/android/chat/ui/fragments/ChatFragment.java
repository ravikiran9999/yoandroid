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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yo.android.R;
import com.yo.android.adapters.ContactsListAdapter;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.helpers.DatabaseHelper;
import com.yo.android.model.ChatRoom;
import com.yo.android.model.Registration;
import com.yo.android.util.Constants;

import java.util.ArrayList;

import javax.inject.Inject;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends BaseFragment implements AdapterView.OnItemClickListener {


    private ArrayList<Registration> arrayOfUsers;
    private ContactsListAdapter contactsListAdapter;
    private ListView listView;
    @Inject
    DatabaseHelper databaseHelper;


    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_chat, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        listView = (ListView) view.findViewById(R.id.lv_contacts);
        listView.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getRegisteredAppUsers();
        arrayOfUsers = new ArrayList<>();
        contactsListAdapter = new ContactsListAdapter(getActivity().getApplicationContext());
        listView.setAdapter(contactsListAdapter);

    }

    private void getRegisteredAppUsers() {
        showProgressDialog();
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constants.APP_USERS);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                arrayOfUsers.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Registration registeredUsers = child.getValue(Registration.class);
                    if (!(registeredUsers.getPhoneNumber().equals(preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER)))) {
                        arrayOfUsers.add(registeredUsers);
                    }
                }
                contactsListAdapter.addItems(arrayOfUsers);
                dismissProgressDialog();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dismissProgressDialog();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Registration registration = (Registration) listView.getItemAtPosition(position);
        String yourPhoneNumber = preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER);
        String opponentPhoneNumber = registration.getPhoneNumber();
        showUserChatScreen(yourPhoneNumber, opponentPhoneNumber);
    }

    private void showUserChatScreen(@NonNull final String yourPhoneNumber, @NonNull final String opponentPhoneNumber) {
        final String roomCombination1 = yourPhoneNumber + ":" + opponentPhoneNumber;
        final String roomCombination2 = opponentPhoneNumber + ":" + yourPhoneNumber;
        DatabaseReference databaseRoomReference = FirebaseDatabase.getInstance().getReference(Constants.ROOM);
        databaseRoomReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean value1 = dataSnapshot.hasChild(roomCombination1);
                boolean value2 = dataSnapshot.hasChild(roomCombination2);
                if (value1) {
                    navigateToChatScreen(roomCombination1, opponentPhoneNumber);
                } else if (value2) {
                    navigateToChatScreen(roomCombination2, opponentPhoneNumber);
                } else {
                    String chatRoomId = yourPhoneNumber + ":" + opponentPhoneNumber;
                    ChatRoom chatRoom = new ChatRoom(yourPhoneNumber, opponentPhoneNumber, chatRoomId);
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Constants.ROOM);
                    DatabaseReference databaseRoomReference = databaseReference.child(chatRoomId);
                    databaseRoomReference.setValue(chatRoom);
                    navigateToChatScreen(chatRoomId, opponentPhoneNumber);
                    //databaseHelper.insertChatRoomObjectToDatabase(chatRoom);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void navigateToChatScreen(String roomId, String opponentPhoneNumber) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(Constants.CHAT_ROOM_ID, roomId);
        intent.putExtra(Constants.OPPONENT_PHONE_NUMBER, opponentPhoneNumber);
        startActivity(intent);
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

package com.yo.android.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yo.android.R;
import com.yo.android.adapters.ContactsListAdapter;
import com.yo.android.chat.ui.UserChatFragment;
import com.yo.android.helpers.DatabaseHelper;
import com.yo.android.model.ChatRoom;
import com.yo.android.model.Registration;
import com.yo.android.util.DatabaseConstant;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */

public class ContactsFragment extends Fragment implements AdapterView.OnItemClickListener {

    private ArrayList<Registration> arrayOfUsers;
    private ContactsListAdapter contactsListAdapter;
    private int incrementalContactsCount;
    private ListView listView;
    private DatabaseHelper databaseHelper;

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new DatabaseHelper(getActivity().getApplicationContext());
        getRoomIdFromDatabase();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        listView = (ListView) view.findViewById(R.id.lv_contacts);
        getRegisteredAppUsers();
        arrayOfUsers = new ArrayList<>();
        contactsListAdapter = new ContactsListAdapter(getActivity().getApplicationContext());
        listView.setAdapter(contactsListAdapter);

        incrementalContactsCount = 0;

        listView.setOnItemClickListener(this);
        return view;
    }

    /*@Override
    public void onResume() {
        super.onResume();
        if(contactsListAdapter.getViewTypeCount() > 0) {
            ((BaseActivity) getActivity()).dismissProgressDialog();
        }
    }*/

    private void getRegisteredAppUsers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(DatabaseConstant.APP_USERS);

        // Retrieve new posts as they are added to the database
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Registration registeredUsers = dataSnapshot.getValue(Registration.class);
                arrayOfUsers.add(registeredUsers);
                contactsListAdapter.addItems(arrayOfUsers);
                int contactsCount = (int) dataSnapshot.getChildrenCount();

                if (incrementalContactsCount == contactsCount) {
                    //((BaseActivity) getActivity()).dismissProgressDialog();
                }
                incrementalContactsCount++;
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Registration registration = (Registration) listView.getItemAtPosition(position);
        String yourPhoneNumber = "1234567899";
        String opponentPhoneNumber = registration.getPhoneNumber();
        ChatRoom chatRoom = null;
        String chatRoomId = getRoomId(yourPhoneNumber, opponentPhoneNumber);
        if (chatRoomId.equals("")) {
            if (!yourPhoneNumber.isEmpty() && !opponentPhoneNumber.isEmpty()) {
                chatRoomId = yourPhoneNumber + ":" + opponentPhoneNumber;
                chatRoom = new ChatRoom(yourPhoneNumber, opponentPhoneNumber, chatRoomId);
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(DatabaseConstant.ROOM);
                DatabaseReference reference = databaseReference.push();
                reference.setValue(chatRoom);
            }

            databaseHelper.insertChatRoomObjectToDatabase(chatRoom);
        }

        UserChatFragment userChatFragment = new UserChatFragment();
        Bundle args = new Bundle();
        args.putString(DatabaseConstant.CHAT_ROOM_ID, chatRoomId);
        userChatFragment.setArguments(args);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.drawer_layout, userChatFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        //getRoomId(yourPhoneNumber, opponentPhoneNumber);
    }

    private String getRoomId(String yourPhoneNumber, String opponentPhoneNumber) {
        String rId = databaseHelper.getRoomId(yourPhoneNumber, opponentPhoneNumber);
        return rId;
    }


    private void getRoomIdFromDatabase() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(DatabaseConstant.ROOM);

        // Retrieve new posts as they are added to the database
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ChatRoom chatRoom = dataSnapshot.getValue(ChatRoom.class);
                databaseHelper.insertChatRoomObjectToDatabase(chatRoom);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }
}

package com.yo.android.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
    private String yourPhoneNumber;
    private String opponentPhoneNumber;

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new DatabaseHelper(getActivity().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        listView = (ListView) view.findViewById(R.id.lv_contacts);
        getMessageFromDatabase();
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

    private void getMessageFromDatabase() {
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
        yourPhoneNumber = "1234567899";
        opponentPhoneNumber = registration.getPhoneNumber();
        ChatRoom chatRoom = null;
        if(getRoomId(yourPhoneNumber, opponentPhoneNumber).equals("")) {
            if (!yourPhoneNumber.isEmpty() && !opponentPhoneNumber.isEmpty()) {
                String roomId = yourPhoneNumber + ":" + opponentPhoneNumber;
                chatRoom = new ChatRoom(yourPhoneNumber, opponentPhoneNumber, roomId);
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(DatabaseConstant.ROOM);
                DatabaseReference reference = databaseReference.push();
                reference.setValue(chatRoom);
    }

            databaseHelper.insertChatRoomObjectToDatabase(chatRoom);
        }
        getRoomId(yourPhoneNumber, opponentPhoneNumber);
    }

    private String getRoomId(String yourPhoneNumber, String opponentPhoneNumber) {
        String rId = databaseHelper.getRoomId(yourPhoneNumber, opponentPhoneNumber);
        return rId;
    }

}

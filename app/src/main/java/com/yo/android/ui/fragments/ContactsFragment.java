package com.yo.android.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yo.android.R;
import com.yo.android.adapters.ContactsListAdapter;
import com.yo.android.model.Registration;
import com.yo.android.ui.BaseActivity;
import com.yo.android.util.DatabaseConstant;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */

public class ContactsFragment extends Fragment {

    private ArrayList<Registration> arrayOfUsers;
    private ContactsListAdapter contactsListAdapter;
    private int incrementalContactsCount;

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        ListView listView = (ListView) view.findViewById(R.id.lv_contacts);
        getMessageFromDatabase();
        arrayOfUsers = new ArrayList<>();
        contactsListAdapter = new ContactsListAdapter(getActivity().getApplicationContext());
        listView.setAdapter(contactsListAdapter);
        incrementalContactsCount = 0;

        return view;
    }

    private void getMessageFromDatabase() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(DatabaseConstant.APP_USERS);

        // Retrieve new posts as they are added to the database
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ((BaseActivity) getActivity()).showProgressDialog();
                Registration registeredUsers = dataSnapshot.getValue(Registration.class);
                arrayOfUsers.add(registeredUsers);
                contactsListAdapter.addItems(arrayOfUsers);
                int contactsCount = (int) dataSnapshot.getChildrenCount();

                if (incrementalContactsCount == contactsCount) {
                    ((BaseActivity) getActivity()).dismissProgressDialog();
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
}

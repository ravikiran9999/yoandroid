package com.yo.android.chat.firebase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yo.android.adapters.AppContactsListAdapter;
import com.yo.android.model.Registration;
import com.yo.android.ui.BaseActivity;
import com.yo.android.util.Constants;

import java.util.ArrayList;

/**
 * Created by rdoddapaneni on 7/8/2016.
 */

public class RetrieveContactsManager extends BaseActivity {

    private ArrayList<Registration> arrayOfUsers;
    private AppContactsListAdapter appContactsListAdapter;

    public RetrieveContactsManager(ArrayList<Registration> arrayOfUsers, AppContactsListAdapter appContactsListAdapter) {
        this.arrayOfUsers = arrayOfUsers;
        this.appContactsListAdapter = appContactsListAdapter;
    }

    public void getRegisteredAppUsers() {

        //showProgressDialog();
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
                appContactsListAdapter.addItems(arrayOfUsers);
                //dismissProgressDialog();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dismissProgressDialog();
            }
        });
    }
}

package com.yo.android.chat.firebase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yo.android.model.Registration;
import com.yo.android.ui.BaseActivity;
import com.yo.android.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rdoddapaneni on 7/8/2016.
 */

public class RetrieveContactsManager extends BaseActivity {


    public RetrieveContactsManager() {

    }

    public List<Registration> getRegisteredAppUsers() {
        final List<Registration> arrayOfUsers = new ArrayList<>();
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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dismissProgressDialog();
            }
        });
        return arrayOfUsers;
    }
}

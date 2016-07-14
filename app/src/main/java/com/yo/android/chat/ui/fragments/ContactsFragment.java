package com.yo.android.chat.ui.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yo.android.R;
import com.yo.android.adapters.ContactsListAdapter;
import com.yo.android.helpers.DatabaseHelper;
import com.yo.android.model.Contacts;
import com.yo.android.model.PhNumberBean;
import com.yo.android.model.Registration;
import com.yo.android.ui.BottomTabsActivity;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.util.ArrayList;

import javax.inject.Inject;

/**
 * A simple {@link Fragment} subclass.
 */

public class ContactsFragment extends BaseFragment {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1001;
    private ArrayList<Registration> arrayOfUsers;
    private ContactsListAdapter contactsListAdapter;
    private ListView listView;
    private static ArrayList<Contacts> nc = new ArrayList<>();
    private Registration registeredUsers;
    private DatabaseReference reference;

    @Inject
    DatabaseHelper databaseHelper;
    private Menu menu;

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        reference = FirebaseDatabase.getInstance().getReference(Constants.APP_USERS);
        reference.keepSynced(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        listView = (ListView) view.findViewById(R.id.lv_contacts);
        //ArrayList<Contacts> contact = readContacts();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getRegisteredAppUsers();
        arrayOfUsers = new ArrayList<>();
        contactsListAdapter = new ContactsListAdapter(getActivity().getApplicationContext(), preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER));
        listView.setAdapter(contactsListAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_contacts, menu);
        this.menu = menu;
        if(getActivity() instanceof BottomTabsActivity) {
            ((BottomTabsActivity)getActivity()).setToolBarColor(getResources().getColor(R.color.colorPrimary));
            Util.changeMenuItemsVisibility(menu, -1, true);
        }
        Util.changeSearchProperties(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    private void getRegisteredAppUsers() {
        showProgressDialog();

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                arrayOfUsers.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    registeredUsers = child.getValue(Registration.class);
                    if(!(registeredUsers.getPhoneNumber().equals(preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER)))) {
                        arrayOfUsers.add(registeredUsers);
                    }
                }
                contactsListAdapter.addItems(arrayOfUsers);
                dismissProgressDialog();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dismissProgressDialog();
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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

    private ArrayList<Contacts> readContacts() {
        try {
            Cursor contactsCursor = getActivity().getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

            while (contactsCursor.moveToNext()) {

                Contacts contactBean = new Contacts();
                contactBean.setId(contactsCursor.getLong(contactsCursor.getColumnIndex(ContactsContract.Contacts._ID)));

                String contactId = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts._ID));

                contactBean.setmFirstName(contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));

                if (Integer.parseInt(contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    ArrayList<PhNumberBean> nc1 = new ArrayList<>();
                    Cursor phoneNumberCursor = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{contactId}, null);

                    PhNumberBean phNumberBean = new PhNumberBean();
                    while (phoneNumberCursor.moveToNext()) {
                        String phoneNumber = phoneNumberCursor.getString(phoneNumberCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        phNumberBean.setmPhNum(phoneNumber);


                    }
                    nc1.add(phNumberBean);
                    contactBean.setmCotactNumber(nc1);
                    phoneNumberCursor.close();
                }
                nc.add(contactBean);
            }
            contactsCursor.close();
        }catch (NullPointerException e) {
            e.printStackTrace();
        }
        return nc;
    }

    public Menu getMenu() {
        return menu;
    }
}

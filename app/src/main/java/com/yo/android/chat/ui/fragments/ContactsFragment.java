package com.yo.android.chat.ui.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.yo.android.R;
import com.yo.android.adapters.ContactsListAdapter;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.model.Contact;
import com.yo.android.ui.UserProfileActivity;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */

public class ContactsFragment extends BaseFragment implements AdapterView.OnItemClickListener {
    private ContactsListAdapter contactsListAdapter;
    private ListView listView;

    private Menu menu;
    @Inject
    ContactsSyncManager mSyncManager;
    private static final int PICK_CONTACT_REQUEST = 100;

    public ContactsFragment() {
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
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        listView = (ListView) view.findViewById(R.id.lv_contacts);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        contactsListAdapter = new ContactsListAdapter(getActivity().getApplicationContext(), preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER));
        listView.setAdapter(contactsListAdapter);
        listView.setOnItemClickListener(this);
        if (!mSyncManager.getContacts().isEmpty()) {
            contactsListAdapter.addItems(mSyncManager.getContacts());
        }
        if (mSyncManager.getContacts().isEmpty()) {
            showProgressDialog();
        }
        mSyncManager.loadContacts(new Callback<List<Contact>>() {
            @Override
            public void onResponse(Call<List<Contact>> call, Response<List<Contact>> response) {
                contactsListAdapter.addItems(mSyncManager.getContacts());
                dismissProgressDialog();
            }

            @Override
            public void onFailure(Call<List<Contact>> call, Throwable t) {
                dismissProgressDialog();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_contacts, menu);
        this.menu = menu;
        Util.prepareContactsSearch(getActivity(), menu, contactsListAdapter, Constants.CONT_FRAG);
        Util.changeSearchProperties(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.invite) {
            Intent i = new Intent(Intent.ACTION_INSERT);
            i.setType(ContactsContract.Contacts.CONTENT_TYPE);
            if (Integer.valueOf(Build.VERSION.SDK) > 14)
                i.putExtra("finishActivityOnSaveCompleted", true); // Fix for 4.0.3 +
            startActivityForResult(i, PICK_CONTACT_REQUEST);
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    public Menu getMenu() {
        return menu;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Contact contact = (Contact) listView.getItemAtPosition(position);
        if (contact.getYoAppUser()) {
            Intent intent = new Intent(getActivity(), UserProfileActivity.class);
            intent.putExtra(Constants.CONTACT, contact);
//            intent.putExtra(Constants.OPPONENT_PHONE_NUMBER, contact.getPhoneNo());
//            intent.putExtra(Constants.OPPONENT_CONTACT_IMAGE,contact.getImage());
//            intent.putExtra(Constants.IS_OPPONENT_YO_USER,contact.getYoAppUser());
            startActivity(intent);
        }

    }
}

package com.yo.android.chat.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yo.android.R;
import com.yo.android.adapters.ContactsListAdapter;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.model.Contact;
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

public class ContactsFragment extends BaseFragment {
    private ContactsListAdapter contactsListAdapter;
    private ListView listView;

    private DatabaseReference reference;
    private Menu menu;
    @Inject
    ContactsSyncManager mSyncManager;

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
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        contactsListAdapter = new ContactsListAdapter(getActivity().getApplicationContext(), preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER));
        listView.setAdapter(contactsListAdapter);

        if (mSyncManager.getContacts().isEmpty()) {
            showProgressDialog();
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
        } else {
            contactsListAdapter.addItems(mSyncManager.getContacts());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_contacts, menu);
        this.menu = menu;
        Util.prepareContactsSearch(getActivity(), menu,contactsListAdapter, Constants.CONT_FRAG);
        Util.changeSearchProperties(menu);
        super.onCreateOptionsMenu(menu, inflater);
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

}

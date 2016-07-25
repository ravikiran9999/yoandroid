package com.yo.android.chat.ui.fragments;


import android.os.Bundle;
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
import android.widget.Toast;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.adapters.GroupContactsListAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.model.Contact;
import com.yo.android.model.Room;
import com.yo.android.model.Registration;
import com.yo.android.util.Constants;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupContactsFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    private ArrayList<Registration> arrayOfUsers;
    private GroupContactsListAdapter groupContactsListAdapter;
    private ListView listView;
    private String groupName;

    @Inject
    YoApi.YoService yoService;
    @Inject
    ContactsSyncManager mContactsSyncManager;

    @Inject
    @Named("login")
    PreferenceEndPoint loginPrefs;

    public GroupContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        groupName = getArguments().getString(Constants.GROUP_NAME);
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
        arrayOfUsers = new ArrayList<>();
        groupContactsListAdapter = new GroupContactsListAdapter(getActivity().getApplicationContext());
        listView.setAdapter(groupContactsListAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_multiple_contacts, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.done) {
            createGroup();
        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        /*Registration registration = (Registration) listView.getItemAtPosition(position);
        String yourPhoneNumber = preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER);
        String opponentPhoneNumber = registration.getPhoneNumber();
        showUserChatScreen(yourPhoneNumber, opponentPhoneNumber);*/

        Toast.makeText(getActivity(), "Need to show App contacts", Toast.LENGTH_SHORT).show();
    }


    private void getYoAppUsers() {
        showProgressDialog();

        mContactsSyncManager.loadContacts(new Callback<List<Contact>>() {
            @Override
            public void onResponse(Call<List<Contact>> call, Response<List<Contact>> response) {
                List<Contact> contactList = new ArrayList<>();
                if (response.body() != null) {
                    for(int i=0; i<response.body().size();i++){
                        if(response.body().get(i).getYoAppUser()) {
                            contactList.add(response.body().get(i));
                        }
                    }
                    groupContactsListAdapter.addItems(contactList);
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

    private void createGroup() {
        showProgressDialog();
        ArrayList<Contact> contactArrayList = groupContactsListAdapter.getmSelectedItems();

        List<String> selectedUsers = new ArrayList<>();
        for (int i = 0; i < contactArrayList.size(); i++) {
            String userId = contactArrayList.get(i).getId();
            selectedUsers.add(userId);
        }

        String access = loginPrefs.getStringPreference(YoApi.ACCESS_TOKEN);
        yoService.createGroupAPI(access, selectedUsers, groupName).enqueue(new Callback<Room>() {
            @Override
            public void onResponse(Call<Room> call, Response<Room> response) {
                response.body();
                dismissProgressDialog();
                getActivity().finish();
            }

            @Override
            public void onFailure(Call<Room> call, Throwable t) {
                dismissProgressDialog();
            }
        });
    }
}

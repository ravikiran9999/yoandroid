package com.yo.android.ui.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.InviteFriendsAdapter;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.helpers.Helper;
import com.yo.android.model.Contact;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;


import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class InviteFriendsFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    @BindView(R.id.invite_list)
    ListView lv_invite;
    private InviteFriendsAdapter inviteFriendsAdapter;
    @BindView(R.id.side_index)
    ListView layout;
    @BindView(R.id.txtEmpty)
    TextView txtEmpty;
    @BindView(R.id.no_search_results)
    TextView noSearchResultsTxtVw;

    private Menu menu;
    private static final int PICK_CONTACT_REQUEST = 100;


    @Inject
    ContactsSyncManager mSyncManager;

    public InviteFriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invite_friends, container, false);
        ButterKnife.bind(this, view);
        setHasOptionsMenu(true);
        return view;
    }

    private void loadInAlphabeticalOrder(List<Contact> contactList) {
        Collections.sort(contactList, new Comparator<Contact>() {
            @Override
            public int compare(Contact lhs, Contact rhs) {
                return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
            }
        });
        Helper.displayIndex(getActivity(), layout, contactList, lv_invite);
        inviteFriendsAdapter.addItems(contactList);
        inviteFriendsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        inviteFriendsAdapter = new InviteFriendsAdapter(getActivity().getApplicationContext());
        lv_invite.setAdapter(inviteFriendsAdapter);

        if (mSyncManager.getContacts().isEmpty()) {
            showProgressDialog();
            mSyncManager.loadContacts(new Callback<List<Contact>>() {
                @Override
                public void onResponse(Call<List<Contact>> call, Response<List<Contact>> response) {
                    List<Contact> nonYoAppContacts = new ArrayList<Contact>();
                    List<Contact> contacts = mSyncManager.getContacts();

                    for (Contact con : contacts) {
                        if (!con.isYoAppUser()) {
                            nonYoAppContacts.add(con);
                        }
                    }
                    if (nonYoAppContacts.isEmpty()) {
                        lv_invite.setVisibility(View.GONE);
                        layout.setVisibility(View.GONE);
                        txtEmpty.setVisibility(View.VISIBLE);
                    } else {
                        txtEmpty.setVisibility(View.GONE);
                        layout.setVisibility(View.VISIBLE);
                        lv_invite.setVisibility(View.VISIBLE);
                        loadInAlphabeticalOrder(nonYoAppContacts);
                    }

                    dismissProgressDialog();
                }

                @Override
                public void onFailure(Call<List<Contact>> call, Throwable t) {
                    noSearchResultsTxtVw.setVisibility(View.VISIBLE);
                    FragmentActivity activity = getActivity();
                    if (activity != null) {
                        noSearchResultsTxtVw.setText(activity.getResources().getString(R.string.connectivity_network_settings));
                    }
                    dismissProgressDialog();
                }
            });
        } else {
            List<Contact> nonYoAppContacts = new ArrayList<Contact>();
            List<Contact> contacts = mSyncManager.getContacts();
            for (Contact con : contacts) {
                if (!con.isYoAppUser()) {
                    nonYoAppContacts.add(con);
                }
            }
            loadInAlphabeticalOrder(nonYoAppContacts);
        }
        lv_invite.setOnItemClickListener(this);
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        InviteFriendsAdapter adapter = (InviteFriendsAdapter) parent.getAdapter();
        Contact contact = adapter.getItem(position);
        Util.inviteFriend(getActivity(), contact.getPhoneNo());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_invite_friends, menu);
        this.menu = menu;
        Util.changeSearchProperties(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Util.prepareContactsSearch(getActivity(), menu, inviteFriendsAdapter , Constants.CONT_FRAG,  noSearchResultsTxtVw, null);
        return super.onOptionsItemSelected(item);
    }

}
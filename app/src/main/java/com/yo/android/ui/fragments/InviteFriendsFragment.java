package com.yo.android.ui.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.yo.android.R;
import com.yo.android.adapters.InviteFriendsAdapter;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.model.Contact;
import com.yo.android.util.Util;

import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class InviteFriendsFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    private ListView lv_invite;
    private InviteFriendsAdapter inviteFriendsAdapter;

    @Inject
    ContactsSyncManager mSyncManager;

    public InviteFriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invite_friends, container, false);
        lv_invite = (ListView) view.findViewById(R.id.invite_list);
        return view;
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
                    inviteFriendsAdapter.addItems(mSyncManager.getContacts());
                    dismissProgressDialog();
                }

                @Override
                public void onFailure(Call<List<Contact>> call, Throwable t) {
                    dismissProgressDialog();
                }
            });
        } else {
            inviteFriendsAdapter.addItems(mSyncManager.getContacts());
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
}

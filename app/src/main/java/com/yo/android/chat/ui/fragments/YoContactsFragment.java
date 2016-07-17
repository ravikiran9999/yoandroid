package com.yo.android.chat.ui.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yo.android.R;
import com.yo.android.adapters.AppContactsListAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.model.Registration;
import com.yo.android.model.YoAppContacts;
import com.yo.android.util.Constants;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class YoContactsFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    private ArrayList<Registration> arrayOfUsers;
    private AppContactsListAdapter appContactsListAdapter;
    private ListView listView;

    @Inject
    YoApi.YoService yoService;

    public YoContactsFragment() {
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
        appContactsListAdapter = new AppContactsListAdapter(getActivity().getApplicationContext());
        listView.setAdapter(appContactsListAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_app_contacts, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        /*Registration registration = (Registration) listView.getItemAtPosition(position);
        String yourPhoneNumber = preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER);
        String opponentPhoneNumber = registration.getPhoneNumber();
        showUserChatScreen(yourPhoneNumber, opponentPhoneNumber);*/

        Toast.makeText(getActivity(), "Need to show App contacts", Toast.LENGTH_SHORT).show();
    }

    private void showUserChatScreen(@NonNull final String yourPhoneNumber, @NonNull final String opponentPhoneNumber) {
        final String roomCombination1 = yourPhoneNumber + ":" + opponentPhoneNumber;
        final String roomCombination2 = opponentPhoneNumber + ":" + yourPhoneNumber;
        DatabaseReference databaseRoomReference = FirebaseDatabase.getInstance().getReference(Constants.ROOM);
        databaseRoomReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean value1 = dataSnapshot.hasChild(roomCombination1);
                boolean value2 = dataSnapshot.hasChild(roomCombination2);
                if (value1) {
                    navigateToChatScreen(roomCombination1, opponentPhoneNumber, yourPhoneNumber);
                } else if (value2) {
                    navigateToChatScreen(roomCombination2, opponentPhoneNumber, yourPhoneNumber);
                } else {

                    navigateToChatScreen("", opponentPhoneNumber, yourPhoneNumber);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void navigateToChatScreen(String roomId, String opponentPhoneNumber, String yourPhoneNumber) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(Constants.CHAT_ROOM_ID, roomId);
        intent.putExtra(Constants.OPPONENT_PHONE_NUMBER, opponentPhoneNumber);
        intent.putExtra(Constants.YOUR_PHONE_NUMBER, yourPhoneNumber);
        startActivity(intent);
        getActivity().finish();
    }


    private void getYoAppUsers() {
        showProgressDialog();

        yoService.getYoAppContactsAPI(1, 20).enqueue(new Callback<List<YoAppContacts>>() {
            @Override
            public void onResponse(Call<List<YoAppContacts>> call, Response<List<YoAppContacts>> response) {
                if (response.body() != null) {
                    appContactsListAdapter.addItems(response.body());
                }
                dismissProgressDialog();
            }

            @Override
            public void onFailure(Call<List<YoAppContacts>> call, Throwable t) {
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
}

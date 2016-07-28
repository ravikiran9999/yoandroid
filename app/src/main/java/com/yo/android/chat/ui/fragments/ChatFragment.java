package com.yo.android.chat.ui.fragments;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.adapters.ChatRoomListAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.chat.ui.CreateGroupActivity;
import com.yo.android.helpers.DatabaseHelper;
import com.yo.android.model.ChatMessage;
import com.yo.android.model.Room;
import com.yo.android.util.Constants;
import com.yo.android.util.FireBaseHelper;
import com.yo.android.util.Util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    private ListView listView;
    private List<Room> arrayOfUsers;
    private ChatRoomListAdapter chatRoomListAdapter;
    private Menu menu;

    @Inject
    YoApi.YoService yoService;

    @Inject
    FireBaseHelper fireBaseHelper;

    @Inject
    @Named("login")
    PreferenceEndPoint loginPrefs;

    public Menu getMenu() {
        return menu;
    }


    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_chat, menu);
        this.menu = menu;
        Util.prepareContactsSearch(getActivity(), menu,chatRoomListAdapter);
        Util.changeSearchProperties(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.chat_contact:
                startActivity(new Intent(getActivity(), AppContactsActivity.class));
                break;
            case R.id.create_group:
                startActivity(new Intent(getActivity(), CreateGroupActivity.class));
                break;

            default:
                break;

        }
        return super.onOptionsItemSelected(item);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        listView = (ListView) view.findViewById(R.id.lv_chat_room);
        listView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getChatRoomList();
        arrayOfUsers = new ArrayList<>();
        chatRoomListAdapter = new ChatRoomListAdapter(getActivity().getApplicationContext());
        listView.setAdapter(chatRoomListAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Room room = chatRoomListAdapter.getItem(position);
        navigateToChatScreen(room);
    }

    private void navigateToChatScreen(Room room) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(Constants.ROOM, room);
        intent.putExtra(Constants.TYPE, Constants.ROOM);
        startActivity(intent);
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

    private void getChatRoomList() {
        showProgressDialog();
        String access = loginPrefs.getStringPreference(YoApi.ACCESS_TOKEN);
        String userId = loginPrefs.getStringPreference(Constants.USER_ID);
        ArrayList<String> stringArrayList = new ArrayList<>();
        stringArrayList.add(userId);
        yoService.getAllRoomsAPI(access).enqueue(new Callback<List<Room>>() {
            @Override
            public void onResponse(Call<List<Room>> call, Response<List<Room>> response) {
                response.body();
                arrayOfUsers = response.body();
                chatRoomListAdapter.addItems(arrayOfUsers);
                Firebase firebaseReference = fireBaseHelper.authWithCustomToken(preferenceEndPoint.getStringPreference(Constants.FIREBASE_TOKEN));
                for (int i = 0; i < arrayOfUsers.size(); i++) {
                    final Room room = arrayOfUsers.get(i);
                    Firebase firebaseRoomReference = firebaseReference.child(room.getFirebaseRoomId()).child(Constants.CHATS);
                    firebaseRoomReference.limitToLast(1).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);

                            if (dataSnapshot.hasChildren()) {
                                room.setLastChat(chatMessage.getMessage());
                                if (!TextUtils.isEmpty(chatMessage.getType()) && chatMessage.getType().equals(Constants.IMAGE)) {
                                    room.setImage(true);
                                } else {
                                    room.setImage(false);
                                }
                                room.setTimeStamp(Util.getChatListTimeFormat(getContext(), chatMessage.getTime()));
                            }
                            chatRoomListAdapter.addItems(arrayOfUsers);
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {
                            ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                            if (dataSnapshot.hasChildren()) {
                                room.setLastChat("");
                                room.setImage(false);
                                room.setTimeStamp(Util.getChatListTimeFormat(getContext(), chatMessage.getTime()));
                            }
                            if (chatRoomListAdapter != null) {
                                chatRoomListAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });
                }

                dismissProgressDialog();
            }

            @Override
            public void onFailure(Call<List<Room>> call, Throwable t) {
                dismissProgressDialog();
            }
        });
    }


}

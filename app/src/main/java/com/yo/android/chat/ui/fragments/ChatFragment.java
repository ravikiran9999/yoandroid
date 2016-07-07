package com.yo.android.chat.ui.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yo.android.R;
import com.yo.android.adapters.ChatRoomListAdapter;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.helpers.DatabaseHelper;
import com.yo.android.model.ChatRoom;
import com.yo.android.util.Constants;

import java.util.ArrayList;

import javax.inject.Inject;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    private ListView listView;
    private ArrayList<ChatRoom> arrayOfUsers;
    private ChatRoomListAdapter chatRoomListAdapter;

    @Inject
    DatabaseHelper databaseHelper;


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
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.chat_contact:
                startActivity(new Intent(getActivity(), AppContactsActivity.class));
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
        ChatRoom chatRoom = (ChatRoom) listView.getItemAtPosition(position);
        navigateToChatScreen(chatRoom.getChatRoomId(), chatRoom.getOpponentPhoneNumber());
    }

    private void navigateToChatScreen(String roomId, String opponentPhoneNumber) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(Constants.CHAT_ROOM_ID, roomId);
        intent.putExtra(Constants.OPPONENT_PHONE_NUMBER, opponentPhoneNumber);
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
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constants.ROOM);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                arrayOfUsers.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    ChatRoom chatRoom = child.getValue(ChatRoom.class);
                    arrayOfUsers.add(chatRoom);
                }
                chatRoomListAdapter.addItems(arrayOfUsers);
                dismissProgressDialog();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dismissProgressDialog();
            }
        });
    }

}

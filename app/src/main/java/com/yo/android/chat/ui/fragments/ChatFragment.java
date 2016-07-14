package com.yo.android.chat.ui.fragments;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.yo.android.R;
import com.yo.android.adapters.ChatRoomListAdapter;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.chat.ui.CreateGroupActivity;
import com.yo.android.helpers.DatabaseHelper;
import com.yo.android.model.ChatMessage;
import com.yo.android.model.ChatRoom;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.inject.Inject;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    private ListView listView;
    private ArrayList<ChatRoom> arrayOfUsers;
    private ChatRoomListAdapter chatRoomListAdapter;
    private ChatMessage forwardChatMessage;
    DatabaseReference reference;
    DatabaseReference roomReference;
    private Menu menu;
    public Menu getMenu() {
        return menu;
    }

    @Inject
    DatabaseHelper databaseHelper;


    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {

            preferenceEndPoint.saveStringPreference(Constants.CHAT_FORWARD, new Gson().toJson(getArguments().getParcelable(Constants.CHAT_FORWARD)));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_chat, menu);
        this.menu = menu;
        SearchView search = (SearchView) menu.findItem(R.id.menu_search).getActionView();

        AutoCompleteTextView searchTextView = (AutoCompleteTextView) search.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        try {
            searchTextView.setTextColor(Color.BLACK);
            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(searchTextView,R.drawable.red_cursor); //This sets the cursor resource ID to 0 or @null which will make it visible on white background
        } catch (Exception e) {
        }
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
        reference = FirebaseDatabase.getInstance().getReference(Constants.ROOM);
        reference.keepSynced(true);
        roomReference = FirebaseDatabase.getInstance().getReference(Constants.ROOM_ID);

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
        ChatRoom chatRoom = chatRoomListAdapter.getItem(position);

        String chatForwardObjectString = preferenceEndPoint.getStringPreference(Constants.CHAT_FORWARD);
        forwardChatMessage = new Gson().fromJson(chatForwardObjectString, ChatMessage.class);

        if (forwardChatMessage != null) {
            navigateToChatScreen(chatRoom.getChatRoomId(), chatRoom.getOpponentPhoneNumber(), forwardChatMessage);
        } else {

            if (!chatRoom.getOpponentPhoneNumber().equals(preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER))) {
                navigateToChatScreen(chatRoom.getChatRoomId(), chatRoom.getOpponentPhoneNumber());
            } else {
                navigateToChatScreen(chatRoom.getChatRoomId(), chatRoom.getYourPhoneNumber());
            }

        }
    }

    private void navigateToChatScreen(String roomId, String opponentPhoneNumber) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(Constants.CHAT_ROOM_ID, roomId);
        intent.putExtra(Constants.OPPONENT_PHONE_NUMBER, opponentPhoneNumber);
        startActivity(intent);
    }

    private void navigateToChatScreen(String roomId, String opponentPhoneNumber, ChatMessage forward) {
        if (preferenceEndPoint.getStringPreference(Constants.CHAT_FORWARD) != null) {
            preferenceEndPoint.removePreference(Constants.CHAT_FORWARD);
        }

        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(Constants.CHAT_ROOM_ID, roomId);
        intent.putExtra(Constants.OPPONENT_PHONE_NUMBER, opponentPhoneNumber);
        intent.putExtra(Constants.CHAT_FORWARD, forward);
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
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                arrayOfUsers.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    final ChatRoom chatRoom = child.getValue(ChatRoom.class);
                    if (chatRoom.getYourPhoneNumber().equals(preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER)) || chatRoom.getOpponentPhoneNumber().equals(preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER))) {

                        roomReference.child(chatRoom.getChatRoomId());
                        roomReference.keepSynced(true);
                        arrayOfUsers.add(chatRoom);
                        roomReference.addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);

                                if (dataSnapshot.hasChildren()) {
                                    chatRoom.setMessage(chatMessage.getMessage());
                                    if (!TextUtils.isEmpty(chatMessage.getType()) && chatMessage.getType().equals(Constants.IMAGE)) {
                                        chatRoom.setIsImage(true);
                                    } else {
                                        chatRoom.setIsImage(false);
                                    }
                                    chatRoom.setTimeStamp(Util.getChatListTimeFormat(getContext(), chatMessage.getTime()));

                                }
                                chatRoomListAdapter.addItems(arrayOfUsers);

                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {

                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
//                        roomReference.addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(DataSnapshot dataSnapshot) {
//                                ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
//
//                                if (dataSnapshot.hasChildren()) {
//                                    chatRoom.setMessage(chatMessage.getMessage());
//                                    if (!TextUtils.isEmpty(chatMessage.getType()) && chatMessage.getType().equals(Constants.IMAGE)) {
//                                        chatRoom.setIsImage(true);
//                                    } else {
//                                        chatRoom.setIsImage(false);
//                                    }
//                                    chatRoom.setTimeStamp(Util.getChatListTimeFormat(getContext(), chatMessage.getTime()));
//                                    arrayOfUsers.add(chatRoom);
//                                }
//                                chatRoomListAdapter.addItems(arrayOfUsers);
//                            }
//
//                            @Override
//                            public void onCancelled(DatabaseError databaseError) {
//
//                            }
//                        });


                    }
                }

                dismissProgressDialog();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dismissProgressDialog();
            }
        });
    }


}

package com.yo.android.chat.ui.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

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
import com.yo.android.chat.firebase.FirebaseService;
import com.yo.android.chat.firebase.MyServiceConnection;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.chat.ui.CreateGroupActivity;
import com.yo.android.helpers.DatabaseHelper;
import com.yo.android.model.ChatMessage;
import com.yo.android.model.ChatRoom;
import com.yo.android.model.Room;
import com.yo.android.util.Constants;
import com.yo.android.util.FireBaseHelper;
import com.yo.android.util.Util;

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
public class ChatFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    private ListView listView;
    private ArrayList<Room> arrayOfUsers;
    private ChatRoomListAdapter chatRoomListAdapter;
    private DatabaseReference reference;
    private DatabaseReference roomReference;
    private Menu menu;


    @Inject
    DatabaseHelper databaseHelper;

    @Inject
    YoApi.YoService yoService;

    @Inject
    @Named("login")
    PreferenceEndPoint loginPrefs;

    @Inject
    FireBaseHelper fireBaseHelper;

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
        if (getArguments() != null) {
            preferenceEndPoint.saveStringPreference(Constants.CHAT_FORWARD, new Gson().toJson(getArguments().getParcelable(Constants.CHAT_FORWARD)));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_chat, menu);
        this.menu = menu;
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
        Room room = chatRoomListAdapter.getItem(position);

        String chatForwardObjectString = preferenceEndPoint.getStringPreference(Constants.CHAT_FORWARD);
        ChatMessage forwardChatMessage = new Gson().fromJson(chatForwardObjectString, ChatMessage.class);

        if (forwardChatMessage != null) {
            if (room.getGroupName() == null) {
                navigateToChatScreen(room.getFirebaseRoomId(), room.getMembers().get(0).getMobileNumber(), forwardChatMessage);
            } else if (room.getGroupName() != null) {
                navigateToChatScreen(room.getFirebaseRoomId(), room.getGroupName(), forwardChatMessage);
            }

        } else if (room.getGroupName() == null) {
            navigateToChatScreen(room.getFirebaseRoomId(), room.getMembers().get(0).getMobileNumber());
        } else if (room.getGroupName() != null) {
            navigateToChatScreen(room.getFirebaseRoomId(), room.getGroupName());
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

    /*private void getChatRoomList() {
        showProgressDialog();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                arrayOfUsers.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    final ChatRoom chatRoom = child.getValue(ChatRoom.class);
                    if (chatRoom.getYourPhoneNumber().equals(preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER)) || chatRoom.getOpponentPhoneNumber().equals(preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER))) {
                        arrayOfUsers.add(chatRoom);

                        roomReference.keepSynced(true);
                        DatabaseReference reference = roomReference.child(chatRoom.getChatRoomId());
                        reference.limitToLast(1).addChildEventListener(new ChildEventListener() {
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
                                ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                                if (dataSnapshot.hasChildren()) {
                                    chatRoom.setMessage("");
                                    chatRoom.setIsImage(false);
                                    chatRoom.setTimeStamp(Util.getChatListTimeFormat(getContext(), chatMessage.getTime()));
                                }
                                if (chatRoomListAdapter != null) {
                                    chatRoomListAdapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }

                dismissProgressDialog();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dismissProgressDialog();
            }
        });
    }*/

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

                chatRoomListAdapter.addItems(response.body());

                Firebase firebaseRoomReference = fireBaseHelper.authWithCustomToken(preferenceEndPoint.getStringPreference(Constants.FIREBASE_TOKEN));
                for(int i =0; i < response.body().size(); i++) {

                    firebaseRoomReference.child(response.body().get(i).getFirebaseRoomId()).child(Constants.CHATS);
                    firebaseRoomReference.limitToLast(1).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);

                            /*if (dataSnapshot.hasChildren()) {
                                room.setMessage(chatMessage.getMessage());
                                if (!TextUtils.isEmpty(chatMessage.getType()) && chatMessage.getType().equals(Constants.IMAGE)) {
                                    room.setIsImage(true);
                                } else {
                                    room.setIsImage(false);
                                }
                                room.setTimeStamp(Util.getChatListTimeFormat(getContext(), chatMessage.getTime()));

                            }*/
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

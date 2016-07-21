package com.yo.android.chat.ui.fragments;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
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
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
import com.yo.android.model.GroupName;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.util.ArrayList;

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
    private ArrayList<ChatRoom> arrayOfUsers;
    private ChatRoomListAdapter chatRoomListAdapter;
    private DatabaseReference reference;
    private DatabaseReference roomReference;
    private Menu menu;

    @Inject
    YoApi.YoService yoService;
    @Inject
    FirebaseService firebaseService;

    @Inject
    DatabaseHelper databaseHelper;
    @Inject
    MyServiceConnection myServiceConnection;

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

        if(myServiceConnection.isServiceConnection()) {
            firebaseService.getFirebaseAuth();
        }

        getChatRoomList();
        arrayOfUsers = new ArrayList<>();
        chatRoomListAdapter = new ChatRoomListAdapter(getActivity().getApplicationContext());
        listView.setAdapter(chatRoomListAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ChatRoom chatRoom = chatRoomListAdapter.getItem(position);

        String chatForwardObjectString = preferenceEndPoint.getStringPreference(Constants.CHAT_FORWARD);
        ChatMessage forwardChatMessage = new Gson().fromJson(chatForwardObjectString, ChatMessage.class);

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

        /*String access = loginPrefs.getStringPreference(YoApi.ACCESS_TOKEN);
        yoService.createGroupAPI(access, selectedUsers).enqueue(new Callback<GroupName>() {
            @Override
            public void onResponse(Call<GroupName> call, Response<GroupName> response) {
                response.body();
                Toast.makeText(getActivity(), "Created Room: "+ groupName, Toast.LENGTH_SHORT).show();
                dismissProgressDialog();
                getActivity().finish();
            }

            @Override
            public void onFailure(Call<GroupName> call, Throwable t) {
                dismissProgressDialog();
            }
        });*/
    }

}

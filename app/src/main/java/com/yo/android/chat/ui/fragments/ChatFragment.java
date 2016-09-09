package com.yo.android.chat.ui.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.adapters.ChatRoomListAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.chat.ui.CreateGroupActivity;
import com.yo.android.model.ChatMessage;
import com.yo.android.model.Contact;
import com.yo.android.model.Room;
import com.yo.android.model.RoomInfo;
import com.yo.android.util.Constants;
import com.yo.android.util.FireBaseHelper;
import com.yo.android.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import de.greenrobot.event.EventBus;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    private static final String TAG = "ChatFragment";

    private ListView listView;
    private ImageView emptyImageView;
    private List<ChildEventListener> childEventListenersList;
    private List<Room> arrayOfUsers;
    private ChatRoomListAdapter chatRoomListAdapter;
    private Menu menu;
    private Room room;
    private String voxUserName;

    @Inject
    YoApi.YoService yoService;

    @Inject
    FireBaseHelper fireBaseHelper;

    @Inject
    @Named("login")
    PreferenceEndPoint loginPrefs;

    @Inject
    ContactsSyncManager mContactsSyncManager;

    public ChatFragment() {
        // Required empty public constructor
    }

    public Menu getMenu() {
        return menu;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        childEventListenersList = new ArrayList<>();
        arrayOfUsers = new ArrayList<>();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        listView = (ListView) view.findViewById(R.id.lv_chat_room);
        emptyImageView = (ImageView) view.findViewById(R.id.empty_chat);
        listView.setOnItemClickListener(this);

        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_chat, menu);
        this.menu = menu;
        Util.prepareContactsSearch(getActivity(), menu, chatRoomListAdapter, Constants.CHAT_FRAG);
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
            case R.id.clear_chat_history:
                Toast.makeText(getActivity(), "Clear chat history not yet implemented", Toast.LENGTH_SHORT).show();
                break;

            default:
                break;

        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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

    @Override
    public void onResume() {
        super.onResume();
        isRoomsExist();
        //getAllRooms();

    }

    private void isRoomsExist() {
        showProgressDialog();
        Firebase authReference = fireBaseHelper.authWithCustomToken(loginPrefs.getStringPreference(Constants.FIREBASE_TOKEN));
        String firebaseUserId = loginPrefs.getStringPreference(Constants.FIREBASE_USER_ID);
        if (!firebaseUserId.isEmpty()) {
            authReference.child(Constants.USERS).child(firebaseUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(Constants.MY_ROOMS).exists()) {
                        getAllRooms();
                    } else {
                        emptyImageView.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                    }
                    dismissProgressDialog();
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    dismissProgressDialog();
                    firebaseError.getMessage();
                    Toast.makeText(getActivity(), "Please try again", Toast.LENGTH_SHORT).show();
                }
            });
            authReference.keepSynced(true);
        }
    }

    private void getAllRooms() {

        Firebase authReference = fireBaseHelper.authWithCustomToken(loginPrefs.getStringPreference(Constants.FIREBASE_TOKEN));

        ChildEventListener mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                getMembersId(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //getChatMessageList(dataSnapshot.getKey());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                firebaseError.getMessage();
            }

        };
        String firebaseUserId = loginPrefs.getStringPreference(Constants.FIREBASE_USER_ID);
        if (!firebaseUserId.isEmpty()) {
            authReference.child(Constants.USERS).child(firebaseUserId).child(Constants.MY_ROOMS).addChildEventListener(mChildEventListener);
            authReference.keepSynced(true);
        }
    }

    @NonNull
    private ChildEventListener createChildEventListener(final Room room) {

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try {
                    ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);

                    if (dataSnapshot.hasChildren()) {
                        room.setLastChat(chatMessage.getMessage());
                        if (!TextUtils.isEmpty(chatMessage.getType()) && chatMessage.getType().equals(Constants.IMAGE)) {
                            room.setImages(true);
                        } else {
                            room.setImages(false);
                        }
                        room.setTime(chatMessage.getTime());
                        room.setTimeStamp(Util.getChatListTimeFormat(getContext(), chatMessage.getTime()));
                    }
                    Collections.sort(arrayOfUsers, new Comparator<Room>() {
                        @Override
                        public int compare(Room lhs, Room rhs) {
                            return (int) (rhs.getTime() - lhs.getTime());
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                try {

                    ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                    if (dataSnapshot.hasChildren()) {
                        room.setLastChat("");
                        room.setImages(false);
                        room.setTimeStamp(Util.getChatListTimeFormat(getContext(), chatMessage.getTime()));
                    }
                    if (chatRoomListAdapter != null) {
                        chatRoomListAdapter.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                firebaseError.getMessage();
            }
        };

        chatRoomListAdapter.addItems(arrayOfUsers);
        return childEventListener;
    }

    private void unregisterAllEventListeners() {
        for (ChildEventListener childEventListener : childEventListenersList) {
            //
        }
    }

    public void onEventMainThread(String action) {
        if (Constants.CHAT_ROOM_REFRESH.equals(action)) {
//            getChatRoomList();
        }

    }

    private void getMembersId(DataSnapshot dataSnapshot) {
        if (!arrayOfUsers.isEmpty()) {
            arrayOfUsers.clear();
        }
        Firebase memberReference = dataSnapshot.getRef().getRoot().child(Constants.ROOMS).child(dataSnapshot.getKey());
        memberReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getMembersProfile(dataSnapshot);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                firebaseError.getMessage();
            }
        });
    }

    private void getMembersProfile(final DataSnapshot dataSnapshot) {

        final Firebase authReference = fireBaseHelper.authWithCustomToken(loginPrefs.getStringPreference(Constants.FIREBASE_TOKEN));
        final String firebaseUserId = loginPrefs.getStringPreference(Constants.FIREBASE_USER_ID);
        if (dataSnapshot.hasChild(Constants.ROOM_INFO)) {
            RoomInfo roomInfo = dataSnapshot.child(Constants.ROOM_INFO).getValue(RoomInfo.class);

            if (roomInfo.getName().isEmpty()) {
                for (DataSnapshot snapshot : dataSnapshot.child(Constants.MEMBERS).getChildren()) {
                    if (!firebaseUserId.equalsIgnoreCase(snapshot.getKey())) {
                        authReference.child(Constants.USERS).child(snapshot.getKey()).child(Constants.PROFILE).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot profileDataSnapshot) {
                                room = profileDataSnapshot.getValue(Room.class);
                                room.setFirebaseRoomId(dataSnapshot.getKey());
                                Contact contact = mContactsSyncManager.getContactByVoxUserName(room.getVoxUserName());
                                if (contact != null && contact.getName() != null) {
                                    room.setFullName(contact.getName());
                                }
                                arrayOfUsers.add(room);

                                Firebase firebaseRoomReference = authReference.child(Constants.ROOMS).child(dataSnapshot.getKey()).child(Constants.CHATS);
                                firebaseRoomReference.limitToLast(1).addChildEventListener(createChildEventListener(room));
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {
                                firebaseError.getMessage();
                            }
                        });
                    }
                }
            } else {
                room = new Room();
                room.setFirebaseRoomId(dataSnapshot.getKey());
                room.setGroupName(roomInfo.getName());
                room.setImage(roomInfo.getImage());
                room.setVoxUserName(voxUserName);
                arrayOfUsers.add(room);
                Firebase firebaseRoomReference = authReference.child(Constants.ROOMS).child(dataSnapshot.getKey()).child(Constants.CHATS);
                firebaseRoomReference.limitToLast(1).addChildEventListener(createChildEventListener(room));
            }
        }
        chatRoomListAdapter.addItems(arrayOfUsers);
    }
}
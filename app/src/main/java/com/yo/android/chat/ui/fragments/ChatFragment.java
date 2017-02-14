package com.yo.android.chat.ui.fragments;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.firebase.database.Query;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.adapters.ChatRoomListAdapter;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.chat.ui.CreateGroupActivity;
import com.yo.android.helpers.PopupHelper;
import com.yo.android.model.ChatMessage;
import com.yo.android.model.ChatMessageReceived;
import com.yo.android.model.Contact;
import com.yo.android.model.Popup;
import com.yo.android.model.Room;
import com.yo.android.model.RoomInfo;
import com.yo.android.ui.BottomTabsActivity;
import com.yo.android.util.Constants;
import com.yo.android.util.FireBaseHelper;
import com.yo.android.util.PopupDialogListener;
import com.yo.android.util.Util;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends BaseFragment implements AdapterView.OnItemClickListener, SharedPreferences.OnSharedPreferenceChangeListener, PopupDialogListener {

    private static final String TAG = "ChatFragment";

    @Bind(R.id.lv_chat_room)
    ListView listView;
    @Bind(R.id.empty_chat)
    ImageView emptyImageView;

    @Bind(R.id.no_search_results)
    protected TextView noSearchResult;

    private List<ChildEventListener> childEventListenersList;
    private List<Room> arrayOfUsers;
    private ChatRoomListAdapter chatRoomListAdapter;
    private Menu menu;
    private Room room;
    private String voxUserName;
    private List<Room> listRoom;
    private List<String> roomId;
    private List<String> checkRoomIdExist;
    private SimpleDateFormat formatterDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss z");
    private Activity activity;
    private int executed;
    private int activeCount;


    @Inject
    FireBaseHelper fireBaseHelper;

    @Inject
    @Named("login")
    PreferenceEndPoint loginPrefs;

    @Inject
    ContactsSyncManager mContactsSyncManager;
    private boolean isAlreadyShown;
    //private boolean isRemoved;
    private boolean isSharedPreferenceShown;
    private boolean isShowDefault;

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
        roomId = new ArrayList<>();
        checkRoomIdExist = new ArrayList<>();
        EventBus.getDefault().register(this);
        preferenceEndPoint.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        executed = 0;
        activeCount = 0;
        isShowDefault = false;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        ButterKnife.bind(this, view);
        listView.setOnItemClickListener(this);
        emptyImageView.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        if (menuVisible) {
            isRoomsExist();
        }
        super.setMenuVisibility(menuVisible);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_chat, menu);
        this.menu = menu;
        Util.changeSearchProperties(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Util.prepareContactsSearch(activity, menu, chatRoomListAdapter, Constants.CHAT_FRAG, noSearchResult);
        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.menu_search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                emptyImageView.setVisibility(View.GONE);
                Util.changeMenuItemsVisibility(menu, R.id.menu_search, false);
                dismissProgressDialog();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                boolean nonEmpty = (chatRoomListAdapter != null && chatRoomListAdapter.getOriginalListCount() > 0);
                emptyImageView.setVisibility(nonEmpty ? View.GONE : View.VISIBLE);
                noSearchResult.setVisibility(View.GONE);
                getActivity().invalidateOptionsMenu();
                return true;
            }
        });
        switch (item.getItemId()) {
            case R.id.chat_contact:
                startActivity(new Intent(activity, AppContactsActivity.class));
                break;
            case R.id.create_group:
                startActivity(new Intent(activity, CreateGroupActivity.class));
                break;
            default:
                break;

        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = getActivity();
        if (activity != null) {
            chatRoomListAdapter = new ChatRoomListAdapter(activity.getApplicationContext());
            listView.setAdapter(chatRoomListAdapter);
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Room room = chatRoomListAdapter.getItem(position);
        if (activity != null) {
            ChatActivity.start(activity, room);
        }
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

    }

    private void isRoomsExist() {
        try {
            showProgressDialog();
            if (isAdded() && activity != null) {
                Firebase authReference = fireBaseHelper.authWithCustomToken(activity, loginPrefs.getStringPreference(Constants.FIREBASE_TOKEN));
                String firebaseUserId = loginPrefs.getStringPreference(Constants.FIREBASE_USER_ID);
                if (!firebaseUserId.isEmpty()) {
                    authReference.child(Constants.USERS).child(firebaseUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.child(Constants.MY_ROOMS).exists()) {
                                getAllRooms();
                                emptyImageView.setVisibility(View.GONE);
                                listView.setVisibility(View.VISIBLE);
                            } else {
                                emptyImageView.setVisibility(View.VISIBLE);
                                listView.setVisibility(View.GONE);
                            }
                            //dismissProgressDialog();
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                            dismissProgressDialog();
                            firebaseError.getMessage();
                            emptyImageView.setVisibility(View.VISIBLE);
                            listView.setVisibility(View.GONE);

                            Log.i(TAG, "firebase Token roomExists :: " + loginPrefs.getStringPreference(Constants.FIREBASE_TOKEN));
                            Log.i(TAG, "firebase User Id roomExists :: " + loginPrefs.getStringPreference(Constants.FIREBASE_USER_ID));
                        }
                    });
                    authReference.keepSynced(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getAllRooms() {
        Firebase authReference = fireBaseHelper.authWithCustomToken(activity, loginPrefs.getStringPreference(Constants.FIREBASE_TOKEN));
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getMembersId(dataSnapshot);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                dismissProgressDialog();

                Log.i(TAG, "firebase Token getAllRooms :: " + loginPrefs.getStringPreference(Constants.FIREBASE_TOKEN));
                Log.i(TAG, "firebase User Id getAllRooms :: " + loginPrefs.getStringPreference(Constants.FIREBASE_USER_ID));
            }
        };
        String firebaseUserId = loginPrefs.getStringPreference(Constants.FIREBASE_USER_ID);
        if (!firebaseUserId.isEmpty()) {
            authReference.child(Constants.USERS).child(firebaseUserId).child(Constants.MY_ROOMS).addValueEventListener(valueEventListener);
            authReference.keepSynced(true);
        }
    }

    @NonNull
    private ChildEventListener createChildEventListener(final Room room) {

        return new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                room.setYouserId(chatMessage.getYouserId());
                if (dataSnapshot.hasChildren()) {
                    room.setLastChat(chatMessage.getMessage());
                    if (!TextUtils.isEmpty(chatMessage.getType()) && chatMessage.getType().equals(Constants.IMAGE)) {
                        room.setImages(true);
                    } else {
                        room.setImages(false);
                    }
                    room.setTime(chatMessage.getTime());
                    room.setTimeStamp(Util.getChatListTimeFormat(activity, chatMessage.getTime()));
                    if (!arrayOfUsers.contains(room)) {
                        arrayOfUsers.add(room);
                    } else {
                        listRoom = new ArrayList<>();
                        listRoom.addAll(arrayOfUsers);
                        for (Room customer : listRoom) {
                            if (customer.getFirebaseRoomId() != null && customer.getId() != null) {
                                if (arrayOfUsers.contains(customer) && customer.getFirebaseRoomId().equalsIgnoreCase(room.getFirebaseRoomId()) && !(customer.getId().equalsIgnoreCase(room.getId()))) {
                                    int i = listRoom.indexOf(customer);
                                    arrayOfUsers.set(i, room);
                                }
                            }
                        }
                        if (!listRoom.isEmpty()) {
                            listRoom.clear();
                        }
                    }
                }
                try {
                    Collections.sort(arrayOfUsers, new Comparator<Room>() {
                        @Override
                        public int compare(Room lhs, Room rhs) {
                            return Long.valueOf(rhs.getTime()).compareTo(lhs.getTime());
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
                chatRoomListAdapter.addChatRoomItems(arrayOfUsers);
                try {
                    if (!chatRoomListAdapter.isEmpty()) {
                        dismissProgressDialog();
                        emptyImageView.setVisibility(View.GONE);
                    } else {
                        emptyImageView.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
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
                        room.setTimeStamp(Util.getChatListTimeFormat(activity, chatMessage.getTime()));
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

    private void getMembersId(final DataSnapshot dataSnapshot) {
        for (final DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
            if (!roomId.contains(dataSnapshot1.getKey())) {

                Firebase memberReference = dataSnapshot1.getRef().getRoot().child(Constants.ROOMS).child(dataSnapshot1.getKey());
                com.firebase.client.Query query = memberReference.child(Constants.ROOM_INFO).orderByChild("status");
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot roomInfoDataSnapshot) {
                        executed = executed + 1;
                        if (roomInfoDataSnapshot.getValue(RoomInfo.class).getStatus().equals(Constants.ROOM_STATUS_ACTIVE)) {
                            roomId.add(dataSnapshot1.getKey());
                            activeCount = activeCount + 1;
                            isShowDefault = false;
                        } else if (dataSnapshot.getChildrenCount() == executed) {
                            dismissProgressDialog();
                            if (activeCount == 0) {
                                emptyImageView.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        firebaseError.getMessage();
                        executed = executed + 1;
                    }
                });
                memberReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(Constants.ROOM_INFO)) {
                            RoomInfo roomInfo = dataSnapshot.child(Constants.ROOM_INFO).getValue(RoomInfo.class);
                            if (roomInfo.getStatus().equalsIgnoreCase(Constants.ROOM_STATUS_ACTIVE)) {
                                getMembersProfile(dataSnapshot);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        firebaseError.getMessage();
                        dismissProgressDialog();
                    }
                });
            }
        }

        Collections.sort(arrayOfUsers, new Comparator<Room>() {
            @Override
            public int compare(Room lhs, Room rhs) {
                return Long.valueOf(rhs.getTime()).compareTo(lhs.getTime());
            }
        });

        chatRoomListAdapter.addChatRoomItems(arrayOfUsers);
        try {

            if (!chatRoomListAdapter.isEmpty()) {
                dismissProgressDialog();
            } else if (isShowDefault) {
                emptyImageView.setVisibility(View.VISIBLE);
            } else {
                emptyImageView.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    private List<Room> getMembersProfile(final DataSnapshot dataSnapshot) {

        final Firebase authReference = fireBaseHelper.authWithCustomToken(activity, loginPrefs.getStringPreference(Constants.FIREBASE_TOKEN));
        final String firebaseUserId = loginPrefs.getStringPreference(Constants.FIREBASE_USER_ID);
        final RoomInfo roomInfo = dataSnapshot.child(Constants.ROOM_INFO).getValue(RoomInfo.class);
        if (roomInfo.getName() != null && roomInfo.getName().isEmpty()) {
            for (DataSnapshot snapshot : dataSnapshot.child(Constants.MEMBERS).getChildren()) {
                if (!firebaseUserId.equalsIgnoreCase(snapshot.getKey())) {
                    authReference.child(Constants.USERS).child(snapshot.getKey()).child(Constants.PROFILE).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot profileDataSnapshot) {

                            room = profileDataSnapshot.getValue(Room.class);
                            if (room != null) {
                                room.setFirebaseRoomId(dataSnapshot.getKey());
                                Contact contact = mContactsSyncManager.getContactByVoxUserName(room.getVoxUserName());
                                if (contact != null && contact.getName() != null) {
                                    room.setFullName(contact.getName());
                                } else if (contact == null && room != null) {
                                    room.setFullName(room.getPhoneNumber());
                                }

                                arrayOfUsers.add(room);

                                Firebase firebaseRoomReference = authReference.child(Constants.ROOMS).child(dataSnapshot.getKey()).child(Constants.CHATS);
                                firebaseRoomReference.limitToLast(1).addChildEventListener(createChildEventListener(room));
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                            executed = 1;
                            firebaseError.getMessage();
                        }
                    });
                }
            }
        } else {

            Date date = null;
            try {
                String createdTime = roomInfo.getCreated_at();
                date = formatterDate.parse(createdTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            room = new Room();
            room.setFirebaseRoomId(dataSnapshot.getKey());
            room.setGroupName(roomInfo.getName());
            room.setImage(roomInfo.getImage());
            room.setVoxUserName(voxUserName);
            //room.setTime(Long.parseLong(groupCreatedTime));
            if (date != null) {
                room.setTime(date.getTime());
            }
            if (!arrayOfUsers.contains(room)) {
                arrayOfUsers.add(room);
            }
            Firebase firebaseRoomReference = authReference.child(Constants.ROOMS).child(dataSnapshot.getKey()).child(Constants.CHATS);
            firebaseRoomReference.limitToLast(1).addChildEventListener(createChildEventListener(room));
        }

        return arrayOfUsers;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (activity instanceof BottomTabsActivity) {
                BottomTabsActivity bottomTabsActivity = (BottomTabsActivity) activity;
                if (bottomTabsActivity.getFragment() instanceof ChatFragment) {
                    if (preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION) != null) {
                        Type type = new TypeToken<List<Popup>>() {
                        }.getType();
                        List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
                        if (popup != null) {
                            Collections.reverse(popup);
                            isAlreadyShown = false;
                            for (Popup p : popup) {
                                if (p.getPopupsEnum() == PopupHelper.PopupsEnum.CHATS) {
                                    if (!isAlreadyShown) {
                                        //PopupHelper.getPopup(PopupHelper.PopupsEnum.CHATS, popup, activity, preferenceEndPoint, this, this);
                                        PopupHelper.getSinglePopup(PopupHelper.PopupsEnum.CHATS, p, getActivity(), preferenceEndPoint, this, this, popup);
                                        isAlreadyShown = true;
                                        isSharedPreferenceShown = false;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } else {
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (activity instanceof BottomTabsActivity) {
            BottomTabsActivity bottomTabsActivity = (BottomTabsActivity) activity;
            if (bottomTabsActivity.getFragment() instanceof ChatFragment) {
                if (preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION) != null) {
                    //if (!isRemoved) {
                    Type type = new TypeToken<List<Popup>>() {
                    }.getType();
                    List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
                    if (popup != null) {
                        for (Popup p : popup) {
                            if (p.getPopupsEnum() == PopupHelper.PopupsEnum.CHATS) {
                                if (!isAlreadyShown) {
                                    //PopupHelper.getPopup(PopupHelper.PopupsEnum.CHATS, popup, activity, preferenceEndPoint, this, this);
                                    PopupHelper.getSinglePopup(PopupHelper.PopupsEnum.CHATS, p, getActivity(), preferenceEndPoint, this, this, popup);
                                    isAlreadyShown = true;
                                    isSharedPreferenceShown = true;
                                    break;
                                }
                            }
                        }
                    }

                }
            }
        }

    }

    @Override
    public void closePopup() {
        //isAlreadyShown = false;
        //isRemoved = true;
        //preferenceEndPoint.removePreference(Constants.POPUP_NOTIFICATION);
        Type type = new TypeToken<List<Popup>>() {
        }.getType();
        List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
        if (popup != null) {
            if (!isSharedPreferenceShown) {
                Collections.reverse(popup);
            }
            List<Popup> tempPopup = new ArrayList<>(popup);
            for (Popup p : popup) {
                if (p.getPopupsEnum() == PopupHelper.PopupsEnum.CHATS) {
                    tempPopup.remove(p);
                    break;
                }
            }
            popup = tempPopup;
        }
        //popup.remove(0);
        preferenceEndPoint.saveStringPreference(Constants.POPUP_NOTIFICATION, new Gson().toJson(popup));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        preferenceEndPoint.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
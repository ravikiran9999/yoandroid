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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
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
import com.yo.android.model.Contact;
import com.yo.android.model.Popup;
import com.yo.android.model.Room;
import com.yo.android.model.RoomInfo;
import com.yo.android.ui.BottomTabsActivity;
import com.yo.android.util.Constants;
import com.yo.android.util.DateUtil;
import com.yo.android.util.FireBaseHelper;
import com.yo.android.util.PopupDialogListener;
import com.yo.android.util.Util;

import org.json.JSONArray;

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
public class ChatFragment extends BaseFragment implements AdapterView.OnItemClickListener, SharedPreferences.OnSharedPreferenceChangeListener, PopupDialogListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "ChatFragment";

    @Bind(R.id.lv_chat_room)
    ListView listView;
    @Bind(R.id.empty_chat)
    ImageView emptyImageView;
    @Bind(R.id.no_search_results)
    protected TextView noSearchResult;
    @Bind(R.id.swipeContainer)
    protected SwipeRefreshLayout swipeRefreshContainer;

    private ArrayList<Room> arrayOfUsers;
    private ChatRoomListAdapter chatRoomListAdapter;
    private Menu menu;
    private Room room;
    private Room mPRoom;
    private String voxUserName;
    private List<String> roomIdList;
    private SimpleDateFormat formatterDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss z");
    private Activity activity;
    private int executed;
    private int activeCount;
    private Room tempRoom;

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
    private SearchView searchView;

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
        arrayOfUsers = new ArrayList<>();
        roomIdList = new ArrayList<>();
        EventBus.getDefault().register(this);
        preferenceEndPoint.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        executed = 0;
        activeCount = 0;
        isShowDefault = false;
        synchronized (this) {
            isRoomsExist(null);
            isRoomsExists(null);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        ButterKnife.bind(this, view);
        getActiveSavedRooms();
        listView.setOnItemClickListener(this);
        emptyImageView.setVisibility(View.GONE);
        swipeRefreshContainer.setOnRefreshListener(this);
        return view;
    }

    private void getActiveSavedRooms() {
        try {
            String activeRooms = loginPrefs.getStringPreference(Constants.FIRE_BASE_ROOMS);
            if (activeRooms != null && !activeRooms.isEmpty()) {
                JSONArray jsonArray = new JSONArray(activeRooms);
                roomIdList = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    if (!roomIdList.contains(jsonArray.getString(i))) {
                        roomIdList.add(jsonArray.getString(i));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        if (menuVisible) {
            isRoomsExist(null);
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
        Util.prepareContactsSearch(activity, menu, chatRoomListAdapter, Constants.CHAT_FRAG, noSearchResult, null);
        searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
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
        if (searchView != null && !TextUtils.isEmpty(searchView.getQuery())) {
            searchView.setQuery(searchView.getQuery(), false);
        } else {
            String rooms = loginPrefs.getStringPreference(Constants.FIRE_BASE_ROOMS);

            ArrayList<String> roomsListdata = new Gson().fromJson(rooms,
                    new TypeToken<ArrayList<String>>() {
                    }.getType());

            String databaseReference = loginPrefs.getStringPreference(Constants.FIRE_BASE_ROOMS_REFERENCE);
            if (!"".equalsIgnoreCase(databaseReference)) {
                Firebase firebaseDatabaseReference = new Firebase(databaseReference);
                activeMembers(firebaseDatabaseReference, roomsListdata);
            }
            //isRoomsExist(null);
        }

    }

    private void isRoomsExists(final SwipeRefreshLayout swipeRefreshContainer) {
        try {
            Firebase authReference = fireBaseHelper.authWithCustomToken(activity, loginPrefs.getStringPreference(Constants.FIREBASE_TOKEN));
            String firebaseUserId = loginPrefs.getStringPreference(Constants.FIREBASE_USER_ID);
            if (!firebaseUserId.isEmpty()) {
                authReference.child(Constants.USERS).child(firebaseUserId).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        getAllRooms(swipeRefreshContainer, true);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void isRoomsExist(final SwipeRefreshLayout swipeRefreshContainer) {
        try {
            refreshProgress(swipeRefreshContainer);
            if (isAdded() && activity != null) {
                Firebase authReference = fireBaseHelper.authWithCustomToken(activity, loginPrefs.getStringPreference(Constants.FIREBASE_TOKEN));
                String firebaseUserId = loginPrefs.getStringPreference(Constants.FIREBASE_USER_ID);
                if (!firebaseUserId.isEmpty()) {
                    authReference.child(Constants.USERS).child(firebaseUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.child(Constants.MY_ROOMS).exists()) {
                                getAllRooms(swipeRefreshContainer, false);
                                emptyImageView.setVisibility(View.GONE);
                                listView.setVisibility(View.VISIBLE);
                            } else {
                                emptyImageView.setVisibility(View.VISIBLE);
                                listView.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                            //dismissProgressDialog();
                            //emptyImageView.setVisibility(View.VISIBLE);
                            firebaseError.getMessage();
                            listView.setVisibility(View.GONE);
                            showEmptyImage();
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

    private void getAllRooms(final SwipeRefreshLayout allRoomsRefreshContainer, final boolean added) {
        Firebase authReference = fireBaseHelper.authWithCustomToken(activity, loginPrefs.getStringPreference(Constants.FIREBASE_TOKEN));
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(added == false) {
                    new FirebaseAsync().execute(dataSnapshot, null, allRoomsRefreshContainer);
                } else {
                    for (final DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        final String mRoomId = dataSnapshot1.getKey();
                        if (!roomIdList.contains(mRoomId))
                            roomIdList.add(mRoomId);
                    }

                    activeMembers(dataSnapshot.getRef(), roomIdList);
                }
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
                    room.setTimeStamp(DateUtil.getChatListTimeFormat(activity, chatMessage.getTime()));
                    if (!arrayOfUsers.contains(room)) {
                        arrayOfUsers.add(room);
                    } else {
                        List<Room> listRoom = new ArrayList<>();
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
                if (!arrayOfUsers.isEmpty()) {
                    try {
                        ArrayList<Room> roomsSortedList = sortedList(arrayOfUsers);
                        chatRoomListAdapter.addChatRoomItems(roomsSortedList);
                        if (!chatRoomListAdapter.isEmpty()) {
                            dismissProgressDialog();
                            emptyImageView.setVisibility(View.GONE);
                        } else {
                            emptyImageView.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    showEmptyImage();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) { }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                try {
                    ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                    if (dataSnapshot.hasChildren()) {
                        room.setLastChat("");
                        room.setImages(false);
                        room.setTimeStamp(DateUtil.getChatListTimeFormat(activity, chatMessage.getTime()));
                    }
                    if (chatRoomListAdapter != null) {
                        chatRoomListAdapter.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) { }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                firebaseError.getMessage();
            }
        };

    }

    public void onEventMainThread(String action) {
        if (Constants.CHAT_ROOM_REFRESH.equals(action)) {
//            getChatRoomList();
        }

    }

    @Override
    public void onRefresh() {
        isRoomsExist(swipeRefreshContainer);
    }

    private class FirebaseAsync extends AsyncTask<Object, Void, List<String>> {
        Firebase databaseReference = null;
        SwipeRefreshLayout swipeRefreshContainer;

        @Override
        protected List<String> doInBackground(final Object... params) {
            //swipeRefreshContainer = (SwipeRefreshLayout) params[1];
            for (final DataSnapshot dataSnapshot1 : ((DataSnapshot) params[0]).getChildren()) {
                final String roomId = dataSnapshot1.getKey();
                databaseReference = dataSnapshot1.getRef();
                if (!roomIdList.contains(roomId)) {
                    Firebase memberReference = dataSnapshot1.getRef().getRoot().child(Constants.ROOMS).child(roomId);
                    com.firebase.client.Query query = memberReference.child(Constants.ROOM_INFO).orderByChild("status");

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot roomInfoDataSnapshot) {
                            executed = executed + 1;
                            RoomInfo roomInfo = roomInfoDataSnapshot.getValue(RoomInfo.class);
                            if (roomInfo != null && roomInfo.getStatus().equals(Constants.ROOM_STATUS_ACTIVE)) {
                                roomIdList.add(roomId);
                                activeCount = activeCount + 1;
                                isShowDefault = false;
                            } else if (((DataSnapshot) params[0]).getChildrenCount() == executed) {
                                //dismissProgressDialog();
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
                }
            }
            return roomIdList;
        }

        @Override
        protected void onPostExecute(List<String> roomsList) {

            if (roomsList != null && roomsList.size() > 0) {
                //dismissProgressDialog();

                if (swipeRefreshContainer != null) {
                    swipeRefreshContainer.setRefreshing(false);
                } else {
                    dismissProgressDialog();
                }

                String listString = new Gson().toJson(roomsList,
                        new TypeToken<ArrayList<Room>>() {
                        }.getType());
                loginPrefs.removePreference(Constants.FIRE_BASE_ROOMS);
                loginPrefs.saveStringPreference(Constants.FIRE_BASE_ROOMS, listString);
                loginPrefs.saveStringPreference(Constants.FIRE_BASE_ROOMS_REFERENCE, databaseReference.toString());

                /*if (chatRoomListAdapter.getOriginalListCount() > 0) {
                    chatRoomListAdapter.clearAll();
                }*/

                activeMembers(databaseReference, roomsList);
            } else {
                showEmptyImage();
            }
            super.onPostExecute(roomsList);
        }
    }

    private void activeMembers(Firebase firebaseAuthReference, List<String> roomsList) {
        Firebase memberReference = firebaseAuthReference.getRoot().child(Constants.ROOMS);
        for (String roomId : roomsList) {
            memberReference.child(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(Constants.ROOM_INFO)) {
                        Room mRoom = getMembersProfile(dataSnapshot);
                        if (mRoom != null && !arrayOfUsers.contains(mRoom))
                            arrayOfUsers.add(mRoom);
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    firebaseError.getMessage();
                    showEmptyImage();
                }
            });
        }
    }

    private Room getMembersProfile(final DataSnapshot dataSnapshot) {

        final Firebase authReference = fireBaseHelper.authWithCustomToken(activity, loginPrefs.getStringPreference(Constants.FIREBASE_TOKEN));
        final String firebaseUserId = loginPrefs.getStringPreference(Constants.FIREBASE_USER_ID);
        final RoomInfo roomInfo = dataSnapshot.child(Constants.ROOM_INFO).getValue(RoomInfo.class);
        if (roomInfo.getName() != null && roomInfo.getName().isEmpty()) {
            for (DataSnapshot snapshot : dataSnapshot.child(Constants.MEMBERS).getChildren()) {
                if (!firebaseUserId.equalsIgnoreCase(snapshot.getKey())) {
                    authReference.child(Constants.USERS).child(snapshot.getKey()).child(Constants.PROFILE).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot profileDataSnapshot) {

                            mPRoom = profileDataSnapshot.getValue(Room.class);
                            if (mPRoom != null) {
                                mPRoom.setFirebaseRoomId(dataSnapshot.getKey());
                                Contact contact = mContactsSyncManager.getContactByVoxUserName(mPRoom.getNexgeUserName());
                                if (contact != null && contact.getName() != null) {
                                    mPRoom.setFullName(contact.getName());
                                } else if (contact == null && mPRoom != null) {
                                    //room.setFullName(room.getPhoneNumber());
                                    mPRoom.setFullName(mPRoom.getMobileNumber()); // phone number with country code
                                }
                                tempRoom = null;
                                tempRoom = room;
                                //arrayOfUsers.add(room);

                                Firebase firebaseRoomReference = authReference.child(Constants.ROOMS).child(dataSnapshot.getKey()).child(Constants.CHATS);
                                firebaseRoomReference.limitToLast(1).addChildEventListener(createChildEventListener(mPRoom));
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                            executed = 1;
                            firebaseError.getMessage();
                        }
                    });
                }
                if (tempRoom != null) {
                    return tempRoom;
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
            mPRoom = new Room();
            mPRoom.setFirebaseRoomId(dataSnapshot.getKey());
            mPRoom.setGroupName(roomInfo.getName());
            mPRoom.setImage(roomInfo.getImage());
            mPRoom.setNexgeUserName(voxUserName);
            //room.setTime(Long.parseLong(groupCreatedTime));
            if (date != null) {
                mPRoom.setTime(date.getTime());
            }

            Firebase firebaseRoomReference = authReference.child(Constants.ROOMS).child(dataSnapshot.getKey()).child(Constants.CHATS);
            firebaseRoomReference.limitToLast(1).addChildEventListener(createChildEventListener(mPRoom));
            if (!arrayOfUsers.contains(mPRoom)) {
                return mPRoom;
            }
        }

        return mPRoom;
    }

    private void showEmptyImage() {
        dismissProgressDialog();
        emptyImageView.setVisibility(View.VISIBLE);
    }

    private ArrayList<Room> sortedList(ArrayList<Room> mSortedList) {
        Collections.sort(mSortedList, new Comparator<Room>() {
            @Override
            public int compare(Room lhs, Room rhs) {
                return Long.valueOf(rhs.getTime()).compareTo(lhs.getTime());
            }
        });
        return mSortedList;
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
                    Type type = new TypeToken<List<Popup>>() {
                    }.getType();
                    List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
                    if (popup != null) {
                        for (Popup p : popup) {
                            if (p.getPopupsEnum() == PopupHelper.PopupsEnum.CHATS) {
                                if (!isAlreadyShown) {
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
        preferenceEndPoint.saveStringPreference(Constants.POPUP_NOTIFICATION, new Gson().toJson(popup));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        preferenceEndPoint.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    private void refreshProgress(SwipeRefreshLayout swipeRefreshContainer) {
        if (swipeRefreshContainer != null) {
            swipeRefreshContainer.setRefreshing(false);
        } else {
            showProgressDialog();
        }
    }
}
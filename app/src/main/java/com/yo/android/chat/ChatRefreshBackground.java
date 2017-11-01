package com.yo.android.chat;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.di.Injector;
import com.yo.android.model.Room;
import com.yo.android.model.RoomInfo;
import com.yo.android.util.Constants;
import com.yo.android.util.FireBaseHelper;
import com.yo.dialer.CallExtras;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by root on 30/10/17.
 */

public class ChatRefreshBackground {
    private static final String TAG = ChatRefreshBackground.class.getSimpleName();

    @Inject
    FireBaseHelper fireBaseHelper;

    @Inject
    @Named("login")
    PreferenceEndPoint loginPrefs;

    private static ChatRefreshBackground instance;

    public static ChatRefreshBackground getInstance() {
        if (instance == null) {
            instance = new ChatRefreshBackground();
        }

        return instance;
    }

    public void doRefresh(final Context context, final String roomId) {
        Injector.obtain(context.getApplicationContext()).inject(this);
        Firebase authReference = fireBaseHelper.authWithCustomToken(context, loginPrefs.getStringPreference(Constants.FIREBASE_TOKEN));
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                new FirebaseRoomAsync(context, roomId).execute(dataSnapshot, null, null);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
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

    private class FirebaseRoomAsync extends AsyncTask<Object, Void, List<String>> {
        private List<String> roomIdList = new ArrayList<>();
        Firebase databaseReference = null;
        private String roomId;
        private Context context;

        public FirebaseRoomAsync(Context context, String roomId) {
            this.roomId = roomId;
            this.context = context;
        }


        @Override
        protected List<String> doInBackground(Object... params) {
            for (final DataSnapshot dataSnapshot1 : ((DataSnapshot) params[0]).getChildren()) {
                databaseReference = dataSnapshot1.getRef();
                if (roomId == null) {
                    roomId = dataSnapshot1.getKey();
                }
                if (!roomIdList.contains(roomId)) {
                    Firebase memberReference = dataSnapshot1.getRef().getRoot().child(Constants.ROOMS).child(roomId);
                    com.firebase.client.Query query = memberReference.child(Constants.ROOM_INFO).orderByChild("status");

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot roomInfoDataSnapshot) {
                            RoomInfo roomInfo = roomInfoDataSnapshot.getValue(RoomInfo.class);
                            if (roomInfo != null && roomInfo.getStatus().equals(Constants.ROOM_STATUS_ACTIVE)) {
                                if (!roomIdList.contains(roomId)) {
                                    roomIdList.add(roomId);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                            firebaseError.getMessage();
                        }
                    });
                }
            }
            return roomIdList;
        }

        @Override
        protected void onPostExecute(List<String> roomsList) {

            if (roomsList != null && roomsList.size() > 0) {
                String rooms = loginPrefs.getStringPreference(Constants.FIRE_BASE_ROOMS);
                ArrayList<String> roomsListdata = new Gson().fromJson(rooms,
                        new TypeToken<ArrayList<String>>() {
                        }.getType());
                if (roomsListdata == null) {
                    roomsListdata = new ArrayList<String>();
                }
                roomsListdata.addAll(roomsList);
                String listString = new Gson().toJson(roomsListdata,
                        new TypeToken<ArrayList<String>>() {
                        }.getType());
                loginPrefs.saveStringPreference(Constants.FIRE_BASE_ROOMS, listString);
                loginPrefs.saveStringPreference(Constants.FIRE_BASE_ROOMS_REFERENCE, databaseReference.toString());
                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                        .getInstance(context);
                localBroadcastManager.sendBroadcast(new Intent(CallExtras.Actions.CHAT_GROUP_CREATED));
            }
        }
    }
}

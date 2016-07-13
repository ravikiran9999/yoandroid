package com.yo.android.util;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yo.android.model.ChatMessage;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Ramesh on 13/7/16.
 */
@Singleton
public class FireBaseHelper {
    private DatabaseReference roomReference;
    private Map<String, ChatMessage> map = new HashMap<>();

    @Inject
    public FireBaseHelper() {
        roomReference = FirebaseDatabase.getInstance().getReference(Constants.ROOM_ID);
    }

    public void startListenForRooms() {

    }

    public void startListeningRoom(final String roomId) {
        if (!map.containsKey(roomId)) {
            DatabaseReference roomIdReference = roomReference.child(roomId);
            roomIdReference.limitToLast(1).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                    map.put(roomId, chatMessage);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public ChatMessage getLastMessage(String roomId) {
        if (!map.containsKey(roomId)) {
            return null;
            //startListeningRoom(roomId);
        }
        return map.get(roomId);
    }


}

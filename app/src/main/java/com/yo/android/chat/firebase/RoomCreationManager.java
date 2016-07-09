package com.yo.android.chat.firebase;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.model.ChatRoom;
import com.yo.android.util.Constants;

/**
 * Created by rdoddapaneni on 7/7/2016.
 */

public class RoomCreationManager {

    public static void showUserChatScreen(final Context context, @NonNull final String yourPhoneNumber, @NonNull final String opponentPhoneNumber) {
        final String roomCombination1 = yourPhoneNumber + ":" + opponentPhoneNumber;
        final String roomCombination2 = opponentPhoneNumber + ":" + yourPhoneNumber;
        DatabaseReference databaseRoomReference = FirebaseDatabase.getInstance().getReference(Constants.ROOM);
        databaseRoomReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean value1 = dataSnapshot.hasChild(roomCombination1);
                boolean value2 = dataSnapshot.hasChild(roomCombination2);
                if (value1) {
                    navigateToChatScreen(context, roomCombination1, opponentPhoneNumber);
                } else if (value2) {
                    navigateToChatScreen(context, roomCombination2, opponentPhoneNumber);
                } else {
                    String chatRoomId = yourPhoneNumber + ":" + opponentPhoneNumber;
                    ChatRoom chatRoom = new ChatRoom(yourPhoneNumber, opponentPhoneNumber, chatRoomId);
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Constants.ROOM);
                    DatabaseReference databaseRoomReference = databaseReference.child(chatRoomId);
                    databaseRoomReference.setValue(chatRoom);
                    navigateToChatScreen(context, chatRoomId, opponentPhoneNumber);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private static void navigateToChatScreen(Context context, String roomId, String opponentPhoneNumber) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.CHAT_ROOM_ID, roomId);
        intent.putExtra(Constants.OPPONENT_PHONE_NUMBER, opponentPhoneNumber);
        context.startActivity(intent);
    }
}

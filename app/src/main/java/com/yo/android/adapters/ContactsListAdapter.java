package com.yo.android.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yo.android.R;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.helpers.RegisteredContactsViewHolder;
import com.yo.android.model.ChatRoom;
import com.yo.android.model.Registration;
import com.yo.android.util.Constants;
import com.yo.android.voip.OutGoingCallActivity;

import java.util.List;

/**
 * Created by rdoddapaneni on 6/29/2016.
 */

public class ContactsListAdapter extends AbstractBaseAdapter<Registration, RegisteredContactsViewHolder> {

    private Context context;
    private String userId;

    public ContactsListAdapter(Context context) {
        super(context);
        this.context = context;
    }

    public ContactsListAdapter(Context context, String userId) {
        super(context);
        this.context = context;
        this.userId = userId;
    }

    @Override
    public int getLayoutId() {
        return R.layout.contacts_list_item;
    }

    @Override
    public RegisteredContactsViewHolder getViewHolder(View convertView) {
        return new RegisteredContactsViewHolder(convertView);
    }

    @Override
    public void bindView(final int position, RegisteredContactsViewHolder holder, Registration item) {

        holder.getContactNumber().setText(item.getPhoneNumber());
        //holder.getContactMail().setText(item.getEmailId());
        holder.getMessageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Registration registration = getAllItems().get(position);
                String yourPhoneNumber = userId;
                String opponentPhoneNumber = registration.getPhoneNumber();
                showUserChatScreen(yourPhoneNumber, opponentPhoneNumber);
            }
        });
        holder.getCallView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Registration registration = getAllItems().get(position);
                String opponentPhoneNumber = registration.getPhoneNumber();
                if (opponentPhoneNumber != null) {
                    Intent intent = new Intent(context, OutGoingCallActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(OutGoingCallActivity.CALLER_NO, opponentPhoneNumber);
                    context.startActivity(intent);
                }
            }
        });

    }

    private void showUserChatScreen(@NonNull final String yourPhoneNumber, @NonNull final String opponentPhoneNumber) {
        final String roomCombination1 = yourPhoneNumber + ":" + opponentPhoneNumber;
        final String roomCombination2 = opponentPhoneNumber + ":" + yourPhoneNumber;
        DatabaseReference databaseRoomReference = FirebaseDatabase.getInstance().getReference(Constants.ROOM);
        databaseRoomReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean value1 = dataSnapshot.hasChild(roomCombination1);
                boolean value2 = dataSnapshot.hasChild(roomCombination2);
                if (value1) {
                    navigateToChatScreen(roomCombination1, opponentPhoneNumber);
                } else if (value2) {
                    navigateToChatScreen(roomCombination2, opponentPhoneNumber);
                } else {
                    String chatRoomId = yourPhoneNumber + ":" + opponentPhoneNumber;
                    ChatRoom chatRoom = new ChatRoom(yourPhoneNumber, opponentPhoneNumber, chatRoomId);
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Constants.ROOM);
                    DatabaseReference databaseRoomReference = databaseReference.child(chatRoomId);
                    databaseRoomReference.setValue(chatRoom);
                    navigateToChatScreen(chatRoomId, opponentPhoneNumber);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void navigateToChatScreen(String roomId, String opponentPhoneNumber) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.CHAT_ROOM_ID, roomId);
        intent.putExtra(Constants.OPPONENT_PHONE_NUMBER, opponentPhoneNumber);
        context.startActivity(intent);
    }
}

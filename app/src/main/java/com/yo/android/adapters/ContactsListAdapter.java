package com.yo.android.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yo.android.R;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.helpers.RegisteredContactsViewHolder;
import com.yo.android.model.Contact;
import com.yo.android.util.Constants;
import com.yo.android.voip.OutGoingCallActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rdoddapaneni on 6/29/2016.
 */

public class ContactsListAdapter extends AbstractBaseAdapter<Contact, RegisteredContactsViewHolder> {

    private Context context;
    private String userId;

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
    public void bindView(final int position, RegisteredContactsViewHolder holder, final Contact item) {
        if(!item.getYoAppUser()) {
            holder.getContactNumber().setText(item.getPhoneNo());
        } else {
            holder.getContactNumber().setText(item.getPhoneNo());
        }
        //holder.getContactMail().setText(item.getEmailId());
        holder.getMessageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Registration registration = getAllItems().get(position);
                String yourPhoneNumber = userId;
                String opponentPhoneNumber = item.getPhoneNo();
                showUserChatScreen(mContext, yourPhoneNumber, opponentPhoneNumber);


            }
        });

        holder.getCallView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Registration registration = getAllItems().get(position);
                //String opponentPhoneNumber = registration.getPhoneNumber();
                String opponentPhoneNumber = item.getPhoneNo();

                if (opponentPhoneNumber != null) {
                    Intent intent = new Intent(context, OutGoingCallActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(OutGoingCallActivity.CALLER_NO, opponentPhoneNumber);
                    context.startActivity(intent);
                }
            }
        });
        if (item.getYoAppUser()) {
            holder.getMessageView().setImageResource(R.drawable.ic_message);
        } else {
            holder.getMessageView().setImageResource(R.drawable.ic_invite_friends);
        }

    }


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
                    navigateToChatScreen(context, roomCombination1, opponentPhoneNumber, yourPhoneNumber);
                } else if (value2) {
                    navigateToChatScreen(context, roomCombination2, opponentPhoneNumber, yourPhoneNumber);
                } else {

                    navigateToChatScreen(context, "", opponentPhoneNumber, yourPhoneNumber);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private static void navigateToChatScreen(Context context, String roomId, String opponentPhoneNumber, String yourPhoneNumber) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.CHAT_ROOM_ID, roomId);
        intent.putExtra(Constants.OPPONENT_PHONE_NUMBER, opponentPhoneNumber);
        intent.putExtra(Constants.YOUR_PHONE_NUMBER, yourPhoneNumber);
        context.startActivity(intent);

    }
}

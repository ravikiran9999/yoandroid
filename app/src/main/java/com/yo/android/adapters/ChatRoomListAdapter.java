package com.yo.android.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.View;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.di.Injector;
import com.yo.android.helpers.ChatRoomViewHolder;
import com.yo.android.model.ChatMessage;
import com.yo.android.model.ChatRoom;
import com.yo.android.util.Constants;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by rdoddapaneni on 7/5/2016.
 */

public class ChatRoomListAdapter extends AbstractBaseAdapter<ChatRoom, ChatRoomViewHolder> {

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;

    private DatabaseReference roomReference;

    public ChatRoomListAdapter(Context context) {
        super(context);
        Injector.obtain(context.getApplicationContext()).inject(this);

        roomReference = FirebaseDatabase.getInstance().getReference(Constants.ROOM_ID);
    }

    @Override
    public int getLayoutId() {
        return R.layout.chat_room_list_item;
    }

    @Override
    public ChatRoomViewHolder getViewHolder(View convertView) {
        return new ChatRoomViewHolder(convertView);
    }

    @Override
    public void bindView(int position, ChatRoomViewHolder holder, final ChatRoom item) {

        String yourPhoneNumber = preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER);

        if (item.getOpponentPhoneNumber().equals(yourPhoneNumber)) {
            holder.getOpponentName().setText(item.getYourPhoneNumber());
        } else {
            holder.getOpponentName().setText(item.getOpponentPhoneNumber());
        }

        if (item.getChatRoomId() != null) {
            DatabaseReference roomIdReference = roomReference.child(item.getChatRoomId());
            roomIdReference.limitToLast(1).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    try {
                        ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                        item.setMessage(chatMessage.getMessage());
                        item.setTimeStamp(DateUtils.getRelativeTimeSpanString(chatMessage.getTime()).toString());
                        notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        holder.getChat().setText(item.getMessage());
        holder.getTimeStamp().setText(item.getTimeStamp());
    }
}

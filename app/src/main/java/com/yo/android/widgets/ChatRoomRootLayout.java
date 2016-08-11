package com.yo.android.widgets;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yo.android.R;
import com.yo.android.model.ChatMessage;
import com.yo.android.model.ChatRoom;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

/**
 * Created by Ramesh on 13/7/16.
 */
public class ChatRoomRootLayout extends RelativeLayout {

    public ChatRoomRootLayout(Context context) {
        super(context);
    }

    public ChatRoomRootLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatRoomRootLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ChatRoomRootLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ChatRoom chatRoom = (ChatRoom) getTag(R.id.wide);
        if (chatRoom != null) {
            // roomReference.limitToLast(1).addValueEventListener(listener);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ChatRoom chatRoom = (ChatRoom) getTag(R.id.wide);
        if (chatRoom != null) {
            // roomReference.limitToLast(1).addValueEventListener(listener);
        }
    }

    private ValueEventListener listener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
            ChatRoom item = (ChatRoom) getTag(R.id.wide);
            item.setMessage(chatMessage.getMessage());
            if (TextUtils.isEmpty(chatMessage.getImagePath())) {
                item.setIsImage(false);
            } else {
                item.setIsImage(true);
            }
            item.setTimeStamp(Util.getChatListTimeFormat(getContext(), chatMessage.getTime()));

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
}

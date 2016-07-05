package com.yo.android.adapters;

import android.content.Context;
import android.view.View;

import com.yo.android.R;
import com.yo.android.helpers.ChatRoomViewHolder;
import com.yo.android.model.ChatRoom;

/**
 * Created by rdoddapaneni on 7/5/2016.
 */

public class ChatRoomListAdapter extends AbstractBaseAdapter<ChatRoom, ChatRoomViewHolder>  {

    public ChatRoomListAdapter(Context context) {
        super(context);
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
    public void bindView(int position, ChatRoomViewHolder holder, ChatRoom item) {

        holder.getOpponentName().setText(item.getOpponentPhoneNumber());
    }
}

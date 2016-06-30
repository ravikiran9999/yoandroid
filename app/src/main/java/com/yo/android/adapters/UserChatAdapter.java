package com.yo.android.adapters;

import android.content.Context;
import android.view.View;

import com.yo.android.R;
import com.yo.android.adapters.AbstractBaseAdapter;
import com.yo.android.helpers.UserChatViewHolder;
import com.yo.android.model.ChatMessage;

/**
 * Created by rdoddapaneni on 6/30/2016.
 */

public class UserChatAdapter extends AbstractBaseAdapter<ChatMessage, UserChatViewHolder> {

    public UserChatAdapter(Context context) {
        super(context);
    }

    @Override
    public int getLayoutId() {
        return R.layout.user_chat_list_item;
    }

    @Override
    public UserChatViewHolder getViewHolder(View convertView) {
        return new UserChatViewHolder(convertView);
    }

    @Override
    public void bindView(int position, UserChatViewHolder holder, ChatMessage item) {
        holder.getChatText().setText(item.getMessage());
    }
}

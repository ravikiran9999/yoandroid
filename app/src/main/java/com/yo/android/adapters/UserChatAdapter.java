package com.yo.android.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.View;

import com.yo.android.R;
import com.yo.android.helpers.UserChatViewHolder;
import com.yo.android.model.ChatMessage;

/**
 * Created by rdoddapaneni on 6/30/2016.
 */

public class UserChatAdapter extends AbstractBaseAdapter<ChatMessage, UserChatViewHolder> {

    private String userId;

    public UserChatAdapter(Context context) {
        super(context);

    }

    public UserChatAdapter(Context context, String userId) {
        super(context);
        this.userId = userId;
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
//        String timeStamp = DateFormat.format("hh:mm", new Date(item.getTime())).toString();
        String timeStamp = DateUtils.getRelativeTimeSpanString(item.getTime(), System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS).toString();
        holder.getChatText().setText(item.getMessage());
        holder.getChatTimeStamp().setText(timeStamp);
        if (userId.equals(item.getSenderID())) {
            holder.getLinearLayout().setGravity(Gravity.RIGHT);
            holder.getChatText().setBackground(mContext.getResources().getDrawable(R.drawable.bg_send));
        } else {
            holder.getLinearLayout().setGravity(Gravity.NO_GRAVITY);
            holder.getChatText().setBackground(mContext.getResources().getDrawable(R.drawable.out_message_bg));
        }
    }


}

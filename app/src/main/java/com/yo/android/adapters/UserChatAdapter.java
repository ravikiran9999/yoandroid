package com.yo.android.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.adapters.AbstractBaseAdapter;
import com.yo.android.di.Injector;
import com.yo.android.helpers.UserChatViewHolder;
import com.yo.android.model.ChatMessage;

import java.sql.Timestamp;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;

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
        String timeStamp = DateFormat.format("hh:mm", new Date(item.getTime())).toString();

        holder.getChatText().setText(item.getMessage());
        holder.getChatTimeStamp().setText(timeStamp);
        if(item.getSenderID().equals(userId)) {
            holder.getLinearLayout().setGravity(Gravity.RIGHT);
        } else {
            holder.getLinearLayout().setGravity(Gravity.NO_GRAVITY);
        }
    }
}

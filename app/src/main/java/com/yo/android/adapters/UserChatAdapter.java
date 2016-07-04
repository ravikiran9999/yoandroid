package com.yo.android.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.yo.android.R;
import com.yo.android.helpers.UserChatViewHolder;
import com.yo.android.model.ChatMessage;

/**
 * Created by rdoddapaneni on 6/30/2016.
 */

public class UserChatAdapter extends AbstractBaseAdapter<ChatMessage, UserChatViewHolder> {

    private String userId;
    private Context context;

    public UserChatAdapter(Context context) {
        super(context);
        this.context = context;

    }

    public UserChatAdapter(Context context, String userId) {
        super(context);
        this.context = context;
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
        String timeStamp = DateUtils.getRelativeTimeSpanString(item.getTime(), System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS).toString();
        holder.getChatText().setText(item.getMessage());
        holder.getChatTimeStamp().setText(timeStamp);
        if (userId.equals(item.getSenderID())) {

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 100);
            layoutParams.setMargins(60,0,0,0);
            //holder.getLinearLayout().setGravity(Gravity.LEFT);

            holder.getLinearLayoutText().setBackground(mContext.getResources().getDrawable(R.drawable.bg_sms_yellow));
            holder.getLinearLayoutText().setLayoutParams(layoutParams);

        } else {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 100);
            layoutParams.setMargins(0,0,60,0);

            holder.getLinearLayoutText().setBackground(mContext.getResources().getDrawable(R.drawable.bg_sms_gray));
            holder.getLinearLayoutText().setLayoutParams(layoutParams);
        }
    }


}

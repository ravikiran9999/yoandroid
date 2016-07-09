package com.yo.android.adapters;

import android.content.Context;
import android.os.Build;
import android.text.format.DateUtils;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.View;
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
    private  SparseBooleanArray mSelectedItemsIds;

    public UserChatAdapter(Context context) {
        super(context);
        this.context = context;

    }

    public UserChatAdapter(Context context, String userId) {
        super(context);
        this.context = context;
        this.userId = userId;
        this.mSelectedItemsIds = new  SparseBooleanArray();
    }


    public void  toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    // Remove selection after unchecked
    public void  removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    // Item checked on selection
    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position,  value);
        else
            mSelectedItemsIds.delete(position);
        notifyDataSetChanged();
    }

    // Get number of selected item
    public int  getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    public  SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
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
        try {
            String timeStamp = DateUtils.getRelativeTimeSpanString(item.getTime(), System.currentTimeMillis(), DateUtils.WEEK_IN_MILLIS).toString();

            holder.getChatText().setText(item.getMessage());
            holder.getChatTimeStamp().setText(timeStamp);
            if (userId.equals(item.getSenderID())) {

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(60, 0, 0, 0);
                holder.getLinearLayout().setGravity(Gravity.RIGHT);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    holder.getLinearLayoutText().setBackground(mContext.getResources().getDrawable(R.drawable.bg_sms_yellow));
                } else {
                    holder.getLinearLayoutText().setBackgroundResource(R.drawable.bg_sms_yellow);
                }
                holder.getLinearLayoutText().setLayoutParams(layoutParams);

            } else {
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, 0, 60, 0);
                holder.getLinearLayout().setGravity(Gravity.LEFT);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    holder.getLinearLayoutText().setBackground(mContext.getResources().getDrawable(R.drawable.bg_sms_grey));
                } else {
                    holder.getLinearLayoutText().setBackgroundResource(R.drawable.bg_sms_grey);
                }
                holder.getLinearLayoutText().setLayoutParams(layoutParams);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}

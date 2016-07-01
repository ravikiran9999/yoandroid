package com.yo.android.helpers;

import android.view.View;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractViewHolder;

/**
 * Created by rdoddapaneni on 6/30/2016.
 */

public class UserChatViewHolder extends AbstractViewHolder {

    private TextView chatText;
    private TextView chatTimeStamp;

    public UserChatViewHolder(View view) {
        super(view);
        chatText = (TextView) view.findViewById(R.id.tv_chat_text);
        //contactNumber = (TextView) view.findViewById(R.id.tv_phone_number);

    }

    public TextView getChatText() {
        return chatText;
    }

    public TextView getChatTimeStamp() {
        return chatTimeStamp;
    }
}

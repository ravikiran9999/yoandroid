package com.yo.android.helpers;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractViewHolder;

/**
 * Created by rdoddapaneni on 6/30/2016.
 */

public class UserChatViewHolder extends AbstractViewHolder {

    private TextView chatText;
    private TextView chatTimeStamp;
    private LinearLayout linearLayout;
    private LinearLayout linearLayoutText;

    public UserChatViewHolder(View view) {
        super(view);
        //chatText = (TextView) view.findViewById(R.id.tv_chat_text);
        chatTimeStamp = (TextView) view.findViewById(R.id.time_stamp);
        linearLayout = (LinearLayout) view.findViewById(R.id.linear);
        linearLayoutText = (LinearLayout) view.findViewById(R.id.linear_text_back);
        //contactNumber = (TextView) view.findViewById(R.id.tv_phone_number);

    }

/*    public TextView getChatText() {
        return chatText;
    }*/

    public TextView getChatTimeStamp() {
        return chatTimeStamp;
    }

    public LinearLayout getLinearLayout() {
        return linearLayout;
    }

    public LinearLayout getLinearLayoutText() {
        return linearLayoutText;
    }

}

package com.yo.android.helpers;

import android.view.View;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractViewHolder;

/**
 * Created by rdoddapaneni on 7/5/2016.
 */

public class ChatRoomViewHolder extends AbstractViewHolder {

    private TextView opponentName;
    private TextView timeStamp;
    private TextView chat;

    public ChatRoomViewHolder(View view) {
        super(view);

        opponentName = (TextView) view.findViewById(R.id.tv_opponent_name);
        chat = (TextView) view.findViewById(R.id.tv_chat_message);
        timeStamp = (TextView) view.findViewById(R.id.tv_time_stamp);
    }

    public TextView getOpponentName() {
        return opponentName;
    }

    public TextView getTimeStamp() {
        return timeStamp;
    }

    public TextView getChat() {
        return chat;
    }
}

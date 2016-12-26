package com.yo.android.helpers;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractViewHolder;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by rdoddapaneni on 7/5/2016.
 */

public class ChatRoomViewHolder extends AbstractViewHolder {

    @Bind(R.id.tv_opponent_name)
    TextView opponentName;
    @Bind(R.id.tv_time_stamp)
    TextView timeStamp;
    @Bind(R.id.tv_chat_message)
    TextView chat;
    @Bind(R.id.imv_chat_room_pic)
    ImageView chatRoomPic;

    public ChatRoomViewHolder(View view) {
        super(view);
        ButterKnife.bind(this,view);
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

    public ImageView getChatRoomPic() {
        return chatRoomPic;
    }
}

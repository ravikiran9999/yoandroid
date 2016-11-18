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

    private TextView name;
    private TextView chatTimeStamp;
    private LinearLayout linearLayout;
    private LinearLayout linearLayoutText;
    private LinearLayout ll;
    private TextView seenTimeStamp;
    //private TextView timeStampHeader;

    public UserChatViewHolder(View view) {
        super(view);
        //name = (TextView) view.findViewById(R.id.name);
        chatTimeStamp = (TextView) view.findViewById(R.id.time_stamp);
        seenTimeStamp = (TextView) view.findViewById(R.id.delivered_time_stamp);
        linearLayout = (LinearLayout) view.findViewById(R.id.linear);
        ll = (LinearLayout) view.findViewById(R.id.linear_layout);
        //timeStampHeader = (TextView) view.findViewById(R.id.time_stamp_header);
        //contactNumber = (TextView) view.findViewById(R.id.tv_phone_number);

    }

    public TextView getName() {
        return name;
    }

    public TextView getChatTimeStamp() {
        return chatTimeStamp;
    }

    public TextView getSeenTimeStamp() {
        return seenTimeStamp;
    }

    public LinearLayout getLinearLayout() {
        return linearLayout;
    }

    public LinearLayout getLl() {
        return ll;
    }

//    public TextView getTimeStampHeader() {
//        return timeStampHeader;
//    }
}

package com.yo.android.helpers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractViewHolder;

/**
 * Created by rdoddapaneni on 6/30/2016.
 */

public class UserChatViewHolder extends AbstractViewHolder {

    private TextView name;
    private ImageView chatTimeStamp;
    private RelativeLayout linearLayout;
    private LinearLayout linearLayoutText;
    private LinearLayout ll;
    private TextView seenTimeStamp;

    public UserChatViewHolder(View view) {
        super(view);
        //name = (TextView) view.findViewById(R.id.name);
        linearLayout = (RelativeLayout) view.findViewById(R.id.linear);
        ll = (LinearLayout) view.findViewById(R.id.linear_layout);
        //contactNumber = (TextView) view.findViewById(R.id.tv_phone_number);

    }

    public TextView getName() {
        return name;
    }

    public ImageView getChatTimeStamp() {
        return chatTimeStamp;
    }

    public TextView getSeenTimeStamp() {
        return seenTimeStamp;
    }

    public RelativeLayout getLinearLayout() {
        return linearLayout;
    }

    public LinearLayout getLl() {
        return ll;
    }
}

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

    private LinearLayout linearLayout;
    private LinearLayout ll;


    public UserChatViewHolder(View view) {
        super(view);
        linearLayout = (LinearLayout) view.findViewById(R.id.linear);
        ll = (LinearLayout) view.findViewById(R.id.linear_layout);
    }

    public LinearLayout getLinearLayout() {
        return linearLayout;
    }

    public LinearLayout getLl() {
        return ll;
    }

}

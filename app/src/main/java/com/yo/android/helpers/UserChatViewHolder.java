package com.yo.android.helpers;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractViewHolder;


import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by rdoddapaneni on 6/30/2016.
 */

public class UserChatViewHolder extends AbstractViewHolder {

    @BindView(R.id.linear)
    LinearLayout linearLayout;
    @BindView(R.id.linear_layout)
    LinearLayout ll;


    public UserChatViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public LinearLayout getLinearLayout() {
        return linearLayout;
    }

    public LinearLayout getLl() {
        return ll;
    }

}

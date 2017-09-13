package com.yo.android.helpers;

import android.view.View;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractViewHolder;



public class GroupUserViewHolder extends AbstractViewHolder {

    private TextView textItem;

    public GroupUserViewHolder(View view) {
        super(view);
        textItem = (TextView) view.findViewById(R.id.tv_name);
    }

    public TextView getTextItem() {
        return textItem;
    }
}

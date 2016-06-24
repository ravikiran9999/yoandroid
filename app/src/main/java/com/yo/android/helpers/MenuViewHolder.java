package com.yo.android.helpers;

import android.view.View;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractViewHolder;


/**
 * Created by ramesh on 12/3/16.
 */
public class MenuViewHolder extends AbstractViewHolder {

    protected TextView titleView;

    public MenuViewHolder(View view) {
        super(view);
        titleView = (TextView) view.findViewById(R.id.title);
    }

    public TextView getTitleView() {
        return titleView;
    }
}

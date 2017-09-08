package com.yo.android.helpers;

import android.view.View;

import com.yo.android.adapters.YoViewHolder;

/**
 * Created by rdoddapaneni on 6/22/2017.
 */

public class EmptyViewHolder extends YoViewHolder {

    public EmptyViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bindData(Object data) {

    }
}

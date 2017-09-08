package com.yo.android.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by rdoddapaneni on 6/20/2017.
 */

public abstract class YoViewHolder extends RecyclerView.ViewHolder {

    public abstract void bindData(Object data);

    public YoViewHolder(View itemView) {
        super(itemView);
    }
}

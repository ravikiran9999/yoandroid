package com.yo.android.helpers;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractViewHolder;


/**
 * Created by ramesh on 12/3/16.
 */
public class MenuViewHolder extends AbstractViewHolder {

    protected TextView titleView;
    protected TextView tvBalance;

    protected ImageView imageView;

    public MenuViewHolder(View view) {
        super(view);
        titleView = (TextView) view.findViewById(R.id.title);
        tvBalance = (TextView) view.findViewById(R.id.tv_balance);
        imageView = (ImageView) view.findViewById(R.id.image);
    }

    public TextView getTitleView() {
        return titleView;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public TextView getTvBalance() {
        return tvBalance;
    }
}

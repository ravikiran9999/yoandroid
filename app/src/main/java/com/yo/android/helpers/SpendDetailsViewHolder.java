package com.yo.android.helpers;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.yo.android.R;


import butterknife.BindView;
import butterknife.ButterKnife;

public class SpendDetailsViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.txtPhone)
    TextView txtPhone;
    @BindView(R.id.txt_price)
    TextView txtPrice;
    @BindView(R.id.txtDate)
    TextView date;
    @BindView(R.id.txt_duration)
    TextView duration;
    @BindView(R.id.txt_reason)
    TextView txtReason;


    public SpendDetailsViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public TextView getTxtPrice() {
        return txtPrice;
    }

    public TextView getTxtPhone() {
        return txtPhone;
    }

    public TextView getDate() {
        return date;
    }

    public TextView getDuration() {
        return duration;
    }

    public TextView getTxtReason() {
        return txtReason;
    }
}

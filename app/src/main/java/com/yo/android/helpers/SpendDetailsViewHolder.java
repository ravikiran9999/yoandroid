package com.yo.android.helpers;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.yo.android.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SpendDetailsViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.txtPhone)
    TextView txtPhone;
    @Bind(R.id.txt_price)
    TextView txtPrice;
    @Bind(R.id.txtDate)
    TextView date;
    @Bind(R.id.txt_duration)
    TextView duration;
    @Bind(R.id.txt_reason)
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

package com.yo.android.helpers;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yo.android.R;


import butterknife.BindView;
import butterknife.ButterKnife;

public class RechargeDetailsViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.imgArrow)
    ImageView arrow;

    @BindView(R.id.txtPhone)
    TextView txtPhone;

    @BindView(R.id.txt_price)
    TextView txtPrice;

    @BindView(R.id.txt_pulse)
    TextView txtPulse;

    @BindView(R.id.duration_container)
    View durationContainer;

    @BindView(R.id.date)
    TextView date;

    @BindView(R.id.duration)
    TextView duration;

    public RechargeDetailsViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }


    public TextView getTxtPulse() {
        return txtPulse;
    }

    public TextView getTxtPrice() {
        return txtPrice;
    }

    public ImageView getArrow() {
        return arrow;
    }

    public TextView getTxtPhone() {
        return txtPhone;
    }

    public View getDurationContainer() {
        return durationContainer;
    }

    public TextView getDate() {
        return date;
    }

    public TextView getDuration() {
        return duration;
    }

}

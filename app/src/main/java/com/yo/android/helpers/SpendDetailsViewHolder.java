package com.yo.android.helpers;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractViewHolder;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Ramesh on 25/7/16.
 */
public class SpendDetailsViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.imgArrow)
    ImageView arrow;

    @Bind(R.id.txtPhone)
    TextView txtPhone;

    @Bind(R.id.txt_price)
    TextView txtPrice;

    @Bind(R.id.txt_pulse)
    TextView txtPulse;

    @Bind(R.id.duration_container)
    View durationContainer;

    @Bind(R.id.date)
    TextView date;

    @Bind(R.id.duration)
    TextView duration;

    public SpendDetailsViewHolder(View view) {
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

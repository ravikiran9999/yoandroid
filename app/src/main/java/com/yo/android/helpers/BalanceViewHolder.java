package com.yo.android.helpers;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.model.MoreData;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by rdoddapaneni on 6/21/2017.
 */

public class BalanceViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.title)
    TextView textView;
    @Bind(R.id.image)
    ImageView arrowView;
    @Bind(R.id.text_view)
    TextView balanceView;


    public BalanceViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(Object data) {

        MoreData item = (MoreData) data;
        textView.setText(item.getName());
        if (item.isHasOptions()) {
            arrowView.setVisibility(View.VISIBLE);
        } else {
            arrowView.setVisibility(View.GONE);
        }
        if (item.getBalance() != null) {
            balanceView.setText(item.getBalance());
            balanceView.setVisibility(View.VISIBLE);
        } else {
            balanceView.setVisibility(View.GONE);
        }
    }
}

package com.yo.android.helpers;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.yo.android.R;
import com.yo.android.model.denominations.Denominations;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by rdoddapaneni on 6/22/2017.
 */

public class DenominationItemViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.btn1)
    Button button;

    public DenominationItemViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(Object data) {
        Denominations item = (Denominations) data;
        button.setText(item.getCurrencySymbol() + " " + item.getDenomination());
    }
}

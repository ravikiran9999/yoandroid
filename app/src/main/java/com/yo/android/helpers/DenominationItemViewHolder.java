package com.yo.android.helpers;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.model.denominations.Denominations;


import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by rdoddapaneni on 6/22/2017.
 */

public class DenominationItemViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.btn1)
    Button button;
    @BindView(R.id.denomination_view)
    TextView denomination;

    public DenominationItemViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(Object data) {
        Denominations item = (Denominations) data;
        denomination.setText(item.getCurrencySymbol() + " " + item.getDenomination());
    }

    public Button getButton() {
        return button;
    }
}

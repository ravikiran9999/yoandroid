package com.yo.android.helpers;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.yo.android.R;
import com.yo.android.model.ecommerce.ItemPackages;


import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by admin on 9/11/2017.
 */

public class ItemPackageViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.buy_package)
    Button button;

    public ItemPackageViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(Object data) {
        ItemPackages item = (ItemPackages) data;
        button.setText(item.getCurrencySymbol() + " " + item.getDenomination());
    }

    public Button getButton() {
        return button;
    }
}

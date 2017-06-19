package com.yo.android.helpers;

import android.view.View;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractViewHolder;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by rdoddapaneni on 6/19/2017.
 */

public class WalletViewHolder extends AbstractViewHolder {

    @Bind(R.id.txt_title_balance)
    TextView title;
    @Bind(R.id.txt_balance)
    TextView balance;

    public WalletViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public TextView getTitle() {
        return title;
    }

    public TextView getBalance() {
        return balance;
    }
}

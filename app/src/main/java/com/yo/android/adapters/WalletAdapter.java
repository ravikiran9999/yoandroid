package com.yo.android.adapters;

import android.content.Context;
import android.view.View;
import com.yo.android.R;
import com.yo.android.helpers.WalletViewHolder;
import com.yo.android.model.Wallet;

/**
 * Created by rdoddapaneni on 6/19/2017.
 */

public class WalletAdapter extends AbstractBaseAdapter<Wallet, WalletViewHolder> {

    public WalletAdapter(Context context) {
        super(context);
    }

    @Override
    public int getLayoutId() {
        return R.layout.credit_item;
    }

    @Override
    public WalletViewHolder getViewHolder(View convertView) {
        return new WalletViewHolder(convertView);
    }

    @Override
    public void bindView(int position, WalletViewHolder holder, Wallet item) {

        if (item != null) {
            if(item.getTotalBalance() != null) {
                holder.getTitle().setText(item.getBalanceDescription());
                holder.getBalance().setText(item.getTotalBalance());
            } else {
                holder.getTitle().setText(null);
                holder.getBalance().setText(null);
            }
        }
    }
}

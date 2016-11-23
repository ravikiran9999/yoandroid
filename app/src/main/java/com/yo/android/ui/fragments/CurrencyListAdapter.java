package com.yo.android.ui.fragments;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import com.yo.android.R;
import com.yo.android.adapters.AbstractBaseAdapter;
import com.yo.android.adapters.MoreListAdapter;
import com.yo.android.helpers.MenuViewHolder;
import com.yo.android.model.MoreData;
import com.yo.android.model.denominations.Denominations;

/**
 * Created by rajesh on 22/11/16.
 */
public class CurrencyListAdapter extends AbstractBaseAdapter<Denominations, CurrencyViewHolder> {
    public CurrencyListAdapter(Context context) {
        super(context);
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_with_payment_options;
    }

    @Override
    public CurrencyViewHolder getViewHolder(View convertView) {
        return new CurrencyViewHolder(convertView);
    }

    @Override
    public void bindView(int position, CurrencyViewHolder holder, Denominations item) {

    }
}

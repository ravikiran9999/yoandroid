package com.yo.android.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.adapters.AbstractBaseAdapter;
import com.yo.android.helpers.MenuViewHolder;
import com.yo.android.helpers.Numerics;
import com.yo.android.model.MoreData;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by root on 15/7/16.
 */
public class MoreListAdapter extends AbstractBaseAdapter<MoreData, MenuViewHolder> {
    @Inject
    @Named("login")
    PreferenceEndPoint preferenceEndPoint;

    public MoreListAdapter(Context context) {
        super(context);
    }

    @Override
    public int getLayoutId() {
        return R.layout.nav_drawer_row;
    }

    @Override
    public MenuViewHolder getViewHolder(View convertView) {
        return new MenuViewHolder(convertView);
    }

    @Override
    public void bindView(int position, MenuViewHolder holder, MoreData item) {
        holder.getTitleView().setText(item.getName());
        if (item.isHasOptions()) {
            holder.getImageView().setVisibility(View.VISIBLE);
        }

        if (item.getName().trim().contains("Yo Credit")) {
            final SpannableString text = new SpannableString(item.getName());
            text.setSpan(new ForegroundColorSpan(Color.RED), Numerics.TEN, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.getTitleView().setText(text);
        }
    }
}

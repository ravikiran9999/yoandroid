package com.yo.android.adapters;

import android.content.Context;
import android.view.View;

import com.yo.android.R;
import com.yo.android.helpers.MenuViewHolder;
import com.yo.android.model.MenuData;

/**
 * Created by ramesh on 12/3/16.
 */
public class MenuListAdapter extends AbstractBaseAdapter<MenuData, MenuViewHolder> {

    public MenuListAdapter(Context context) {
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
    public void bindView(int position, MenuViewHolder holder, MenuData item) {
        holder.getTitleView().setText(item.getName());
        holder.getImageView().setImageResource(item.getIcon());
    }
}

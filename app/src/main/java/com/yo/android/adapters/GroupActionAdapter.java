package com.yo.android.adapters;


import android.content.Context;
import android.view.View;

import com.yo.android.R;
import com.yo.android.helpers.GroupUserViewHolder;
import com.yo.android.model.GroupAction;

public class GroupActionAdapter extends AbstractBaseAdapter<GroupAction, GroupUserViewHolder> {

    public GroupActionAdapter(Context context) {
        super(context);
    }

    @Override
    public int getLayoutId() {
        return R.layout.group_user_layout;
    }

    @Override
    public GroupUserViewHolder getViewHolder(View convertView) {
        return new GroupUserViewHolder(convertView);
    }

    @Override
    public void bindView(int position, GroupUserViewHolder holder, GroupAction item) {
        String fullName = item.getDisplayItem();
        if(fullName != null) {
            holder.getTextItem().setText(fullName);
        } else {
            holder.getTextItem().setText("");
        }
    }
}

package com.yo.android.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.squareup.picasso.Picasso;
import com.yo.android.R;
import com.yo.android.helpers.MyCollectionsViewHolder;
import com.yo.android.model.Collections;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by creatives on 7/9/2016.
 */
public class MyCollectionsAdapter extends AbstractBaseAdapter<Collections, MyCollectionsViewHolder> {

    private boolean contextualMenuEnable;

    public MyCollectionsAdapter(Context context) {
        super(context);
    }

    @Override
    public int getLayoutId() {
        return R.layout.create_magazine_item;
    }

    @Override
    public MyCollectionsViewHolder getViewHolder(View convertView) {
        return new MyCollectionsViewHolder(convertView);
    }

    @Override
    public void bindView(int position, MyCollectionsViewHolder holder, Collections item) {

        if (position == 0 && "Follow more topics".equalsIgnoreCase(item.getName())) {
            Picasso.with(mContext)
                    .load(R.color.grey_divider)
                    .into(holder.getImageView());
        } else if (!TextUtils.isEmpty(item.getImage())) {
            Picasso.with(mContext)
                    .load(item.getImage())
                    .into(holder.getImageView());
        } else {
            Picasso.with(mContext)
                    .load(R.color.grey_divider)
                    .into(holder.getImageView());
        }
        holder.getTextView().setText(item.getName());
        if (position != 0) {
            holder.getTextView().setTextColor(mContext.getResources().getColor(android.R.color.white));
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.LEFT | Gravity.TOP;
            params.leftMargin = 5;
            params.rightMargin = 5;
            holder.getTextView().setLayoutParams(params);

        } else if(position == 0 && !"Follow more topics".equalsIgnoreCase(item.getName())) {
            holder.getTextView().setTextColor(mContext.getResources().getColor(android.R.color.white));
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.LEFT | Gravity.TOP;
            params.leftMargin = 5;
            params.rightMargin = 5;
            holder.getTextView().setLayoutParams(params);
        } else {
            holder.getTextView().setTextColor(mContext.getResources().getColor(android.R.color.black));
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            holder.getTextView().setLayoutParams(params);
        }
        //
        if (!contextualMenuEnable) {
            item.setSelect(false);
        }
        //1. Hide tick
        holder.getTick().setVisibility(View.GONE);
        if (contextualMenuEnable) {
            if (item.isSelect()) {
                //Show tick
                if (position != 0) {
                    holder.getTick().setVisibility(View.VISIBLE);
                } else {
                    holder.getTick().setVisibility(View.GONE);
                }
            }
        }

    }

    public void setContextualMenuEnable(boolean enable) {
        this.contextualMenuEnable = enable;
    }

    public List<Collections> getSelectedItems() {
        List<Collections> list = new ArrayList<>();
        for (Collections collections : mList) {
            if (collections.isSelect()) {
                list.add(collections);
            }
        }
        return list;
    }

    @Override
    protected boolean hasData(Collections collections, String key) {
        if (collections.getName() != null && !"Follow more topics".equals(collections.getName())) {
            if (containsValue(collections.getName().toLowerCase(), key)) {
                return true;
            }
        }
        return super.hasData(collections, key);
    }

    private boolean containsValue(String str, String key) {
        return str != null && str.toLowerCase().contains(key);
    }
}

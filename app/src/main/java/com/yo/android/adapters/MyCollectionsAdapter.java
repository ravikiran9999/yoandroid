package com.yo.android.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.yo.android.R;
import com.yo.android.helpers.MyCollectionsViewHolder;
import com.yo.android.model.Collections;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by creatives on 7/9/2016.
 */
/*public class MyCollectionsAdapter extends BaseAdapter {
    private Context mContext;
    private List<Collections> collectionsList;
    private boolean contextualMenuEnable;

    public MyCollectionsAdapter(final Context context) {
        mContext = context;
    }

    public void addItems(final List<Collections> collectionsList) {
        this.collectionsList = collectionsList;
        notifyDataSetChanged();
    }

    public int getCount() {
        return collectionsList.size();
    }

    public void setContextualMenuEnable(boolean enable) {
        this.contextualMenuEnable = enable;
    }

    public List<Collections> getSelectedItems() {
        List<Collections> list = new ArrayList<>();
        for (Collections collections : collectionsList) {
            if (collections.isSelect()) {
                list.add(collections);
            }
        }
        return list;
    }

    public Collections getItem(int position) {
        return collectionsList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        View layout = convertView;
        if (layout == null) {
            layout = LayoutInflater.from(mContext).inflate(R.layout.create_magazine_item, parent, false);
            holder = new ViewHolder();
            holder.imageView = (ImageView) layout.findViewById(R.id.img_magazine);
            holder.textView = (TextView) layout.findViewById(R.id.tv_title);
            holder.tick = (ImageView) layout.findViewById(R.id.imv_magazine_tick);
            layout.setTag(holder);
        } else {
            holder = (ViewHolder) layout.getTag();
        }

        Collections mCollections = getItem(position);
        if (position == 0) {
            Picasso.with(mContext)
                    .load(R.color.grey_divider)
                    .into(holder.imageView);
        } else if (!TextUtils.isEmpty(collectionsList.get(position).getImage())) {
            Picasso.with(mContext)
                    .load(collectionsList.get(position).getImage())
                    .into(holder.imageView);
        } else {
            Picasso.with(mContext)
                    .load(R.color.grey_divider)
                    .into(holder.imageView);
        }
        holder.textView.setText(collectionsList.get(position).getName());
        if (position != 0) {
            holder.textView.setTextColor(mContext.getResources().getColor(android.R.color.white));
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.LEFT | Gravity.TOP;
            params.leftMargin = 5;
            params.rightMargin = 5;
            holder.textView.setLayoutParams(params);

        } else {
            holder.textView.setTextColor(mContext.getResources().getColor(android.R.color.black));
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            holder.textView.setLayoutParams(params);
        }
        //
        if (!contextualMenuEnable) {
            mCollections.setSelect(false);
        }
        //1. Hide tick
        holder.tick.setVisibility(View.GONE);
        if (contextualMenuEnable) {
            if (mCollections.isSelect()) {
                //Show tick
                if (position != 0) {
                    holder.tick.setVisibility(View.VISIBLE);
                } else {
                    holder.tick.setVisibility(View.GONE);
                }
            }
        }

        return layout;
    }

    private class ViewHolder {

        private ImageView imageView;
        private TextView textView;
        private ImageView tick;

    }
}*/

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

        if (position == 0 && item.getName().equalsIgnoreCase("Follow more topics")) {
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

        }
        else if(position == 0 && !item.getName().equalsIgnoreCase("Follow more topics")) {
            holder.getTextView().setTextColor(mContext.getResources().getColor(android.R.color.white));
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.LEFT | Gravity.TOP;
            params.leftMargin = 5;
            params.rightMargin = 5;
            holder.getTextView().setLayoutParams(params);
        }
        else {
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
        if (collections.getName() != null && !collections.getName().equals("Follow more topics")) {
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

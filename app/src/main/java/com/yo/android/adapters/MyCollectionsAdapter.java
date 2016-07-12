package com.yo.android.adapters;

import android.content.Context;
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
import com.yo.android.model.Collections;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by creatives on 7/9/2016.
 */
public class MyCollectionsAdapter extends BaseAdapter {
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

//    public void setSelectedPosition(int position, boolean enable) {
//        getItem(position).toggleSelection();
//    }

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

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.create_magazine_item, parent, false);
        }
        ImageView imageView = (ImageView) convertView.findViewById(R.id.img_magazine);
        Collections mCollections = getItem(position);
        if (collectionsList.get(position).getImage() != "") {
            Picasso.with(mContext)
                    .load(collectionsList.get(position).getImage())
                    .into(imageView);
        }
        TextView textView = (TextView) convertView.findViewById(R.id.tv_title);
        textView.setText(collectionsList.get(position).getName());
        if (position != 0) {
            textView.setTextColor(mContext.getResources().getColor(android.R.color.white));
        } else {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            textView.setLayoutParams(params);
        }
        //
        if (!contextualMenuEnable) {
            mCollections.setSelect(false);
        }
        //1. Hide tick
        ImageView tick = (ImageView) convertView.findViewById(R.id.imv_magazine_tick);
        tick.setVisibility(View.GONE);
        if (contextualMenuEnable) {
            if (mCollections.isSelect()) {
                //Show tick
                if(position != 0) {
                    tick.setVisibility(View.VISIBLE);
                }
                else {
                    tick.setVisibility(View.GONE);
                }
            }
        }

        return convertView;
    }
}

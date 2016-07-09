package com.yo.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.yo.android.R;
import com.yo.android.model.Collections;
import com.yo.android.model.OwnMagazine;

import java.util.List;

/**
 * Created by creatives on 7/9/2016.
 */
public class MyCollectionsAdapter extends BaseAdapter {
    private Context mContext;
    private List<Collections> collectionsList;

    public MyCollectionsAdapter(final Context context, final List<Collections> collectionsList) {
        mContext = context;
        this.collectionsList = collectionsList;
    }

    public int getCount() {
        return collectionsList.size();
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
        if(collectionsList.get(position).getImage() != "") {
            Picasso.with(mContext)
                    .load(collectionsList.get(position).getImage())
                    .into(imageView);
        }
        TextView textView = (TextView) convertView.findViewById(R.id.tv_title);
        textView.setText(collectionsList.get(position).getName());
        if(position != 0) {
            textView.setTextColor(mContext.getResources().getColor(android.R.color.white));
        }

        return convertView;
    }
}

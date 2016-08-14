package com.yo.android.adapters;

import android.content.Context;
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
import com.yo.android.model.OwnMagazine;
import com.yo.android.widgets.SquareItemLinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by creatives on 7/19/2016.
 */
public class OthersMagazinesAdapter extends BaseAdapter {
    private Context mContext;
    private List<OwnMagazine> ownMagazineList;

    public OthersMagazinesAdapter(final Context context) {
        mContext = context;
        this.ownMagazineList = new ArrayList<>();
    }

    public void addItems(final List<OwnMagazine> ownMagazineList) {
        this.ownMagazineList.clear();
        this.ownMagazineList.addAll(ownMagazineList);
        notifyDataSetChanged();
    }

    public int getCount() {
        return ownMagazineList.size();
    }

    public OwnMagazine getItem(int position) {
        return ownMagazineList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.create_magazine_item, parent, false);
        }
        ImageView imageView = (ImageView) convertView.findViewById(R.id.img_magazine);
        SquareItemLinearLayout squareItemLinearLayout = (SquareItemLinearLayout) convertView.findViewById(R.id.sq_layout);


        TextView textView = (TextView) convertView.findViewById(R.id.tv_title);
        textView.setText(ownMagazineList.get(position).getName());

        TextView textViewDesc = (TextView) convertView.findViewById(R.id.tv_desc);
        textViewDesc.setText(ownMagazineList.get(position).getDescription());

            if (!TextUtils.isEmpty(ownMagazineList.get(position).getImage())) {
                Picasso.with(mContext)
                        .load(ownMagazineList.get(position).getImage())
                        .into(imageView);

            } else {
                Picasso.with(mContext)
                        .load(R.color.black)
                        .into(imageView);
            }
            textView.setTextColor(mContext.getResources().getColor(android.R.color.white));
            textViewDesc.setTextColor(mContext.getResources().getColor(android.R.color.white));
            squareItemLinearLayout.setBackgroundColor(mContext.getResources().getColor(android.R.color.black));
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.TOP | Gravity.LEFT;
            params.leftMargin = 10;
            textView.setLayoutParams(params);

        return convertView;
    }
}

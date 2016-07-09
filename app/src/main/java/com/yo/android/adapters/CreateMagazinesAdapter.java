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
import com.yo.android.model.OwnMagazine;

import java.util.List;

/**
 * Created by creatives on 7/9/2016.
 */
public class CreateMagazinesAdapter extends BaseAdapter {
    private Context mContext;
    private List<OwnMagazine> ownMagazineList;

    public CreateMagazinesAdapter(final Context context, final List<OwnMagazine> ownMagazineList) {
        mContext = context;
        this.ownMagazineList = ownMagazineList;
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
        if(ownMagazineList.get(position).getImage() != "") {
            Picasso.with(mContext)
                    .load(ownMagazineList.get(position).getImage())
                    .into(imageView);
        }
        TextView textView = (TextView) convertView.findViewById(R.id.tv_title);
        textView.setText(ownMagazineList.get(position).getTitle());

        return convertView;
    }
}

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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.yo.android.R;
import com.yo.android.model.OwnMagazine;
import com.yo.android.ui.BaseActivity;
import com.yo.android.util.Util;
import com.yo.android.widgets.SquareItemLinearLayout;

import java.util.ArrayList;
import java.util.List;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

//import com.squareup.picasso.Picasso;

/**
 * Created by creatives on 7/19/2016.
 */

/**
 * This adapter is used to display the Others Profile Magazines
 */
public class OthersMagazinesAdapter extends BaseAdapter {
    private Context mContext;
    private List<OwnMagazine> ownMagazineList;

    public OthersMagazinesAdapter(final Context context) {
        mContext = context;
        this.ownMagazineList = new ArrayList<>();
    }

    /**
     * Adds the items to the existing list
     * @param ownMagazineList
     */
    public void addItems(final List<OwnMagazine> ownMagazineList) {
        this.ownMagazineList.clear();
        this.ownMagazineList.addAll(ownMagazineList);
        if(mContext !=null) {
            if (!((BaseActivity) mContext).hasDestroyed()) {
                notifyDataSetChanged();
            }
        }
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

            if (!TextUtils.isEmpty(ownMagazineList.get(position).getImage())) { // Image url is not empty
                RequestOptions requestOptions = new RequestOptions()
                        .placeholder(Util.getMagazineBackdrop(mContext))
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.ALL);
                Glide.with(mContext)
                        .load(ownMagazineList.get(position).getImage())
                        .apply(requestOptions)
                        //.transition(withCrossFade())
                        .into(imageView);

            } else { // Image url is empty
                RequestOptions requestOptions = new RequestOptions()
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.ALL);
                if(ownMagazineList.get(position).getArticlesCount() == 0) {
                    Glide.with(mContext)
                            .load(R.drawable.ic_default_magazine)
                            //.transition(withCrossFade())
                            .into(imageView);
                } else {
                    Glide.with(mContext)
                            .load(Util.getMagazineBackdrop(mContext))
                            //.transition(withCrossFade())
                            .into(imageView);
                }
            }
            textView.setTextColor(mContext.getResources().getColor(android.R.color.white));
            textViewDesc.setTextColor(mContext.getResources().getColor(android.R.color.white));
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.TOP | Gravity.LEFT;
            params.leftMargin = 10;
            textView.setLayoutParams(params);

        return convertView;
    }

    /**
     * Updates the magazine
     * @param ownMagazine The OwnMagazine object
     * @param position The position to be updated in the list
     * @param isMagazineDeleted isMagazineDeleted or not
     */
    public void updateMagazine(OwnMagazine ownMagazine, int position, boolean isMagazineDeleted) {
        if(isMagazineDeleted) {
            ownMagazineList.remove(position);
        } else {
            ownMagazineList.remove(position);
            ownMagazineList.add(position, ownMagazine);
        }
        notifyDataSetChanged();
    }
}

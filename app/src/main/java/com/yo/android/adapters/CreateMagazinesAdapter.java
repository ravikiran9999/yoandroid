package com.yo.android.adapters;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

//import com.squareup.picasso.Picasso;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yo.android.R;
import com.yo.android.helpers.OwnMagazineViewHolder;
import com.yo.android.model.OwnMagazine;

/**
 * Created by creatives on 7/9/2016.
 */
public class CreateMagazinesAdapter extends AbstractBaseAdapter<OwnMagazine, OwnMagazineViewHolder> {

    public CreateMagazinesAdapter(Context context) {
        super(context);
    }

    @Override
    public int getLayoutId() {
        return R.layout.create_magazine_item;
    }

    @Override
    public OwnMagazineViewHolder getViewHolder(View convertView) {
        return new OwnMagazineViewHolder(convertView);
    }

    @Override
    public void bindView(final int position, final OwnMagazineViewHolder holder, final OwnMagazine item) {
        holder.getTextView().setText(item.getName());

        holder.getTextViewDesc().setText(item.getDescription());

        if(position != 0) {
            if(!TextUtils.isEmpty(item.getImage())) {

                Glide.with(mContext)
                        .load(item.getImage())
                        .placeholder(R.drawable.img_placeholder)
                        .fitCenter()
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate()
                        .into(holder.getImageView());

            } else {
                if(item.getArticlesCount() == 0) {
                    Glide.with(mContext)
                            //.load(R.color.black)
                            .load(R.drawable.ic_default_magazine)
                            .fitCenter()
                            .crossFade()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .dontAnimate()
                            .into(holder.getImageView());
                } else {
                    Glide.with(mContext)
                            //.load(R.color.black)
                            .load(R.drawable.img_placeholder)
                            .fitCenter()
                            .crossFade()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .dontAnimate()
                            .into(holder.getImageView());
                }

            }
            holder.getTextView().setTextColor(mContext.getResources().getColor(android.R.color.white));
            holder.getTextViewDesc().setTextColor(mContext.getResources().getColor(android.R.color.white));
            //holder.getSquareItemLinearLayout().setBackgroundColor(mContext.getResources().getColor(android.R.color.black));
            //holder.getSquareItemLinearLayout().setBackgroundResource(R.drawable.ic_default_magazine);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.TOP|Gravity.LEFT;
            params.leftMargin = 10;
            params.rightMargin = 10;
            holder.getTextView().setLayoutParams(params);
        } else if(position == 0 && !"+ New Magazine".equalsIgnoreCase(item.getName())) {
            if(!TextUtils.isEmpty(item.getImage())) {

                Glide.with(mContext)
                        .load(item.getImage())
                        .placeholder(R.drawable.img_placeholder)
                        .fitCenter()
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate()
                        .into(holder.getImageView());

            } else {
                if(item.getArticlesCount() == 0) {
                    Glide.with(mContext)
                            //.load(R.color.black)
                            .load(R.drawable.ic_default_magazine)
                            .fitCenter()
                            .crossFade()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .dontAnimate()
                            .into(holder.getImageView());
                } else {
                    Glide.with(mContext)
                            //.load(R.color.black)
                            .load(R.drawable.img_placeholder)
                            .fitCenter()
                            .crossFade()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .dontAnimate()
                            .into(holder.getImageView());
                }
            }
            holder.getTextView().setTextColor(mContext.getResources().getColor(android.R.color.white));
            holder.getTextViewDesc().setTextColor(mContext.getResources().getColor(android.R.color.white));
            //holder.getSquareItemLinearLayout().setBackgroundColor(mContext.getResources().getColor(android.R.color.black));
            //holder.getSquareItemLinearLayout().setBackgroundResource(R.drawable.ic_default_magazine);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.TOP|Gravity.LEFT;
            params.leftMargin = 10;
            params.rightMargin = 10;
            holder.getTextView().setLayoutParams(params);
        } else {

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            holder.getTextView().setTextColor(mContext.getResources().getColor(android.R.color.black));
            holder.getTextView().setLayoutParams(params);
            holder.getTextViewDesc().setTextColor(mContext.getResources().getColor(android.R.color.black));
            holder.getImageView().setImageDrawable(new ColorDrawable(mContext.getResources().getColor(R.color.grey_divider)));
            holder.getSquareItemLinearLayout().setBackgroundColor(mContext.getResources().getColor(R.color.grey_divider));
        }
    }

    @Override
    protected boolean hasData(OwnMagazine ownMagazine, String key) {
        if (ownMagazine.getName() != null && ownMagazine.getDescription() != null) {
            if (containsValue(ownMagazine.getName().toLowerCase(), key)
                    || containsValue(ownMagazine.getDescription().toLowerCase(), key)) {
                return true;
            }
        }
        return super.hasData(ownMagazine, key);
    }

    private boolean containsValue(String str, String key) {
        return str != null && str.toLowerCase().contains(key);
    }

}

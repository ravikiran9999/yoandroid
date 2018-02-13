package com.yo.android.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.yo.android.R;
import com.yo.android.helpers.MyCollectionsViewHolder;
import com.yo.android.model.Collections;
import com.yo.android.ui.BitmapScaler;
import com.yo.android.ui.DeviceDimensionsHelper;
import com.yo.android.ui.NewImageRenderTask;

import java.util.ArrayList;
import java.util.List;

//import com.squareup.picasso.Picasso;

/**
 * Created by creatives on 7/9/2016.
 */

/**
 * This adapter is used to show the My Collections screen
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
    public void bindView(int position, final MyCollectionsViewHolder holder, final Collections item) {

        if (position == 0 && "Follow more topics".equalsIgnoreCase(item.getName())) { // First position and is the Follow more topics text

            Glide.with(mContext)
                    .load(R.color.grey_divider)
                    .fitCenter()
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .dontAnimate()
                    .into(holder.getImageView());

        } else if (!TextUtils.isEmpty(item.getImage())) { // Image url is not empty
            //new NewImageRenderTask(mContext,item.getImage(),holder.getImageView()).execute();
            Glide.clear(holder.getImageView());
            Glide.with(mContext)
                    //.load(item.getImage())
                    .load(item.getS3_image_filename())
                    .asBitmap()
                    .placeholder(R.drawable.magazine_backdrop)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .dontAnimate()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            int screenWidth = DeviceDimensionsHelper.getDisplayWidth(mContext);
                            Bitmap bmp = null;
                            if (resource != null) {
                                try {
                                bmp = BitmapScaler.scaleToFitWidth(resource, screenWidth);
                                Glide.with(mContext)
                                        .load(item.getImage())
                                        .override(bmp.getWidth(), bmp.getHeight())
                                        .placeholder(R.drawable.magazine_backdrop)
                                        .crossFade()
                                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                        .dontAnimate()
                                        .into(holder.getImageView());
                                }finally {
                                    if(bmp != null) {
                                        bmp.recycle();
                                        bmp = null;
                                    }
                                }
                            }
                        }
                    });
        } else { // Not first position and not having image url
            Glide.clear(holder.getImageView());
            if(item.getArticlesCount() == 0) {
                Glide.with(mContext)
                        .load(R.drawable.magazine_backdrop)
                        .fitCenter()
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .dontAnimate()
                        .into(holder.getImageView());
            } else {
                Glide.clear(holder.getImageView());
                Glide.with(mContext)
                        .load(R.drawable.magazine_backdrop)
                        .fitCenter()
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .dontAnimate()
                        .into(holder.getImageView());
            }
        }
        holder.getTextView().setText(item.getName());
        if (position != 0) {
            holder.getTextView().setTextColor(mContext.getResources().getColor(android.R.color.white));
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.LEFT | Gravity.TOP;
            params.leftMargin = 10;
            params.rightMargin = 10;
            holder.getTextView().setLayoutParams(params);

        } else if(position == 0 && !"Follow more topics".equalsIgnoreCase(item.getName())) {
            holder.getTextView().setTextColor(mContext.getResources().getColor(android.R.color.white));
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.LEFT | Gravity.TOP;
            params.leftMargin = 10;
            params.rightMargin = 10;
            holder.getTextView().setLayoutParams(params);
        } else {
            holder.getTextView().setTextColor(mContext.getResources().getColor(android.R.color.black));
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            holder.getTextView().setLayoutParams(params);
        }
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

    /**
     * Gets the selected items in the list
     * @return The selected items
     */
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

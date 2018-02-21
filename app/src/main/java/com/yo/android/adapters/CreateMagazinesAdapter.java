package com.yo.android.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.yo.android.R;
import com.yo.android.helpers.OwnMagazineViewHolder;
import com.yo.android.model.OwnMagazine;
import com.yo.android.ui.BitmapScaler;
import com.yo.android.ui.DeviceDimensionsHelper;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by creatives on 7/9/2016.
 */

/**
 * This adapter is used to display the My Magazines
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

        if (position != 0) { // Other than the first position
            if (!TextUtils.isEmpty(item.getImage())) { // Image url is not null

                RequestOptions myOptions = new RequestOptions()
                        .placeholder(R.drawable.magazine_backdrop)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate();
                Glide.with(mContext)
                        .asBitmap()
                        .load(item.getImage())
                        .apply(myOptions)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                int screenWidth = DeviceDimensionsHelper.getDisplayWidth(mContext);
                                Bitmap bmp = null;
                                if (resource != null) {
                                    try {
                                        bmp = BitmapScaler.scaleToFitWidth(resource, screenWidth);
                                        RequestOptions myOptions = new RequestOptions()
                                                .override(bmp.getWidth(), bmp.getHeight())
                                                .placeholder(R.drawable.magazine_backdrop)
                                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                                .dontAnimate();

                                        Glide.with(mContext)
                                                .load(item.getImage())
                                                .apply(myOptions)
                                                .transition(withCrossFade())
                                                .into(holder.getImageView());
                                    } finally {
                                        if (bmp != null) {
                                            bmp.recycle();
                                            bmp = null;
                                        }
                                    }
                                }
                            }
                        });

            } else {
                RequestOptions requestOptions = new RequestOptions()
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate();
                if (item.getArticlesCount() == 0) { // Image url is null and no articles are present
                    Glide.with(mContext)
                            .load(R.drawable.ic_default_magazine)
                            .apply(requestOptions)
                            .transition(withCrossFade())
                            .into(holder.getImageView());
                } else { // Image url is null and articles are present
                    Glide.with(mContext)
                            .load(R.drawable.magazine_backdrop)
                            .apply(requestOptions)
                            .transition(withCrossFade())
                            .into(holder.getImageView());
                }

            }
            holder.getTextView().setTextColor(mContext.getResources().getColor(android.R.color.white));
            holder.getTextViewDesc().setTextColor(mContext.getResources().getColor(android.R.color.white));
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.TOP | Gravity.LEFT;
            params.leftMargin = 10;
            params.rightMargin = 10;
            holder.getTextView().setLayoutParams(params);
        } else if (position == 0 && !"+ New Magazine".equalsIgnoreCase(item.getName())) { // First position but not the New Magazine text(applies when searching)
            if (!TextUtils.isEmpty(item.getImage())) { // Image url is not null
                RequestOptions requestOptions = new RequestOptions()
                        .placeholder(R.drawable.magazine_backdrop)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate();
                Glide.with(mContext)
                        .asBitmap()
                        .load(item.getImage())
                        .apply(requestOptions)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                int screenWidth = DeviceDimensionsHelper.getDisplayWidth(mContext);
                                Bitmap bmp = null;

                                if (resource != null) {
                                    try {
                                        bmp = BitmapScaler.scaleToFitWidth(resource, screenWidth);
                                        RequestOptions myOptions = new RequestOptions()
                                                .override(bmp.getWidth(), bmp.getHeight())
                                                .placeholder(R.drawable.magazine_backdrop)
                                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                                .dontAnimate();

                                        Glide.with(mContext)
                                                .load(item.getImage())
                                                .apply(myOptions)
                                                .transition(withCrossFade())
                                                .into(holder.getImageView());
                                    } finally {
                                        if (bmp != null) {
                                            bmp.recycle();
                                            bmp = null;
                                        }
                                    }
                                }
                            }
                        });

            } else {
                RequestOptions myOptions = new RequestOptions()
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate();

                if (item.getArticlesCount() == 0) { // Image url is null and no articles are present
                    Glide.with(mContext)
                            .load(R.drawable.ic_default_magazine)
                            .apply(myOptions)
                            .transition(withCrossFade())
                            .into(holder.getImageView());
                } else { // Image url is null and articles are present
                    Glide.with(mContext)
                            .load(R.drawable.magazine_backdrop)
                            .transition(withCrossFade())
                            .into(holder.getImageView());
                }
            }
            holder.getTextView().setTextColor(mContext.getResources().getColor(android.R.color.white));
            holder.getTextViewDesc().setTextColor(mContext.getResources().getColor(android.R.color.white));
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.TOP | Gravity.LEFT;
            params.leftMargin = 10;
            params.rightMargin = 10;
            holder.getTextView().setLayoutParams(params);
        } else { // First position and is New Magazine text

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

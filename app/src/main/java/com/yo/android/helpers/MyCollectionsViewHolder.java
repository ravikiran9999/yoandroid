package com.yo.android.helpers;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractViewHolder;

/**
 * Created by creatives on 8/4/2016.
 */
public class MyCollectionsViewHolder extends AbstractViewHolder {

    private ImageView imageView;
    private TextView textView;
    private ImageView tick;

    public MyCollectionsViewHolder(View view) {
        super(view);
        imageView = (ImageView) view.findViewById(R.id.img_magazine);
        textView = (TextView) view.findViewById(R.id.tv_title);
        tick = (ImageView) view.findViewById(R.id.imv_magazine_tick);
    }

    public TextView getTextView() {
        return textView;
    }

    public void setTextView(TextView textView) {
        this.textView = textView;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public ImageView getTick() {
        return tick;
    }

    public void setTick(ImageView tick) {
        this.tick = tick;
    }
}

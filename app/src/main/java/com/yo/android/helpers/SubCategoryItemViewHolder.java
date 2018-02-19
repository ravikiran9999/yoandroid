package com.yo.android.helpers;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.yo.android.R;
import com.yo.android.adapters.YoViewHolder;
import com.yo.android.model.Topics;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class SubCategoryItemViewHolder extends YoViewHolder {

    @Bind(R.id.img_magazine)
    ImageView tile;
    @Bind(R.id.tv_title)
    TextView subCategoryItem;
    @Bind(R.id.checkbox)
    CheckBox checkBox;

    private Context mContext;

    public SubCategoryItemViewHolder(Context context, View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        mContext = context;
    }

    public CheckBox getCheckBox() {
        return checkBox;
    }

    @Override
    public void bindData(Object data) {
        Topics item = (Topics) data;
        subCategoryItem.setText(item.getName());
        loadImage(item.getImage());

        if (item.isSelected()) {
            //Show tick
            checkBox.setChecked(true);
        } else {
            checkBox.setChecked(false);
        }
    }

    private void loadImage(String imageUrl) {
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.magazine_backdrop)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate();
        Glide.with(mContext)
                .load(imageUrl)
                //.override(bmp.getWidth(), bmp.getHeight())
                .apply(requestOptions)
                .transition(withCrossFade())
                .into(tile);
    }
}

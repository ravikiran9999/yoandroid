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
import com.yo.android.model.Categories;

import butterknife.Bind;
import butterknife.ButterKnife;


public class NewSuggestionsViewHolder extends YoViewHolder {

    @Bind(R.id.img_magazine)
    ImageView imageView;
    @Bind(R.id.tv_title)
    TextView topic_textView;
    @Bind(R.id.checkbox)
    CheckBox checkBox;

    private Context mContext;

    public NewSuggestionsViewHolder(Context context, View view) {
        super(view);
        ButterKnife.bind(this, view);
        mContext = context;
    }

    public CheckBox getCheckBox() {
        return checkBox;
    }

    @Override
    public void bindData(Object data) {
        Categories categories = (Categories) data;
        if (categories != null && categories.getTags() != null && categories.getTags().size() > 0 && categories.getTags().get(0) != null) {
            topic_textView.setText(categories.getTags().get(0).getName());
            loadImage(categories.getTags().get(0).getImage());

            if (categories.getTags().get(0).isSelected()) {
                //Show tick
                checkBox.setChecked(true);
            } else {
                checkBox.setChecked(false);
            }
        }
    }

    private void loadImage(String imageUrl) {
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.magazine_backdrop)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate();
        Glide.with(mContext)
                .load(imageUrl)
                .apply(requestOptions)
                .into(imageView);
    }
}

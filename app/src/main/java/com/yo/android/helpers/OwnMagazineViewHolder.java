package com.yo.android.helpers;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractViewHolder;
import com.yo.android.widgets.SquareItemLinearLayout;

/**
 * Created by creatives on 8/4/2016.
 */
public class OwnMagazineViewHolder extends AbstractViewHolder {

    private ImageView imageView;
    private SquareItemLinearLayout squareItemLinearLayout;
    private TextView textView;
    private TextView textViewDesc;

    public OwnMagazineViewHolder(View view) {
        super(view);
        imageView = (ImageView) view.findViewById(R.id.img_magazine);
        squareItemLinearLayout = (SquareItemLinearLayout) view.findViewById(R.id.sq_layout);
        textView = (TextView) view.findViewById(R.id.tv_title);
        textViewDesc = (TextView) view.findViewById(R.id.tv_desc);
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public TextView getTextView() {
        return textView;
    }

    public void setTextView(TextView textView) {
        this.textView = textView;
    }

    public SquareItemLinearLayout getSquareItemLinearLayout() {
        return squareItemLinearLayout;
    }

    public void setSquareItemLinearLayout(SquareItemLinearLayout squareItemLinearLayout) {
        this.squareItemLinearLayout = squareItemLinearLayout;
    }

    public TextView getTextViewDesc() {
        return textViewDesc;
    }

    public void setTextViewDesc(TextView textViewDesc) {
        this.textViewDesc = textViewDesc;
    }
}

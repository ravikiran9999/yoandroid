package com.yo.android.adapters;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
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
import com.yo.android.helpers.OwnMagazineViewHolder;
import com.yo.android.model.OwnMagazine;
import com.yo.android.widgets.SquareItemLinearLayout;

import java.util.ArrayList;
import java.util.List;

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
                Picasso.with(mContext)
                        .load(item.getImage())
                        .into(holder.getImageView());

            }
            else {
                Picasso.with(mContext)
                        .load(R.color.black)
                        .into(holder.getImageView());
            }
            holder.getTextView().setTextColor(mContext.getResources().getColor(android.R.color.white));
            holder.getTextViewDesc().setTextColor(mContext.getResources().getColor(android.R.color.white));
            holder.getSquareItemLinearLayout().setBackgroundColor(mContext.getResources().getColor(android.R.color.black));
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.TOP|Gravity.LEFT;
            params.leftMargin = 10;
            holder.getTextView().setLayoutParams(params);
        }
        else if(position == 0 && !item.getName().equalsIgnoreCase("+ New Magazine")) {
            if(!TextUtils.isEmpty(item.getImage())) {
                Picasso.with(mContext)
                        .load(item.getImage())
                        .into(holder.getImageView());

            }
            else {
                Picasso.with(mContext)
                        .load(R.color.black)
                        .into(holder.getImageView());
            }
            holder.getTextView().setTextColor(mContext.getResources().getColor(android.R.color.white));
            holder.getTextViewDesc().setTextColor(mContext.getResources().getColor(android.R.color.white));
            holder.getSquareItemLinearLayout().setBackgroundColor(mContext.getResources().getColor(android.R.color.black));
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.TOP|Gravity.LEFT;
            params.leftMargin = 10;
            holder.getTextView().setLayoutParams(params);
        }

        else {

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

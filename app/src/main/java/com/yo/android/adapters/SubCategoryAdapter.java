package com.yo.android.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yo.android.R;
import com.yo.android.helpers.DenominationItemViewHolder;
import com.yo.android.helpers.SubCategoryItemViewHolder;
import com.yo.android.model.Topics;
import com.yo.android.model.denominations.Denominations;

import java.util.ArrayList;

public class SubCategoryAdapter extends RecyclerView.Adapter<YoViewHolder> {

    private ArrayList<Topics> mData;
    private LayoutInflater mInflater;
    private TopicsItemListener topicsItemListener;
    private Context mContext;

    public interface TopicsItemListener {
        void onItemSelected(Topics topics);
    }

    public SubCategoryAdapter(Context context, ArrayList<Topics> data) {
        mData = data;
        mInflater = LayoutInflater.from(context);
        mContext = context;
    }

    @Override
    public YoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SubCategoryItemViewHolder(mContext, mInflater.inflate(R.layout.sub_categories_item, parent, false));
    }

    @Override
    public void onBindViewHolder(YoViewHolder holder, int position) {
        final SubCategoryItemViewHolder subCategoryItemViewHolder = (SubCategoryItemViewHolder) holder;
        subCategoryItemViewHolder.bindData(mData.get(position));
        subCategoryItemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = subCategoryItemViewHolder.getAdapterPosition();
                if (position < 0) {
                    return;
                }
                if (topicsItemListener != null) {
                    Topics item = mData.get(position);
                    topicsItemListener.onItemSelected(item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setTopicsItemListener(TopicsItemListener listener) {
        topicsItemListener = listener;
    }
}

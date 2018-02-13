package com.yo.android.helpers;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.yo.android.R;
import com.yo.android.adapters.SubCategoryAdapter;
import com.yo.android.adapters.YoViewHolder;
import com.yo.android.model.Topics;
import com.yo.android.ui.NewFollowMoreTopicsActivity;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SubCategoryViewHolder extends YoViewHolder implements SubCategoryAdapter.TopicsItemListener {

    @Bind(R.id.inner_view)
    RecyclerView recyclerView;

    private SubCategoryAdapter subCategoryAdapter;
    private ArrayList<String> mSelectedTopics;
    private Context mContext;

    public SubCategoryViewHolder(Context context, View itemView, ArrayList<String> selectedTopics) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        mContext = context;
        mSelectedTopics = selectedTopics;
    }

    @Override
    public void bindData(Object data) {
        ArrayList<Topics> topicsArrayList = (ArrayList<Topics>) data;

        subCategoryAdapter = new SubCategoryAdapter(mContext, topicsArrayList);
        subCategoryAdapter.setTopicsItemListener(this);
        recyclerView.setAdapter(subCategoryAdapter);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new GridLayoutManager(mContext, 2));
    }

    @Override
    public void onItemSelected(Topics topics) {
        if(!topics.isSelected()) {
            topics.setSelected(true);
            mSelectedTopics.add(topics.getId());
        } else {
            topics.setSelected(false);
            mSelectedTopics.remove(topics.getId());
        }
        if(mContext instanceof NewFollowMoreTopicsActivity) {
            ((NewFollowMoreTopicsActivity) mContext).getCategoriesAdapter().notifyDataSetChanged();
        }
        subCategoryAdapter.notifyDataSetChanged();
    }

    public SubCategoryAdapter getSubCategoryAdapter() {
        return subCategoryAdapter;
    }
}

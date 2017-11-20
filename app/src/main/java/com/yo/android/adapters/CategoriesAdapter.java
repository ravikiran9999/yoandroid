package com.yo.android.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.yo.android.R;
import com.yo.android.helpers.CategoryViewHolder;
import com.yo.android.helpers.EmptyViewHolder;
import com.yo.android.helpers.SubCategoryViewHolder;
import com.yo.android.model.Topics;
import com.yo.android.ui.followmoretopics.CategoriesAccordionSection;
import com.yo.android.widgets.expandablerecycler.YoExpandableRecyclerAdapter;

import java.util.ArrayList;

public class CategoriesAdapter extends YoExpandableRecyclerAdapter {

    private Context context;
    private ArrayList<String> selectedTopics;
    SubCategoryViewHolder subCategoryViewHolder;


    public CategoriesAdapter(Context context, ArrayList<String> selectedTopics) {
        this.context = context;
        this.selectedTopics = selectedTopics;
    }

    @Override
    protected int layout(int position) {
        Object item = data.get(position);
        if (item instanceof CategoriesAccordionSection) {
            return R.layout.categories;
        } else if (item instanceof ArrayList) {
            return R.layout.sub_categories;
        } else {
            return R.layout.empty_view;
        }
    }

    @NonNull
    @Override
    protected YoViewHolder viewHolder(int layout, @NonNull View view) {

        switch (layout) {
            case R.layout.categories:
                return new CategoryViewHolder(this, view, selectedTopics);
            case R.layout.sub_categories:
                subCategoryViewHolder = new SubCategoryViewHolder(context, view, selectedTopics);
                return subCategoryViewHolder;
            default:
                return new EmptyViewHolder(view);
        }
    }

    public void subCategoryAdapter() {
        if(subCategoryViewHolder != null)
        subCategoryViewHolder.getSubCategoryAdapter().notifyDataSetChanged();
    }
}

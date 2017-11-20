package com.yo.android.helpers;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.CategoriesAdapter;
import com.yo.android.adapters.YoViewHolder;
import com.yo.android.sectionheaders.Section;
import com.yo.android.ui.followmoretopics.CategoriesAccordionSection;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CategoryViewHolder extends YoViewHolder {
    @Bind(R.id.title)
    TextView title;
    @Bind(R.id.checkbox)
    CheckBox checkBox;

    private CategoriesAdapter categoriesAdapter;
    private ArrayList<String> mSelectedTopics;

    public CategoryViewHolder(CategoriesAdapter categoriesAdapter, View itemView, ArrayList<String> selectedTopics) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.categoriesAdapter = categoriesAdapter;
        mSelectedTopics = selectedTopics;
    }

    @Override
    public void bindData(Object data) {
        final CategoriesAccordionSection accordionSection = (CategoriesAccordionSection) data;

        title.setText(accordionSection.getName());
        checkBox.setChecked(accordionSection.checkAllTopicsSelected());
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                if (cb.isChecked()) {
                    accordionSection.checkAllTopics(mSelectedTopics);
                } else {
                    accordionSection.unCheckAllTopics(mSelectedTopics);
                }
                categoriesAdapter.subCategoryAdapter();
            }
        });

    }
}

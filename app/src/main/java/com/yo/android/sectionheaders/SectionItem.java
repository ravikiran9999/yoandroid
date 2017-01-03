package com.yo.android.sectionheaders;

import com.cunoraz.tagview.Tag;
import com.cunoraz.tagview.TagView;
import com.yo.android.model.Topics;

import java.util.List;

/**
 * Created by creatives on 12/20/2016.
 */
public class SectionItem {
    /// <summary>
    /// Gets or sets the category item.
    /// </summary>
    /// <value>The category item.</value>
    private TagView view;

    /// <summary>
    /// Gets or sets the category.
    /// </summary>
    /// <value>The category.</value>
    private String category;

    public TagView getCategoryItem() {
        return view;
    }

    public void setCategoryItem(TagView categoryItem) {
        this.view = categoryItem;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}

package com.yo.android.sectionheaders;

import android.app.Activity;
import android.widget.ListView;

import com.cunoraz.tagview.Tag;
import com.cunoraz.tagview.TagView;
import com.yo.android.R;
import com.yo.android.adapters.CategoriesAdapter;
import com.yo.android.model.Categories;

import java.util.ArrayList;
import java.util.List;

// TODO delete it

/**
 * Created by creatives on 12/20/2016.
 */
public class CategorizedList {

    /// <summary>
    /// The context.
    /// </summary>
    private Activity context;
    /// <summary>
    /// The list view.
    /// </summary>
    private ListView listView;
    /// <summary>
    /// The section items with headers.
    /// </summary>
    private List<Section> sectionItemsWithHeaders;



    /// <summary>
    /// The category adapter.
    /// </summary>
    CategoryAdapter categoryAdapter;

    CategoriesAdapter categoriesAdapter;



    private ArrayList<Tag> initialTags;

    private List<Categories> topicsList;

    /// <summary>
    /// Initializes a new instance of the <see cref="SectionHeadersEx.CategorizedList"/> class.
    /// </summary>
    /// <param name="context">Context.</param>
    /// <param name="listView">List view.</param>
    public CategorizedList(Activity context, ListView listView, ArrayList<Tag> initialTags, List<Categories> topicsList) {
        this.context = context;
        this.listView = listView;
        sectionItemsWithHeaders = new ArrayList<Section>();
        this.initialTags = initialTags;
        this.topicsList = topicsList;
    }

    /// <summary>
    /// Creates the section items.
    /// </summary>
    /// <param name="sectionItems">Section items.</param>
    /// <param name="category">Category.</param>
    public void CreateSectionItems(TagView sectionItems, String category) {
        List<SectionItem> sectionItemsList = new ArrayList<SectionItem>();

        SectionItem sectionItem = new SectionItem();
        sectionItem.setCategoryItem(sectionItems);
        sectionItemsList.add(sectionItem);

        SetItemsWithHeaders(sectionItemsList, category);

    }

    /// <summary>
    /// Sets the items with headers.
    /// </summary>
    /// <param name="sectionItemsList">Section items list.</param>
    /// <param name="header">Header.</param>
    void SetItemsWithHeaders(List<SectionItem> sectionItemsList, String header) {
        SectionItem sectionItem = new SectionItem();
        sectionItem.setCategory(header);
        Section section = new Section();
        section.setSectionItem(sectionItem);
        section.setLayoutId(R.layout.section);
        sectionItemsWithHeaders.add(section);

        for (SectionItem sectItem : sectionItemsList) {
            Section sectionHeader = new Section();
            sectionHeader.setSectionItem(sectItem);
            sectionHeader.setLayoutId(R.layout.section_list_item);
            sectionHeader.setSectionHeader(header);
            sectionItemsWithHeaders.add(sectionHeader);
        }

    }

    /// <summary>
    /// Loads the category adapter.
    /// </summary>
    /// <returns>The category adapter.</returns>
    public void loadCategoryAdapter() {
        categoryAdapter = new CategoryAdapter(context, sectionItemsWithHeaders);
        listView.setAdapter(categoryAdapter);
    }

    /*public void loadCategoriesAdapter() {
        categoriesAdapter = new CategoriesAdapter(context);
        categoriesAdapter.setData(new ArrayList<Object>(sectionItemsWithHeaders));
        listView.setAdapter(categoryAdapter);

    }*/

    public CategoryAdapter getCategoryAdapter() {
        return categoryAdapter;
    }

    public void setAdapterToListView() {
        listView.setAdapter(categoryAdapter);
    }
}

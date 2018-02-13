package com.yo.android.sectionheaders;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ListView;

import com.cunoraz.tagview.Tag;
import com.cunoraz.tagview.TagView;
import com.yo.android.R;
import com.yo.android.adapters.CategoriesAdapter;
import com.yo.android.model.Categories;

import java.util.ArrayList;
import java.util.List;

public class NewCategorizedList {

    /// <summary>
    /// The context.
    /// </summary>
    private Activity context;
    /// <summary>
    /// The list view.
    /// </summary>
    private RecyclerView recyclerView;
    /// <summary>
    /// The section items with headers.
    /// </summary>
    private List<Section> sectionItemsWithHeaders;

    CategoriesAdapter categoriesAdapter;



    private ArrayList<Tag> initialTags;

    private List<Categories> topicsList;

    /// <summary>
    /// Initializes a new instance of the <see cref="SectionHeadersEx.CategorizedList"/> class.
    /// </summary>
    /// <param name="context">Context.</param>
    /// <param name="listView">List view.</param>
    public NewCategorizedList(Activity context, RecyclerView listView, ArrayList<Tag> initialTags, List<Categories> topicsList) {
        this.context = context;
        this.recyclerView = listView;
        sectionItemsWithHeaders = new ArrayList<Section>();
        this.initialTags = initialTags;
        this.topicsList = topicsList;
    }

    /// <summary>
    /// Creates the section items.
    /// </summary>
    /// <param name="sectionItems">Section items.</param>
    /// <param name="category">Category.</param>
    public void createSectionItems(TagView sectionItems, String category) {
        List<SectionItem> sectionItemsList = new ArrayList<SectionItem>();

        SectionItem sectionItem = new SectionItem();
        sectionItem.setCategoryItem(sectionItems);
        sectionItemsList.add(sectionItem);

        setItemsWithHeaders(sectionItemsList, category);

    }

    /// <summary>
    /// Sets the items with headers.
    /// </summary>
    /// <param name="sectionItemsList">Section items list.</param>
    /// <param name="header">Header.</param>
    private void setItemsWithHeaders(List<SectionItem> sectionItemsList, String header) {
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

   /* public void loadCategoriesAdapter() {
        categoriesAdapter = new CategoriesAdapter(context);
        categoriesAdapter.setData(new ArrayList<Object>(sectionItemsWithHeaders));
        categoriesAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(categoriesAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

    }*/

    public CategoriesAdapter getCategoriesAdapter() {
        return categoriesAdapter;
    }

}

package com.yo.android.sectionheaders;

import android.app.Activity;
import android.content.res.Resources;
import android.widget.ListView;

import com.cunoraz.tagview.Tag;
import com.cunoraz.tagview.TagView;
import com.yo.android.R;
import com.yo.android.model.Categories;
import com.yo.android.model.Topics;
import com.yo.android.ui.FollowMoreTopicsActivity;

import java.util.ArrayList;
import java.util.List;

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

    private ArrayList<Tag> initialTags;

    private List<Categories> topicsList;

    /// <summary>
    /// Initializes a new instance of the <see cref="SectionHeadersEx.CategorizedList"/> class.
    /// </summary>
    /// <param name="context">Context.</param>
    /// <param name="listView">List view.</param>
    public CategorizedList (Activity context, ListView listView, ArrayList<Tag> initialTags, List<Categories> topicsList)
    {
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
    public void CreateSectionItems (TagView sectionItems, String category)
    {
        List<SectionItem> sectionItemsList = new ArrayList<SectionItem> ();

        //for(Tag sectItems : sectionItems) {
        SectionItem sectionItem = new SectionItem ();
        //sectionItem.categoryItem = sectItems;
            sectionItem.setCategoryItem(sectionItems);
        sectionItemsList.add(sectionItem);
    //}

        //((FollowMoreTopicsActivity)context).callTagLoader(sectionItems);

        SetItemsWithHeaders(sectionItemsList, category);

    }

    /// <summary>
    /// Sets the items with headers.
    /// </summary>
    /// <param name="sectionItemsList">Section items list.</param>
    /// <param name="header">Header.</param>
    void SetItemsWithHeaders (List<SectionItem> sectionItemsList, String header)
    {
        SectionItem sectionItem = new SectionItem ();
        //sectionItem.category = header;
        sectionItem.setCategory(header);
        Section section = new Section ();
        //section.sectionItem = sectionItem;
        section.setSectionItem(sectionItem);
        //section.layoutId = Resource.Layout.section;
        //section.layoutId = R.layout.section;
        section.setLayoutId(R.layout.section);
        sectionItemsWithHeaders.add(section);

        for (SectionItem sectItem : sectionItemsList) {
            Section sectionHeader = new Section ();
        /*sectionHeader.sectionItem = sectItem;
        sectionHeader.layoutId = R.layout.section_list_item;
        sectionHeader.sectionHeader = header;*/
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
    public CategoryAdapter LoadCategoryAdapter ()
    {
        //categoryAdapter = new CategoryAdapter (context, sectionItemsWithHeaders, initialTags, this,  topicsList);
        categoryAdapter = new CategoryAdapter (context, sectionItemsWithHeaders);
        //listView.Adapter = categoryAdapter;
        listView.setAdapter(categoryAdapter);
        return categoryAdapter;

    }

    public void setAdapterToListView() {
        listView.setAdapter(categoryAdapter);
    }
}

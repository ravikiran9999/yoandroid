package com.yo.android.sectionheaders;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cunoraz.tagview.Tag;
import com.cunoraz.tagview.TagView;
import com.yo.android.R;
import com.yo.android.helpers.TagLoader;
import com.yo.android.helpers.TagSelected;
import com.yo.android.model.Topics;
import com.yo.android.ui.FollowMoreTopicsActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by creatives on 12/20/2016.
 */
public class CategoryAdapter extends BaseAdapter {


    /// <summary>
    /// The list items with headers.
    /// </summary>
    public List<Section> listItemsWithHeaders;
    /// <summary>
    /// The context.
    /// </summary>
    private Activity context;
    /// <summary>
    /// The layout inflater.
    /// </summary>
    private LayoutInflater layoutInflater;

    /*private ArrayList<Tag> initialTags;

    private CategorizedList categorisedList;

    private List<Topics> topicsList;*/

    /// <summary>
    /// Initializes a new instance of the <see cref="SectionHeadersEx.CategoryAdapter"/> class.
    /// </summary>
    /// <param name="context">Context.</param>
    /// <param name="listItemsWithHeaders">Books items with headers.</param>
    public CategoryAdapter(Activity context, List<Section> listItemsWithHeaders) {
        this.context = context;

        this.listItemsWithHeaders = listItemsWithHeaders;

        Log.d("CategoryAdapter", "listItemsWithHeaders is " + listItemsWithHeaders.size());

        /*this.initialTags = initialTags;

        this.categorisedList = categorisedList;

        this.topicsList = topicsList;*/

        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    /// <param name="position">The position of the item within the adapter's data set whose row id we want.</param>
    /// <summary>
    /// Get the row id associated with the specified position in the list.
    /// </summary>
    /// <returns>Row Id.</returns>
    @Override
    public long getItemId(int position) {
        return position;
    }

    /// <summary>
    /// Gets the item at position.
    /// </summary>
    /// <returns>The item at position.</returns>
    /// <param name="position">Position.</param>
    public Section GetItemAtPosition(int position) {
        return listItemsWithHeaders.get(position);
    }

    /// <summary>
    /// Gets the item list.
    /// </summary>
    /// <returns>The item list.</returns>
    public List<Section> GetItemList() {
        return listItemsWithHeaders;
    }

    /// <summary>
    /// Gets the <see cref="SectionHeadersEx.CategoryAdapter"/> at the specified index.
    /// </summary>
    /// <param name="index">Index.</param>
    /*@Override
    public Section this [int index] {
        get { return listItemsWithHeaders [index]; }
    }*/
    @Override
    public Section getItem(int position) {
        return listItemsWithHeaders.get(position);
    }

    /// <summary>
    /// How many items are in the data set represented by this Adapter.
    /// </summary>
    /// <value>The count of the number of items</value>
    @Override
    public int getCount() {
        return listItemsWithHeaders.size();
    }

    /// <param name="position">The position of the item within the adapter's data set of the item whose view
    ///  we want.</param>
    /// <summary>
    /// Gets the view.
    /// </summary>
    /// <returns>The view.</returns>
    /// <param name="convertView">Convert view.</param>
    /// <param name="parent">Parent.</param>
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView; // re-use an existing view, if one is available

        Section selectedItem = getItem(position);

        //if (selectedItem.layoutId == R.layout.section) { // List item is a Section
        if (selectedItem.getLayoutId() == R.layout.section) { // List item is a Section
            view = layoutInflater.inflate(R.layout.section, null);
            SectionItem sectionItem = selectedItem.getSectionItem();
            TextView title = (TextView) view.findViewById(R.id.title);
            title.setText(sectionItem.getCategory());
            //} else if (selectedItem.layoutId == R.layout.section_list_item) { // List item is a Section Item
        } else if (selectedItem.getLayoutId() == R.layout.section_list_item) { // List item is a Section Item
            final TagView tagView = selectedItem.getSectionItem().getCategoryItem();
            tagView.setOnTagClickListener(new TagView.OnTagClickListener() {
                @Override
                public void onTagClick(Tag tag, int position) {
                    Log.d("TagClick", "Tag is clicked");
                    ((FollowMoreTopicsActivity) context).onClickingTag(tag, position);

                }
            });
            return selectedItem.getSectionItem().getCategoryItem();
        }

        return view;
    }

    /// <param name="position">Index of the item</param>
    /// <summary>
    /// Returns true if the item at the specified position is not a separator.
    /// </summary>
    /// <returns>true if the item is not a separator, false otherwise.</returns>
    @Override
    public boolean isEnabled(int position) {

        //return getItem(position).getLayoutId() != R.layout.section;
        return false;
    }

    /*    private void displayTags(TagView tagGroup) {
            topicsList = new ArrayList<Topics>();
            List<Topics> dummyTopicsList = new ArrayList<>(topicsList);
            tagGroup.setVisibility(View.GONE);
            initialTags.clear();
            topicsList.clear();
            topicsList.addAll(dummyTopicsList);
            synchronized (initialTags) {
                for (Topics topic : topicsList) {
                    final TagSelected tag = ((FollowMoreTopicsActivity)context).prepareTag(topic);
                    initialTags.add(tag);
                }
            }

            ArrayList<Tag> tagSelected = initialTags;

            tagGroup.addTags(tagSelected);
            if (tagGroup != null) {
                tagGroup.setVisibility(View.VISIBLE);
            }
          //  categorisedList.CreateSectionItems(tagSelected, "Category 1");
          //  categorisedList.CreateSectionItems(tagSelected, "Category 2");

            //CategoryAdapter categoryAdapter = categorisedList.LoadCategoryAdapter ();
            //categorisedList.setAdapterToListView();
            ((FollowMoreTopicsActivity)context).dismissProgressDialog();
        }*/
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
       /* worldpopulationlist.clear();
        if (charText.length() == 0) {
            worldpopulationlist.addAll(arraylist);
        } else {
            for (WorldPopulation wp : arraylist) {
                if (wp.getCountry().toLowerCase(Locale.getDefault())
                        .contains(charText)) {
                    worldpopulationlist.add(wp);
                }
            }
        }*/
        notifyDataSetChanged();
    }

}

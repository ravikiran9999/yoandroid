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

    /// <summary>
    /// Initializes a new instance of the <see cref="SectionHeadersEx.CategoryAdapter"/> class.
    /// </summary>
    /// <param name="context">Context.</param>
    /// <param name="listItemsWithHeaders">Books items with headers.</param>
    public CategoryAdapter(Activity context, List<Section> listItemsWithHeaders) {
        this.context = context;

        this.listItemsWithHeaders = listItemsWithHeaders;

        Log.d("CategoryAdapter", "listItemsWithHeaders is " + listItemsWithHeaders.size());

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

        if (selectedItem.getLayoutId() == R.layout.section) { // List item is a Section
            view = layoutInflater.inflate(R.layout.section, null);
            SectionItem sectionItem = selectedItem.getSectionItem();
            TextView title = (TextView) view.findViewById(R.id.title);
            title.setText(sectionItem.getCategory());
        } else if (selectedItem.getLayoutId() == R.layout.section_list_item) { // List item is a Section Item
            final TagView tagView = selectedItem.getSectionItem().getCategoryItem();
            /*tagView.setOnTagClickListener(new TagView.OnTagClickListener() {
                @Override
                public void onTagClick(Tag tag, int position) {
                    Log.d("TagClick", "Tag is clicked");
                    ((FollowMoreTopicsActivity) context).onClickingTag(tag, position);

                }
            });*/
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
        return false;
    }
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        notifyDataSetChanged();
    }
}

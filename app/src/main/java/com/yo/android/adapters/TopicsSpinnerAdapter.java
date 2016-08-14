package com.yo.android.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yo.android.model.Topics;

import java.util.List;

/**
 * Created by creatives on 7/5/2016.
 */
public class TopicsSpinnerAdapter extends BaseAdapter {

    List<Topics> items;
    Activity context;


    public TopicsSpinnerAdapter(Activity context, int resourceId, List<Topics> items) {
        this.context = context;

        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Topics getItem(int position) {
        if (getCount() > position) {
            return items.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public android.view.View getView(int position, View convertView, ViewGroup parent) {
        // re-use an existing view, if one is available
        View view = convertView;
        // otherwise create a new one
        if (view == null) {
            view = context.getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        Topics item = items.get(position);

        if (item != null) {
            TextView name = (TextView) view.findViewById(android.R.id.text1);

            if (name != null) {

                name.setText(item.getName());
            }

        }

        return view;
    }


}

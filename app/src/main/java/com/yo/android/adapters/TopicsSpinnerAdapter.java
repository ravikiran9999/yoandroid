package com.yo.android.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.yo.android.model.Topics;
import android.widget.BaseAdapter;


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


                View view = convertView; // re-use an existing view, if one is available
                if (view == null) { // otherwise create a new one
                        view = context.getLayoutInflater().inflate(android.R.layout.simple_spinner_item, parent, false);
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

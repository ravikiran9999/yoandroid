package com.yo.android.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.cognito.internal.util.StringUtils;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.yo.android.R;

import java.util.List;

/**
 * Created by rajesh on 21/9/16.
 */
public class AlphabetAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> mList;
    //private LayoutInflater inflater;

    public AlphabetAdapter(Context context, List<String> list) {
        this.mContext = context;
        this.mList = list;
        if (mContext != null) {
            //inflater = LayoutInflater.from(mContext);
        }
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public String getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        if (inflater != null) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.side_index_item, null);
                holder = new ViewHolder();
                holder.textView = (TextView) convertView.findViewById(R.id.side_list_item);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.textView.setText(getItem(position));
            return convertView;
        }
        return null;
    }

    public static class ViewHolder {
        public TextView textView;
    }
}

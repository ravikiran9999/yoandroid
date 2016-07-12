package com.yo.android.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ramesh on 14/1/16.
 */
public abstract class AbstractBaseAdapter<T, V extends AbstractViewHolder> extends BaseAdapter {

    private List<T> mList;
    protected final Context mContext;
    private List<T> mOriginalList = new ArrayList<>();
    private SparseBooleanArray mSelectedItemsIds;

    public AbstractBaseAdapter(Context context) {
        mContext = context;
        mList = new ArrayList<>();
        mSelectedItemsIds = new SparseBooleanArray();
    }


    public void addItems(List<T> list) {
        mList = new ArrayList<>(list);
        if (mOriginalList.isEmpty()) {
            mOriginalList = new ArrayList<>(list);
        }
        notifyDataSetChanged();
    }

    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    // Remove selection after unchecked
    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    // Item checked on selection
    public void selectView(int position, boolean value) {
        if (value) {
            mSelectedItemsIds.put(position, value);
        } else {
            mSelectedItemsIds.delete(position);
        }
        notifyDataSetChanged();
    }

    // Get number of selected item
    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }


    public void addItemsAll(List<T> list) {
        this.mList.addAll(list);
        this.mOriginalList.addAll(list);
        notifyDataSetChanged();
    }

    /**
     * Use this method for search to clear data when calling addItems() multiple times.
     */
    public void clearData() {
        mOriginalList.clear();
        mList.clear();
    }

    public void performSearch(@NonNull String key) {
        if (key.isEmpty()) {
            addItems(mOriginalList);
        } else {
            List<T> temp = new ArrayList<>();
            for (T event : mOriginalList) {
                if (hasData(event, key)) {
                    temp.add(event);
                }
            }
            addItems(temp);
        }
    }

    protected boolean hasData(T event, String key) {
        return false;
    }

    public void removeItem(Object object) {
        this.mList.remove(object);
        notifyDataSetChanged();
    }

    public List<T> getAllItems() {
        return mList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public T getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        V holder;
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(getLayoutId(), null);
            holder = getViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (V) view.getTag();
        }
        bindView(position, holder, getItem(position));
        return view;
    }

    public abstract int getLayoutId();

    public abstract V getViewHolder(View convertView);

    public abstract void bindView(int position, V holder, T item);

}

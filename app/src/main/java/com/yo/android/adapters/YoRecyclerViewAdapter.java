package com.yo.android.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by rdoddapaneni on 6/20/2017.
 */

public abstract class YoRecyclerViewAdapter extends RecyclerView.Adapter<YoViewHolder> {

    protected ArrayList<Object> mData;

    protected abstract int layout(final int position);

    protected abstract @NonNull
    YoViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view);

    @Override
    public YoViewHolder onCreateViewHolder(ViewGroup parent, int layout) {
        final LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        final View view = layoutInflater.inflate(layout, parent, false);
        return viewHolder(layout, view);
    }

    @Override
    public void onBindViewHolder(YoViewHolder holder, int position) {
        holder.bindData(mData.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        return layout(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public ArrayList<Object> getData() {
        return mData;
    }

    public void setData(ArrayList<Object> data) {
        mData = data;
    }
}

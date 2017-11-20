package com.yo.android.widgets.expandablerecycler;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.yo.android.adapters.YoViewHolder;

import java.util.ArrayList;

public abstract class YoRecyclerAdapter extends RecyclerView.Adapter<YoViewHolder> {

    protected ArrayList<Object> data;

    protected abstract int layout(final int position);
    protected abstract @NonNull YoViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view);

    @Override
    public YoViewHolder onCreateViewHolder(ViewGroup parent, int layout) {
        final LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        final View view = layoutInflater.inflate(layout, parent, false);
        return viewHolder(layout, view);
    }

    @Override
    public void onBindViewHolder(YoViewHolder holder, int position) {
        holder.bindData(data.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        return layout(position);
    }

    @Override
    public int getItemCount() {
        return data == null? 0 : data.size();
    }

    public ArrayList<Object> getData() {
        return data;
    }

    public void setData(ArrayList<Object> data) {
        this.data = data;
    }

}
package com.yo.android.widgets.expandablerecycler;

import android.view.View;

import com.yo.android.adapters.YoRecyclerViewAdapter;
import com.yo.android.adapters.YoViewHolder;

import java.util.ArrayList;

public abstract class YoExpandableRecyclerAdapter extends YoRecyclerViewAdapter {

    public void onBindViewHolder(final YoViewHolder holder, int position) {
        holder.bindData(mData.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition < 0) {
                    return;
                }

                Object item = mData.get(adapterPosition);
                if (item instanceof ExpandableItem) {
                    ExpandableItem expandableItem = (ExpandableItem) item;
                    if (!expandableItem.isExpanded()) {
                        expandParent(expandableItem);
                    } else {
                        collapseParent(expandableItem);
                    }
                }
            }
        });
    }

    public void expandParent(ExpandableItem expandableItem) {
        int indexOfParent = mData.indexOf(expandableItem);

        if (expandableItem.getChild() instanceof ArrayList) {
            ArrayList childList = (ArrayList) expandableItem.getChild();

            mData.add(indexOfParent + 1, childList);
            notifyItemChanged(indexOfParent);
            notifyItemRangeInserted(indexOfParent + 1, 1);
        } else {
            //mData.add(indexOfParent + 1, expandableItem.getChild());
            notifyItemChanged(indexOfParent);
        }

        expandableItem.setExpanded(true);
    }

    public void collapseParent(ExpandableItem expandableItem) {
        int indexOfParent = mData.indexOf(expandableItem);

        if (expandableItem.getChild() instanceof ArrayList) {
            ArrayList childList = (ArrayList) expandableItem.getChild();

            mData.remove(childList);
            notifyItemChanged(indexOfParent);
            //notifyItemRangeRemoved(indexOfParent + 1, childList.size());
            notifyItemRangeRemoved(indexOfParent + 1, 1);
        } else {
            int indexOfChild = mData.indexOf(expandableItem.getChild());
            mData.remove(indexOfChild);
            notifyItemChanged(indexOfParent);
        }

        expandableItem.setExpanded(false);
    }
}
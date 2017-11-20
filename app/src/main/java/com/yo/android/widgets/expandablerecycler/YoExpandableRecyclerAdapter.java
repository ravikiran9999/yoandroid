package com.yo.android.widgets.expandablerecycler;

import android.view.View;
import com.yo.android.adapters.YoViewHolder;

import java.util.ArrayList;

public abstract class YoExpandableRecyclerAdapter extends YoRecyclerAdapter {

    public void onBindViewHolder(final YoViewHolder holder, int position) {
            holder.bindData(data.get(position));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition < 0) {
                        return;
                    }

                    Object item = data.get(adapterPosition);
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
        int indexOfParent = data.indexOf(expandableItem);

        if (expandableItem.getChild() instanceof ArrayList) {
            ArrayList childList = (ArrayList) expandableItem.getChild();
            /*for (int i = 0; i < childList.size(); i++) {
                data.add(indexOfParent + 1 + i, childList.get(i));
            }*/
            data.add(indexOfParent + 1, childList);
            notifyItemChanged(indexOfParent);
            //notifyItemRangeInserted(indexOfParent + 1, childList.size());
            notifyItemRangeInserted(indexOfParent + 1, 1);
        }
        else {
            data.add(indexOfParent + 1, expandableItem.getChild());
            //notifyItemChanged(indexOfParent);
        }

        expandableItem.setExpanded(true);
    }

    public void collapseParent(ExpandableItem expandableItem) {
        int indexOfParent = data.indexOf(expandableItem);

        if (expandableItem.getChild() instanceof ArrayList) {
            ArrayList childList = (ArrayList) expandableItem.getChild();
            /*for (Object item : childList) {
                data.remove(item);
            }*/
            data.remove(childList);
            notifyItemChanged(indexOfParent);
            notifyItemRangeRemoved(indexOfParent + 1, childList.size());
        }
        else {
            int indexOfChild = data.indexOf(expandableItem.getChild());
            data.remove(indexOfChild);
            notifyItemChanged(indexOfParent);
        }

        expandableItem.setExpanded(false);
    }
}
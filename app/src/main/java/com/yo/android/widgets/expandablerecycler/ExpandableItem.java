package com.yo.android.widgets.expandablerecycler;

public abstract class ExpandableItem<T> {

    protected boolean isExpanded;

    public abstract T getChild();

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean isExpanded) {
        this.isExpanded = isExpanded;
    }

}
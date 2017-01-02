package com.yo.android.helpers;

import com.cunoraz.tagview.Tag;

/**
 * Created by creatives on 12/21/2016.
 */
public class TagSelected extends Tag {

    private boolean selected;
    private String tagId;

    public TagSelected(String text) {
        super(text);
    }

    public boolean getSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void toggleSelection() {
        this.selected = !selected;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }
}

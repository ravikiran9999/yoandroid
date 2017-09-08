package com.yo.android.model;

public class GroupAction {

    private String type;
    private String displayItem;
    private String value;

    public GroupAction(String type, String displayItem, String value) {
        this.type = type;
        this.value = value;
        this.displayItem = displayItem;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayItem() {
        return displayItem;
    }
}

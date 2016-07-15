package com.yo.android.model;

/**
 * Created by root on 15/7/16.
 */
public class MoreData {
    private String name;
    private boolean hasOptions;

    public MoreData(String name, boolean bool) {
        this.name = name;
        this.hasOptions = bool;
    }

    public String getName() {
        return name;
    }


    public boolean isHasOptions() {
        return hasOptions;
    }
}

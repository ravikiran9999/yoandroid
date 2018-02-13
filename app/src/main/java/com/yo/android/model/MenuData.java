package com.yo.android.model;

/**
 * Created by ramesh on 12/3/16.
 */
public class MenuData {
    private String name;
    private int icon;

    public MenuData(String name, int icon) {
        this.name = name;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}

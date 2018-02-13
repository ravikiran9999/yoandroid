package com.yo.android.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by creatives on 12/22/2016.
 */
public class Categories implements Serializable {

    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private String name;
    @SerializedName("tags")
    private ArrayList<Topics> tags;

    private transient boolean language_specific;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isLanguage_specific() {
        return language_specific;
    }

    public void setLanguage_specific(boolean language_specific) {
        this.language_specific = language_specific;
    }

    public ArrayList<Topics> getTags() {
        return tags;
    }

    public void setTags(ArrayList<Topics> tags) {
        this.tags = tags;
    }
}

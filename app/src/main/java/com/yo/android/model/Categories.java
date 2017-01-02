package com.yo.android.model;

import java.util.List;

/**
 * Created by creatives on 12/22/2016.
 */
public class Categories {

    private String id;
    private String name;
    private boolean language_specific;
    private List<Topics> tags;

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

    public List<Topics> getTags() {
        return tags;
    }

    public void setTags(List<Topics> tags) {
        this.tags = tags;
    }
}

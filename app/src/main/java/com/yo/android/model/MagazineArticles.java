package com.yo.android.model;

import java.util.List;

/**
 * Created by creatives on 7/15/2016.
 */
public class MagazineArticles {

    private String id;
    private String name;
    private String description;
    private String privacy;
    private List<Articles> articles;

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

    public List<Articles> getArticlesList() {
        return articles;
    }

    public void setArticlesList(List<Articles> articlesList) {
        this.articles = articlesList;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }
}

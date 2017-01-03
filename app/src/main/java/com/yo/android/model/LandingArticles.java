package com.yo.android.model;

import java.util.List;

/**
 * Created by creatives on 12/8/2016.
 */
public class LandingArticles {

    private List<Articles> followed_topic_articles;
    private List<Articles> random_articles;

    public List<Articles> getFollowed_topic_articles() {
        return followed_topic_articles;
    }

    public void setFollowed_topic_articles(List<Articles> followed_topic_articles) {
        this.followed_topic_articles = followed_topic_articles;
    }

    public List<Articles> getRandom_articles() {
        return random_articles;
    }

    public void setRandom_articles(List<Articles> random_articles) {
        this.random_articles = random_articles;
    }
}

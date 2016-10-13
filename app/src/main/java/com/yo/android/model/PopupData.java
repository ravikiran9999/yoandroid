package com.yo.android.model;

/**
 * Created by creatives on 10/6/2016.
 */
public class PopupData {
    private String title;
    private String id;
    private String message;
    private String image_url;
    private String tag;
    private String redirect_to;
    private long timestamp;
    private String live_from;
    private String live_to;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getRedirect_to() {
        return redirect_to;
    }

    public void setRedirect_to(String redirect_to) {
        this.redirect_to = redirect_to;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getLive_from() {
        return live_from;
    }

    public void setLive_from(String live_from) {
        this.live_from = live_from;
    }

    public String getLive_to() {
        return live_to;
    }

    public void setLive_to(String live_to) {
        this.live_to = live_to;
    }
}

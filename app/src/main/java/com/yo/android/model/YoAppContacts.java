package com.yo.android.model;

/**
 * Created by rdoddapaneni on 7/17/2016.
 */

public class YoAppContacts {
    private String id;
    private String first_name;
    private String last_ame;
    private String description;
    private String avatar;

    public YoAppContacts(String id, String first_name, String last_ame, String description, String avatar) {
        this.id = id;
        this.first_name = first_name;
        this.last_ame = last_ame;
        this.description = description;
        this.avatar = avatar;
    }

    public String getId() {
        return id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public String getLast_ame() {
        return last_ame;
    }

    public String getDescription() {
        return description;
    }

    public String getAvatar() {
        return avatar;
    }
}

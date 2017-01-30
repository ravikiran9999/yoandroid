package com.yo.android.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by rdoddapaneni on 8/23/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoomInfo {

    private String image;
    private String name;
    private String created_at;
    private String status;

    public RoomInfo() {
        // empty default constructor, necessary for Firebase to be able to deserialize
    }

    public RoomInfo(String image, String name, String created_at, String status) {
        this.image = image;
        this.name = name;
        this.created_at = created_at;
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

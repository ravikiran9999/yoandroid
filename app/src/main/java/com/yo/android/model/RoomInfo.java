package com.yo.android.model;

/**
 * Created by rdoddapaneni on 8/23/2016.
 */

public class RoomInfo {

    private String image;
    private String name;

    public RoomInfo() {
        // empty default constructor, necessary for Firebase to be able to deserialize
    }

    public RoomInfo(String image, String name) {
        this.image = image;
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public String getName() {
        return name;
    }
}

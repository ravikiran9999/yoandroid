package com.yo.android.model;

/**
 * Created by creatives on 7/9/2016.
 */
public class Collections {

    private String id;
    private String name;
    private String image;
    private String type;
    private boolean select;

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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    public boolean toggleSelection() {
        return select = !select;
    }

}

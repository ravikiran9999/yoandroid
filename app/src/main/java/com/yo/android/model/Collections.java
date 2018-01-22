package com.yo.android.model;

/**
 * Created by creatives on 7/9/2016.
 */
public class Collections {

    private String id;
    private String name;
    private String image;
    private String s3_image_filename;
    private String type;
    private boolean select;
    private int articlesCount;
    private String video_url;


    public int getArticlesCount() {
        return articlesCount;
    }

    public void setArticlesCount(int articlesCount) {
        this.articlesCount = articlesCount;
    }

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

    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

    public String getS3_image_filename() {
        return s3_image_filename;
    }

    public void setS3_image_filename(String s3_image_filename) {
        this.s3_image_filename = s3_image_filename;
    }
}

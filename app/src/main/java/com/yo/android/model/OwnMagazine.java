package com.yo.android.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by creatives on 7/9/2016.
 */
public class OwnMagazine implements Parcelable {
    private String id;
    private String name;
    private String description;
    private String privacy;
    private String image;
    private String isFollowing;
    private int articlesCount;

    public static final Creator<OwnMagazine> CREATOR = new Creator<OwnMagazine>() {
        @Override
        public OwnMagazine createFromParcel(Parcel in) {
            return new OwnMagazine(in);
        }

        @Override
        public OwnMagazine[] newArray(int size) {
            return new OwnMagazine[size];
        }
    };

    public OwnMagazine() {
    }

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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getIsFollowing() {
        return isFollowing;
    }

    public void setIsFollowing(String isFollowing) {
        this.isFollowing = isFollowing;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(privacy);
        dest.writeString(image);
        dest.writeString(isFollowing);
        dest.writeInt(articlesCount);
    }

    private OwnMagazine(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.description = in.readString();
        this.privacy = in.readString();
        this.image = in.readString();
        this.isFollowing = in.readString();
        this.articlesCount = in.readInt();
    }
}

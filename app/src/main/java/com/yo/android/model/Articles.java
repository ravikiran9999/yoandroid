package com.yo.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashSet;

/**
 * Created by creatives on 7/5/2016.
 */
public class Articles implements Parcelable {

    private String id;
    private String title;
    private String url;
    private String image_filename;
    private String summary;
    private boolean isChecked;
    private String liked;
    private boolean isFollow;
    private String isFollowing;
    private String magzine_id;
    private String generated_url;
    private String topicName;
    private String topicId;
    private String topicFollowing;
    private String updated;
    private String video_url;

    public Articles() {
    }

    protected Articles(Parcel in) {
        id = in.readString();
        title = in.readString();
        url = in.readString();
        image_filename = in.readString();
        summary = in.readString();
        isChecked = in.readByte() != 0;
        liked = in.readString();
        isFollow = in.readByte() != 0;
        isFollowing = in.readString();
        magzine_id = in.readString();
        generated_url = in.readString();
        topicName = in.readString();
        topicId = in.readString();
        topicFollowing = in.readString();
        updated = in.readString();
        video_url = in.readString();
    }

    public static final Creator<Articles> CREATOR = new Creator<Articles>() {
        @Override
        public Articles createFromParcel(Parcel in) {
            return new Articles(in);
        }

        @Override
        public Articles[] newArray(int size) {
            return new Articles[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImage_filename() {
        return image_filename;
    }

    public void setImage_filename(String image_filename) {
        this.image_filename = image_filename;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public String getLiked() {
        return liked;
    }

    public void setLiked(String liked) {
        this.liked = liked;
    }

    public boolean isFollow() {
        return isFollow;
    }

    public void setIsFollow(boolean isFollow) {
        this.isFollow = isFollow;
    }

    public String getIsFollowing() {
        return isFollowing;
    }

    public void setIsFollowing(String isFollowing) {
        this.isFollowing = isFollowing;
    }

    public String getMagzine_id() {
        return magzine_id;
    }

    public void setMagzine_id(String magzine_id) {
        this.magzine_id = magzine_id;
    }

    public String getGenerated_url() {
        return generated_url;
    }

    public void setGenerated_url(String generated_url) {
        this.generated_url = generated_url;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public String getTopicFollowing() {
        return topicFollowing;
    }

    public void setTopicFollowing(String topicFollowing) {
        this.topicFollowing = topicFollowing;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }


    @Override
    public int describeContents() {
        return 0;
    }


    @Override

    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(url);
        dest.writeString(image_filename);
        dest.writeString(summary);
        dest.writeInt(isChecked ? 0 : 1);
        dest.writeString(liked);
        dest.writeInt(isFollow ? 0 : 1);
        dest.writeString(isFollowing);
        dest.writeString(magzine_id);
        dest.writeString(generated_url);
        dest.writeString(topicName);
        dest.writeString(topicId);
        dest.writeString(topicFollowing);
        dest.writeString(updated);
        dest.writeString(video_url);
    }

    public int hashCode() {
        int hash = getId().hashCode();
        return hash;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Articles) {
            Articles articles = (Articles) obj;
            return (articles.getId().equals(this.getId()));
        } else {
            return false;
        }
    }
}

package com.yo.android.model;

import android.content.Context;

import com.yo.android.R;

/**
 * Created by MYPC on 7/17/2016.
 */
public class FindPeople {

    private String id;
    private String first_name;
    private String last_name;
    private String description;
    private String avatar;
    private String isFollowing;
    private int magzinesCount;
    private int likedArticlesCount;
    private int followersCount;
    private String self;
    private String phone_no;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIsFollowing() {
        return isFollowing;
    }

    public void setIsFollowing(String isFollowing) {
        this.isFollowing = isFollowing;
    }

    public int getMagzinesCount() {
        return magzinesCount;
    }

    public void setMagzinesCount(int magzinesCount) {
        this.magzinesCount = magzinesCount;
    }

    public int getLikedArticlesCount() {
        return likedArticlesCount;
    }

    public void setLikedArticlesCount(int likedArticlesCount) {
        this.likedArticlesCount = likedArticlesCount;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public String getPhone_no() {
        return phone_no;
    }

    public void setPhone_no(String phone_no) {
        this.phone_no = phone_no;
    }

    public String getFullName(Context context) {
        String fullName = String.format(context.getString(R.string.first_last_name_format), getFirst_name(), getLast_name());
        return fullName.substring(0, 1).toUpperCase() + fullName.substring(1);
    }
}

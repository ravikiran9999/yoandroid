package com.yo.android.database.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class DBRoom extends RealmObject {

    @PrimaryKey
    private String firebaseRoomId;
    private String id;
    private String groupName;
    private String image;
    private RealmList<DBMembers> members;
    private String lastChat;
    private boolean isImages;
    private String timeStamp;
    private String presence;
    private String firebaseUserId;

    //For sorting
    private long groupCreationTime;
    private long time;
    private String fullName;
    private String mobileNumber;
    private String phoneNumber;
    private String nexgeUserName = "";
    private String youserId;
    private String created_at;
    private RealmList<DBGroupMembers> groupMembers;

    public String getFirebaseRoomId() {
        return firebaseRoomId;
    }

    public void setFirebaseRoomId(String firebaseRoomId) {
        this.firebaseRoomId = firebaseRoomId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public RealmList<DBMembers> getMembers() {
        return members;
    }

    public void setMembers(RealmList<DBMembers> members) {
        this.members = members;
    }

    public String getLastChat() {
        return lastChat;
    }

    public void setLastChat(String lastChat) {
        this.lastChat = lastChat;
    }

    public boolean isImages() {
        return isImages;
    }

    public void setImages(boolean images) {
        isImages = images;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getPresence() {
        return presence;
    }

    public void setPresence(String presence) {
        this.presence = presence;
    }

    public String getFirebaseUserId() {
        return firebaseUserId;
    }

    public void setFirebaseUserId(String firebaseUserId) {
        this.firebaseUserId = firebaseUserId;
    }

    public long getGroupCreationTime() {
        return groupCreationTime;
    }

    public void setGroupCreationTime(long groupCreationTime) {
        this.groupCreationTime = groupCreationTime;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getNexgeUserName() {
        return nexgeUserName;
    }

    public void setNexgeUserName(String nexgeUserName) {
        this.nexgeUserName = nexgeUserName;
    }

    public String getYouserId() {
        return youserId;
    }

    public void setYouserId(String youserId) {
        this.youserId = youserId;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public RealmList<DBGroupMembers> getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(RealmList<DBGroupMembers> groupMembers) {
        this.groupMembers = groupMembers;
    }
}

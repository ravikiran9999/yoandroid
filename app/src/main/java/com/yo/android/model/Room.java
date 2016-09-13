package com.yo.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rdoddapaneni on 7/20/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Room implements Parcelable {

    private String id;
    private String firebaseRoomId;
    private String groupName;
    private String image;
    private List<Members> members = new ArrayList<>();
    private String lastChat;
    private boolean isImages;
    private String timeStamp;
    //For sorting
    private long time;
    private String fullName;
    private String mobileNumber;
    private String voxUserName;
    private String youserId;
    private List<GroupMembers> groupMembers = new ArrayList<>();

    public String getVoxUserName() {
        return voxUserName;
    }

    public void setVoxUserName(String voxUserName) {
        this.voxUserName = voxUserName;
    }


    public Room() {
        // empty default constructor, necessary for Firebase to be able to deserialize
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirebaseRoomId() {
        return firebaseRoomId;
    }

    public void setFirebaseRoomId(String firebaseRoomId) {
        this.firebaseRoomId = firebaseRoomId;
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

    public List<Members> getMembers() {
        return members;
    }

    public void setMembers(List<Members> members) {
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

    public List<GroupMembers> getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(List<GroupMembers> groupMembers) {
        this.groupMembers = groupMembers;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(firebaseRoomId);
        dest.writeString(groupName);
        dest.writeString(image);
        dest.writeTypedList(members);
        dest.writeString(fullName);
        dest.writeString(mobileNumber);
        dest.writeString(voxUserName);
        dest.writeString(youserId);
    }

    public String getYouserId() {
        return youserId;
    }

    public void setYouserId(String youserId) {
        this.youserId = youserId;
    }

    public Room(Parcel in) {

        this.id = in.readString();
        this.firebaseRoomId = in.readString();
        this.groupName = in.readString();
        this.image = in.readString();
        in.readTypedList(members, Members.CREATOR);
        this.fullName = in.readString();
        this.mobileNumber = in.readString();
        this.voxUserName = in.readString();
        this.youserId = in.readString();
    }

    public static final Parcelable.Creator<Room> CREATOR = new Parcelable.Creator<Room>() {
        @Override
        public Room createFromParcel(Parcel source) {
            return new Room(source);
        }

        @Override
        public Room[] newArray(int size) {
            return new Room[size];
        }
    };

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Room)) {
            return false;
        }
        Room other = (Room) obj;
        return this.voxUserName.equals(other.voxUserName);
    }

    public int hashCode() {
        return voxUserName.hashCode();
    }
}

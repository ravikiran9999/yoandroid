package com.yo.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rdoddapaneni on 7/20/2016.
 */

public class Room implements Parcelable {

    private String id;
    private String firebaseRoomId;
    private String groupName;
    private String image;
    private List<Members> members = new ArrayList<>();
    private String lastChat;
    private boolean isImage;
    private String timeStamp;

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

    public boolean isImage() {
        return isImage;
    }

    public void setImage(boolean image) {
        isImage = image;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
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
    }

    public Room(Parcel in) {

        this.id = in.readString();
        this.firebaseRoomId = in.readString();
        this.groupName = in.readString();
        this.image = in.readString();
        in.readTypedList(members,Members.CREATOR);
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
}

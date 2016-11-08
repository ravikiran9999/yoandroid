package com.yo.android.model;

/**
 * Created by rdoddapaneni on 6/27/2016.
 */

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.IgnoreExtraProperties;
import com.j256.ormlite.field.DatabaseField;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

/**
 * Class name will be tablename
 */

@IgnoreExtraProperties
public class ChatMessage implements Parcelable {

    @DatabaseField(id = true)
    private int msgID;
    @DatabaseField(columnName = Constants.MESSAGE)
    private String message;
    @DatabaseField
    private String senderID;
    @DatabaseField
    private int status;
    @DatabaseField
    private String imagePath;
    @DatabaseField
    private long time;
    @DatabaseField
    private boolean readUnreadStatus;
    private int delivered;
    private int sent;
    private long deliveredTime;

    private String stickeyHeader;

    private String type;
    private String roomId;
    private boolean selected;
    //For caching the image
    private String imageUrl;
    private String voxUserName;
    private String youserId;
    private String chatProfileUserName;
    private String roomName;


    public ChatMessage() {
        // Default constructor required for calls to DataSnapshot.getValue(ChatMessage.class)
    }


    private ChatMessage(Parcel in) {
        this.msgID = in.readInt();
        this.message = in.readString();
        this.senderID = in.readString();
        this.status = in.readInt();
        this.imagePath = in.readString();
        this.time = in.readLong();
        this.type = in.readString();
        this.roomId = in.readString();
        this.stickeyHeader = in.readString();
        this.sent = in.readInt();
        this.delivered = in.readInt();
        this.voxUserName = in.readString();
        this.youserId = in.readString();
        this.chatProfileUserName = in.readString();
        this.roomName = in.readString();
    }

    public int getMsgID() {
        return msgID;
    }

    public void setMsgID(int msgID) {
        this.msgID = msgID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public long getTime() {
        return time;
    }

    public String getChatProfileUserName() {
        return chatProfileUserName;
    }

    public void setChatProfileUserName(String chatProfileUserName) {
        this.chatProfileUserName = chatProfileUserName;
    }

    public void setTime(long time) {
        this.time = time;
        this.stickeyHeader = Util.getChatListTimeFormat(time);
    }

    public boolean isReadUnreadStatus() {
        return readUnreadStatus;
    }

    public void setReadUnreadStatus(boolean readUnreadStatus) {
        this.readUnreadStatus = readUnreadStatus;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }


    public String getStickeyHeader() {
        return stickeyHeader;
    }

    public int getDelivered() {
        return delivered;
    }

    public void setDelivered(int delivered) {
        this.delivered = delivered;
    }

    public int getSent() {
        return sent;
    }

    public void setSent(int sent) {
        this.sent = sent;
    }

    public long getDeliveredTime() {
        return deliveredTime;
    }

    public void setDeliveredTime(long deliveredTime) {
        this.deliveredTime = deliveredTime;
    }

    public String getVoxUserName() {
        return voxUserName;
    }

    public void setVoxUserName(String voxUserName) {
        this.voxUserName = voxUserName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getYouserId() {
        return youserId;
    }

    public void setYouserId(String youserId) {
        this.youserId = youserId;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(msgID);
        dest.writeString(message);
        dest.writeString(senderID);
        dest.writeInt(status);
        dest.writeString(imagePath);
        dest.writeLong(time);
        dest.writeString(type);
        dest.writeString(roomId);
        dest.writeString(stickeyHeader);
        dest.writeInt(sent);
        dest.writeInt(delivered);
        dest.writeString(voxUserName);
        dest.writeString(youserId);
        dest.writeString(chatProfileUserName);
        dest.writeString(roomName);
    }


    public static final Parcelable.Creator<ChatMessage> CREATOR = new Parcelable.Creator<ChatMessage>() {
        @Override
        public ChatMessage createFromParcel(Parcel source) {
            return new ChatMessage(source);
        }

        @Override
        public ChatMessage[] newArray(int size) {
            return new ChatMessage[size];
        }
    };

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}

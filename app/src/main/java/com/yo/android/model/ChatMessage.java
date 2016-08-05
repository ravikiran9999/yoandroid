package com.yo.android.model;

/**
 * Created by rdoddapaneni on 6/27/2016.
 */

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.j256.ormlite.field.DatabaseField;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.util.HashMap;
import java.util.Map;

/**
 * Class name will be tablename
 */
public class ChatMessage implements Parcelable {

    @DatabaseField(id = true)
    private String msgID;
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

    public ChatMessage() {
    }

    public String getMsgID() {
        return msgID;
    }

    public void setMsgID(String msgID) {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(msgID);
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

    }

    private ChatMessage(Parcel in) {
        this.msgID = in.readString();
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

package com.yo.android.model;

/**
 * Created by rdoddapaneni on 6/27/2016.
 */

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.yo.android.util.Constants;
import com.yo.android.util.DateUtil;
import com.yo.android.util.Util;

import java.util.HashMap;
import java.util.Map;

/**
 * Class name will be tablename
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@IgnoreExtraProperties
public class ChatMessage implements Parcelable {

    //@DatabaseField(id = true)
    private int msgID;
    //@DatabaseField(columnName = Constants.MESSAGE)
    private String message;
    //@DatabaseField
    private String senderID;
    //@DatabaseField
    private int status;
    //@DatabaseField
    private String imagePath;
    //@DatabaseField
    private long time;
    //@DatabaseField
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
    private String nexgeUserName;
    private String youserId;
    private String chatProfileUserName;
    private String roomName;
    private String roomImage;
    private String messageKey;
    private Map<String, Object> timeStampMap;
    @JsonIgnore
    private long serverTimeStampReceived;

    public ChatMessage() {
        // Default constructor required for calls to DataSnapshot.getValue(ChatMessage.class)
    }

    public ChatMessage(int msgID, String message, String senderID, int status, String imagePath, long time, int delivered, int sent, long deliveredTime, String stickeyHeader, String type, String roomId, boolean selected, String imageUrl, String nexgeUserName, String youserId, String chatProfileUserName, String roomName, String roomImage, String messageKey, Map<String, Object> timeStampMap, long serverTimeStampReceived) {
        this.msgID = msgID;
        this.message = message;
        this.senderID = senderID;
        this.status = status;
        this.imagePath = imagePath;
        this.time = time;
        this.delivered = delivered;
        this.sent = sent;
        this.deliveredTime = deliveredTime;
        this.stickeyHeader = stickeyHeader;
        this.type = type;
        this.roomId = roomId;
        this.selected = selected;
        this.imageUrl = imageUrl;
        this.nexgeUserName = nexgeUserName;
        this.youserId = youserId;
        this.chatProfileUserName = chatProfileUserName;
        this.roomName = roomName;
        this.roomImage = roomImage;
        this.messageKey = messageKey;
        this.timeStampMap = timeStampMap;
        this.serverTimeStampReceived = serverTimeStampReceived;
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
        this.nexgeUserName = in.readString();
        this.youserId = in.readString();
        this.chatProfileUserName = in.readString();
        this.roomName = in.readString();
        this.messageKey = in.readString();
    }

    public Map<String, Object> getTimeStampMap() {
        return timeStampMap;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public long getServerTimeStampReceived() {
        return serverTimeStampReceived;
    }

    public void setServerTimeStampReceived(long serverTimeStampReceived) {
        this.serverTimeStampReceived = serverTimeStampReceived;
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

        Map<String, Object> serverTimeStamp = new HashMap<>();
        serverTimeStamp.put("serverTimeStamp", ServerValue.TIMESTAMP);
        timeStampMap = serverTimeStamp;
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


    public static String getStickeyHeader(long time) {
        return DateUtil.getChatListTimeFormat(time);
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
        return nexgeUserName;
    }

    public void setVoxUserName(String nexgeUserName) {
        this.nexgeUserName = nexgeUserName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomImage() {
        return roomImage;
    }

    public void setRoomImage(String roomImage) {
        this.roomImage = roomImage;
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

    public void setNexgeUserName(String nexgeUserName) {
        this.nexgeUserName = nexgeUserName;
    }

    public void setTimeStampMap(Map<String, Object> timeStampMap) {
        this.timeStampMap = timeStampMap;
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
        dest.writeString(nexgeUserName);
        dest.writeString(youserId);
        dest.writeString(chatProfileUserName);
        dest.writeString(roomName);
        dest.writeString(messageKey);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatMessage)) return false;

        ChatMessage that = (ChatMessage) o;

        if (msgID != that.msgID) return false;
        if (status != that.status) return false;
        if (time != that.time) return false;
        if (readUnreadStatus != that.readUnreadStatus) return false;
        if (delivered != that.delivered) return false;
        if (sent != that.sent) return false;
        if (deliveredTime != that.deliveredTime) return false;
        if (selected != that.selected) return false;
        if (message != null ? !message.equals(that.message) : that.message != null) return false;
        if (senderID != null ? !senderID.equals(that.senderID) : that.senderID != null)
            return false;
        if (imagePath != null ? !imagePath.equals(that.imagePath) : that.imagePath != null)
            return false;
        if (stickeyHeader != null ? !stickeyHeader.equals(that.stickeyHeader) : that.stickeyHeader != null)
            return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (roomId != null ? !roomId.equals(that.roomId) : that.roomId != null) return false;
        if (imageUrl != null ? !imageUrl.equals(that.imageUrl) : that.imageUrl != null)
            return false;
        if (nexgeUserName != null ? !nexgeUserName.equals(that.nexgeUserName) : that.nexgeUserName != null)
            return false;
        if (youserId != null ? !youserId.equals(that.youserId) : that.youserId != null)
            return false;
        if (chatProfileUserName != null ? !chatProfileUserName.equals(that.chatProfileUserName) : that.chatProfileUserName != null)
            return false;
        if (roomName != null ? !roomName.equals(that.roomName) : that.roomName != null)
            return false;
        return messageKey != null ? messageKey.equals(that.messageKey) : that.messageKey == null;
    }

    @Override
    public int hashCode() {
        int result = msgID;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (senderID != null ? senderID.hashCode() : 0);
        result = 31 * result + status;
        result = 31 * result + (imagePath != null ? imagePath.hashCode() : 0);
        result = 31 * result + (int) (time ^ (time >>> 32));
        result = 31 * result + (readUnreadStatus ? 1 : 0);
        result = 31 * result + delivered;
        result = 31 * result + sent;
        result = 31 * result + (int) (deliveredTime ^ (deliveredTime >>> 32));
        result = 31 * result + (stickeyHeader != null ? stickeyHeader.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (roomId != null ? roomId.hashCode() : 0);
        result = 31 * result + (selected ? 1 : 0);
        result = 31 * result + (imageUrl != null ? imageUrl.hashCode() : 0);
        result = 31 * result + (nexgeUserName != null ? nexgeUserName.hashCode() : 0);
        result = 31 * result + (youserId != null ? youserId.hashCode() : 0);
        result = 31 * result + (chatProfileUserName != null ? chatProfileUserName.hashCode() : 0);
        result = 31 * result + (roomName != null ? roomName.hashCode() : 0);
        result = 31 * result + (messageKey != null ? messageKey.hashCode() : 0);
        return result;
    }
}

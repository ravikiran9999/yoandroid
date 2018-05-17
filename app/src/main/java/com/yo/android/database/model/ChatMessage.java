package com.yo.android.database.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "ChatMessage")
public class ChatMessage {

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "message_id")
    private int msgID;
    @ColumnInfo(name = "chat_room_id")
    private String roomId;
    @ColumnInfo(name = "message_key")
    private String messageKey;
    @ColumnInfo(name = "message")
    private String message;
    @ColumnInfo(name = "sender_id")
    private String senderID;
    @ColumnInfo(name = "status")
    private int status;
    @ColumnInfo(name = "image_path")
    private String imagePath;
    @ColumnInfo(name = "time")
    private long time;
    @ColumnInfo(name = "delivered")
    private int delivered;
    @ColumnInfo(name = "sent")
    private int sent;
    @ColumnInfo(name = "delivered_time")
    private long deliveredTime;
    @ColumnInfo(name = "stickey_header")
    private String stickeyHeader;
    @ColumnInfo(name = "type")
    private String type;
    @ColumnInfo(name = "selected")
    private boolean selected;
    @ColumnInfo(name = "nexge_user_name")
    private String nexgeUserName;
    @ColumnInfo(name = "yo_user_id")
    private String youserId;
    @ColumnInfo(name = "chat_profile_user_name")
    private String chatProfileUserName;
    @ColumnInfo(name = "room_name")
    private String roomName;
    @ColumnInfo(name = "room_image")
    private String roomImage;
    @ColumnInfo(name = "server_timestamp")
    private long serverTimeStampReceived;

    //For caching the image
    private String imageUrl;

    public ChatMessage() {
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
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

    public void setTime(long time) {
        this.time = time;
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

    public String getStickeyHeader() {
        return stickeyHeader;
    }

    public void setStickeyHeader(String stickeyHeader) {
        this.stickeyHeader = stickeyHeader;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public String getChatProfileUserName() {
        return chatProfileUserName;
    }

    public void setChatProfileUserName(String chatProfileUserName) {
        this.chatProfileUserName = chatProfileUserName;
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
}

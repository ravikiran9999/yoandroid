package com.yo.android.database.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Chaatz on 19-02-2018.
 */

public class DBChatMessage extends RealmObject {
    @PrimaryKey
    private String roomId;
    private int msgID;
    private String message;
    private String senderID;
    private int status;
    private String imagePath;
    private long time;
    private boolean readUnreadStatus;
    private int delivered;
    private int sent;
    private long deliveredTime;
    private String stickeyHeader;
    private String type;
    private boolean selected;
    //For caching the image
    private String imageUrl;

    private String nexgeUserName;
    private String youserId;
    private String chatProfileUserName;
    private String roomName;
    private String roomImage;
    private String messageKey;
    //private RealmList<DBTimestamp> dbTimestamps;
    private long serverTimeStampReceived;


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

    public boolean isReadUnreadStatus() {
        return readUnreadStatus;
    }

    public void setReadUnreadStatus(boolean readUnreadStatus) {
        this.readUnreadStatus = readUnreadStatus;
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

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
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

    /*public RealmList<DBTimestamp> getDbTimestamps() {
        return dbTimestamps;
    }

    public void setDbTimestamps(RealmList<DBTimestamp> dbTimestamps) {
        this.dbTimestamps = dbTimestamps;
    }*/

    public long getServerTimeStampReceived() {
        return serverTimeStampReceived;
    }

    public void setServerTimeStampReceived(long serverTimeStampReceived) {
        this.serverTimeStampReceived = serverTimeStampReceived;
    }
}

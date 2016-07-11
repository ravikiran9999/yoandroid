package com.yo.android.model;

/**
 * Created by rdoddapaneni on 6/27/2016.
 */

import android.net.Uri;

import com.j256.ormlite.field.DatabaseField;
import com.yo.android.util.Constants;

/**
 * Class name will be tablename
 */
public class ChatMessage {

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

    private String type;
    private String roomId;
    private boolean selected;

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
}

package com.yo.android.model;

/**
 * Created by rdoddapaneni on 6/27/2016.
 */

import com.j256.ormlite.field.DatabaseField;
import com.yo.android.util.DatabaseConstant;

/**
 * Class name will be tablename
 */
public class ChatMessage {

    @DatabaseField(id = true)
    private String msgID;
    @DatabaseField(columnName = DatabaseConstant.MESSAGE)
    private String message;
    @DatabaseField
    private String senderID;
    @DatabaseField
    private int status;
    @DatabaseField
    private String path;
    @DatabaseField
    private long time;

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

}

package com.yo.android.model;

import com.j256.ormlite.field.DatabaseField;
import com.yo.android.util.Constants;

/**
 * Created by rdoddapaneni on 6/30/2016.
 */

public class ChatRoom {
    @DatabaseField(id = true, columnName = Constants.YOUR_PHONE_NUMBER)
    private String yourPhoneNumber;
    @DatabaseField(columnName = Constants.OPPONENT_PHONE_NUMBER)
    private String opponentPhoneNumber;
    @DatabaseField(columnName = Constants.CHAT_ROOM_ID, unique = true)
    private String chatRoomId;

    private String message;
    private String timeStamp;

    private boolean isImage;

    public ChatRoom() {
        // empty default constructor, necessary for Firebase to be able to deserialize
    }

    public ChatRoom(String yourPhoneNumber, String opponentPhoneNumber, String chatRoomId) {
        this.yourPhoneNumber = yourPhoneNumber;
        this.opponentPhoneNumber = opponentPhoneNumber;
        this.chatRoomId = chatRoomId;
    }

    public String getYourPhoneNumber() {
        return yourPhoneNumber;
    }

    public String getOpponentPhoneNumber() {
        return opponentPhoneNumber;
    }

    public String getChatRoomId() {
        return chatRoomId;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean isImage() {
        return isImage;
    }

    public void setIsImage(boolean isImage) {
        this.isImage = isImage;
    }
}

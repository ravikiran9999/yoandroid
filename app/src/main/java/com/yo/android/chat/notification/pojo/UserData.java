package com.yo.android.chat.notification.pojo;

/**
 * Created by Anitha on 7/12/15.
 */
public class UserData {

    private String description;
    private int messageId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserData userData = (UserData) o;

        if (messageId != userData.messageId) return false;
        return description != null ? description.equals(userData.description) : userData.description == null;

    }

    @Override
    public int hashCode() {
        int result = description != null ? description.hashCode() : 0;
        result = 31 * result + messageId;
        return result;
    }

    public int getMessageId() {

        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

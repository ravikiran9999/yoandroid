package com.yo.android.database.mapper;

import com.yo.android.database.model.DBChatMessage;
import com.yo.android.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chaatz on 19-02-2018.
 */

public class DbChatMessageMapper {

    public static DBChatMessage map(ChatMessage chatMessage) {

        DBChatMessage dbChatMessage = new DBChatMessage();
        dbChatMessage.setMsgID(chatMessage.getMsgID());
        dbChatMessage.setMessage(chatMessage.getMessage());
        dbChatMessage.setSenderID(chatMessage.getSenderID());
        dbChatMessage.setStatus(chatMessage.getStatus());
        dbChatMessage.setImagePath(chatMessage.getImagePath());
        dbChatMessage.setTime(chatMessage.getTime());
        dbChatMessage.setDelivered(chatMessage.getDelivered());
        dbChatMessage.setSent(chatMessage.getSent());
        dbChatMessage.setDeliveredTime(chatMessage.getDeliveredTime());
        dbChatMessage.setType(chatMessage.getType());
        dbChatMessage.setRoomId(chatMessage.getRoomId());
        dbChatMessage.setImageUrl(chatMessage.getImageUrl());
        dbChatMessage.setNexgeUserName(chatMessage.getVoxUserName());
        dbChatMessage.setYouserId(chatMessage.getYouserId());
        dbChatMessage.setChatProfileUserName(chatMessage.getChatProfileUserName());
        dbChatMessage.setRoomName(chatMessage.getRoomName());
        dbChatMessage.setRoomImage(chatMessage.getRoomImage());
        dbChatMessage.setMessageKey(chatMessage.getMessageKey());
        //dbChatMessage.setTimeStampMap(chatMessage.getTimeStampMap());
        dbChatMessage.setServerTimeStampReceived(chatMessage.getServerTimeStampReceived());

        // These two are not added. Need to check
        //this.stickeyHeader = stickeyHeader;
        //this.selected = selected;

        return dbChatMessage;
    }

    public static ArrayList<ChatMessage> map(List<DBChatMessage> dbChatMessages) {
        ArrayList<ChatMessage> chatMessageList = new ArrayList<>();

        for(DBChatMessage dbChatMessage : dbChatMessages) {
            ChatMessage chatMessage = new ChatMessage();

            chatMessage.setMsgID(dbChatMessage.getMsgID());
            chatMessage.setMessage(dbChatMessage.getMessage());
            chatMessage.setSenderID(dbChatMessage.getSenderID());
            chatMessage.setStatus(dbChatMessage.getStatus());
            chatMessage.setImagePath(dbChatMessage.getImagePath());
            chatMessage.setTime(dbChatMessage.getTime());
            chatMessage.setDelivered(dbChatMessage.getDelivered());
            chatMessage.setSent(dbChatMessage.getSent());
            chatMessage.setDeliveredTime(dbChatMessage.getDeliveredTime());
            chatMessage.setType(dbChatMessage.getType());
            chatMessage.setRoomId(dbChatMessage.getRoomId());
            chatMessage.setImageUrl(dbChatMessage.getImageUrl());
            chatMessage.setNexgeUserName(dbChatMessage.getNexgeUserName());
            chatMessage.setYouserId(dbChatMessage.getYouserId());
            chatMessage.setChatProfileUserName(dbChatMessage.getChatProfileUserName());
            chatMessage.setRoomName(dbChatMessage.getRoomName());
            chatMessage.setRoomImage(dbChatMessage.getRoomImage());
            chatMessage.setMessageKey(dbChatMessage.getMessageKey());

            //MyClass myClass = myData.getMyMap().where().equalTo("key", myKey).firstFirst()
            //chatMessage.setTimeStampMap(dbChatMessage.getServerTimeStampReceived());
            chatMessage.setServerTimeStampReceived(dbChatMessage.getServerTimeStampReceived());

            chatMessageList.add(chatMessage);
        }

        return chatMessageList;
    }

}

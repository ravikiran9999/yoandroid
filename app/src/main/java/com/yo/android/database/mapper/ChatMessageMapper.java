package com.yo.android.database.mapper;

import com.yo.android.database.model.ChatMessage;

public class ChatMessageMapper {

    public static ChatMessage map(com.yo.android.model.ChatMessage chatMessage) {

        ChatMessage dbChatMessage = new ChatMessage();
        try {
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
        }catch (Exception e) {
            e.printStackTrace();
        }
        return dbChatMessage;
    }
}

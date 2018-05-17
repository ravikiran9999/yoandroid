package com.yo.android.database.local;

import com.yo.android.database.idatabase.IYoDataSource;
import com.yo.android.database.model.ChatMessage;
import com.yo.android.database.model.UserProfile;
import com.yo.android.database.model.UserRooms;

import java.util.List;

public class YoDataSource implements IYoDataSource {

    private ChatMessageDAO chatMessageDAO;
    private UserProfileDAO userProfileDAO;
    private RoomDao roomDao;

    private static YoDataSource mInstance;

    private YoDataSource(ChatMessageDAO chatMessageDAO, UserProfileDAO userProfileDAO, RoomDao roomDao) {
        this.chatMessageDAO = chatMessageDAO;
        this.userProfileDAO = userProfileDAO;
        this.roomDao = roomDao;
    }

    public static YoDataSource getInstance(ChatMessageDAO chatMessageDAO, UserProfileDAO userProfileDAO, RoomDao roomDao) {
        if (mInstance == null) {
            mInstance = new YoDataSource(chatMessageDAO, userProfileDAO, roomDao);
        }
        return mInstance;
    }

    @Override
    public void insetChatMessage(ChatMessage... chatMessages) {
        chatMessageDAO.insetChatMessage(chatMessages);
    }

    @Override
    public void updateChatMessage(ChatMessage... chatMessages) {
        chatMessageDAO.updateChatMessage(chatMessages);
    }

    @Override
    public void insertUserProfile(UserProfile... userProfiles) {
        userProfileDAO.insertUserProfile(userProfiles);
    }

    @Override
    public void updateUserProfile(UserProfile... userProfiles) {
        userProfileDAO.updateChatMessage(userProfiles);
    }

    @Override
    public void insertUserRoom(UserRooms... rooms) {
        roomDao.insertUserRooms(rooms);
    }

    @Override
    public void updateUserRoom(UserRooms... rooms) {
        roomDao.updateUserRooms(rooms);
    }


}

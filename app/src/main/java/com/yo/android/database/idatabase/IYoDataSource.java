package com.yo.android.database.idatabase;

import com.yo.android.database.model.ChatMessage;
import com.yo.android.database.model.UserProfile;
import com.yo.android.database.model.UserRooms;

import java.util.List;

public interface IYoDataSource {
    //chat message
    void insetChatMessage(ChatMessage... chatMessages);
    void updateChatMessage(ChatMessage... chatMessages);

    // user profile
    void insertUserProfile(UserProfile... userProfiles);
    void updateUserProfile(UserProfile... userProfiles);

    // user rooms
    void insertUserRoom(UserRooms... rooms);
    void updateUserRoom(UserRooms... rooms);
}

package com.yo.android.database.idatabase;

import com.yo.android.database.model.ChatMessage;
import com.yo.android.database.model.UserProfile;
import com.yo.android.database.model.UserRooms;

import java.util.List;

public class DataRepository implements IYoDataSource {

    private IYoDataSource mYoDataSource;
    private static DataRepository mInstance;

    private DataRepository(IYoDataSource mYoDataSource) {
        this.mYoDataSource = mYoDataSource;
    }

    public static DataRepository getInstance(IYoDataSource iYoDataSource) {
        if (mInstance == null) {
            mInstance = new DataRepository(iYoDataSource);
        }
        return mInstance;
    }

    @Override
    public void insetChatMessage(ChatMessage... chatMessages) {
        mYoDataSource.insetChatMessage(chatMessages);
    }

    @Override
    public void updateChatMessage(ChatMessage... chatMessages) {
        mYoDataSource.updateChatMessage(chatMessages);
    }

    @Override
    public void insertUserProfile(UserProfile... userProfiles) {
        mYoDataSource.insertUserProfile(userProfiles);
    }

    @Override
    public void updateUserProfile(UserProfile... userProfiles) {
        mYoDataSource.updateUserProfile(userProfiles);
    }

    @Override
    public void insertUserRoom(UserRooms... rooms) {
        mYoDataSource.insertUserRoom(rooms);
    }

    @Override
    public void updateUserRoom(UserRooms... rooms) {
        mYoDataSource.updateUserRoom(rooms);
    }


}

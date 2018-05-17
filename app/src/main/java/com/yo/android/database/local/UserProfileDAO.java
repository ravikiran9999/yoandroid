package com.yo.android.database.local;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Update;

import com.yo.android.database.model.UserProfile;

@Dao
public interface UserProfileDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertUserProfile(UserProfile... userProfiles);

    @Update
    void updateChatMessage(UserProfile... userProfiles);
}


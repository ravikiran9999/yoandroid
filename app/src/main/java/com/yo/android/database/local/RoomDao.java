package com.yo.android.database.local;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Update;

import com.yo.android.database.model.UserRooms;

@Dao
public interface RoomDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertUserRooms(UserRooms... rooms);

    @Update
    void updateUserRooms(UserRooms... rooms);
}

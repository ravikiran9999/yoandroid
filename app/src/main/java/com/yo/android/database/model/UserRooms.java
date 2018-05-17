package com.yo.android.database.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import com.yo.android.database.RoomTypeConverters;

@Entity(tableName = "UserRooms")
public class UserRooms {
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "room_id")
    private String roomId;
    @ColumnInfo(name = "status")
    private boolean status;

    public UserRooms(@NonNull String roomId, boolean status) {
        this.roomId = roomId;
        this.status = status;
    }

    @NonNull
    public String getRoomId() {
        return roomId;
    }

    public boolean isStatus() {
        return status;
    }
}

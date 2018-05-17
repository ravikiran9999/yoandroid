package com.yo.android.database;

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yo.android.database.model.UserRooms;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class RoomTypeConverters {

    @TypeConverter
    public static List<UserRooms> stringToRoomList(String data) {
        if (data == null) {
            return Collections.emptyList();
        }

        Type listType = new TypeToken<List<UserRooms>>() {}.getType();

        return new Gson().fromJson(data, listType);
    }

    @TypeConverter
    public static String roomListToString(List<UserRooms> someObjects) {
        return new Gson().toJson(someObjects);
    }
}

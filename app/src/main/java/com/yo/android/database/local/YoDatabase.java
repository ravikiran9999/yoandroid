package com.yo.android.database.local;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.yo.android.database.model.ChatMessage;
import com.yo.android.database.model.UserProfile;
import com.yo.android.database.model.UserRooms;

import static com.yo.android.database.local.YoDatabase.VERSION_NUMBER;

@Database(entities = {ChatMessage.class,UserProfile.class, UserRooms.class}, version = VERSION_NUMBER, exportSchema = true)
public abstract class YoDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "yodatabase";
    public static final int VERSION_NUMBER = 1;

    public abstract ChatMessageDAO chatMessageDAO();
    public abstract UserProfileDAO userProfileDAO();
    public abstract RoomDao roomDao();

    private static YoDatabase mInstance;

    public static YoDatabase getInstance(Context context) {
        if(mInstance == null) {
            mInstance = Room.databaseBuilder(context.getApplicationContext(), YoDatabase.class, DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }

        return mInstance;
    }

    public static void destroyInstance() {
        mInstance = null;
    }
}

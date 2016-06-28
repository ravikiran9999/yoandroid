package com.yo.android.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.yo.android.model.ChatMessage;

import java.sql.SQLException;

/**
 * Created by rdoddapaneni on 6/27/2016.
 */

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "Yo";
    private static final int DATABASE_VERSION = 1;

    private Dao<ChatMessage, Integer> chatMessageDao;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {

    }

    /**
     * Returns an instance of the data access object
     * @return
     * @throws SQLException
     */
    public Dao<ChatMessage, Integer> getChatDao() throws SQLException {
        if (chatMessageDao == null) {
            chatMessageDao = getDao(ChatMessage.class);
        }
        return chatMessageDao;
    }

}

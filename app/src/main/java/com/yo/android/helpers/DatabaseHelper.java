package com.yo.android.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.yo.android.model.ChatMessage;

import java.sql.SQLException;

/**
 * Created by rdoddapaneni on 6/27/2016.
 */

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "Yo";
    private static final int DATABASE_VERSION = 1;

    private Context context;

    private Dao<ChatMessage, Integer> chatMessageDao;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, ChatMessage.class);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Unable to create datbases", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {

    }

    /**
     * @return Returns an instance of the data access object
     * @throws SQLException
     */
    private Dao<ChatMessage, Integer> getChatDao() throws SQLException {
        if (chatMessageDao == null) {
            chatMessageDao = getDao(ChatMessage.class);
        }
        return chatMessageDao;
    }

    // Insert data

    /**
     * Insert chat message object to ChatMessage table
     * @param obj ChatMessage object
     * @return true or false
     */
    public boolean insertChatObjectToDatabase(ChatMessage obj) {
        Dao<ChatMessage, Integer> chatDao;

        try {
            chatDao = OpenHelperManager.getHelper(context, DatabaseHelper.class).getChatDao();
            chatDao.create(obj);
            Log.i(TAG, "AddedChatObjectToDatabase");
            return true;
        } catch (SQLException e) {
            Log.e(TAG , e.toString());
            return false;
        }
    }

    // Delete data

    /**
     * Delete chat message from ChatMessage table
     * @param message delete selected message
     * @return true or false
     */
    public boolean deleteRowFromDatabase( String message) {
        Dao<ChatMessage, Integer> chatDao;

        try {
            chatDao = OpenHelperManager.getHelper(context, DatabaseHelper.class).getChatDao();
            DeleteBuilder<ChatMessage, Integer> deleteBuilder = chatDao.deleteBuilder();
            deleteBuilder.where().eq("message", message);
            deleteBuilder.delete();
            return true;
        } catch (SQLException e) {
            Log.e(TAG , e.toString());
            return false;
        }
    }
}

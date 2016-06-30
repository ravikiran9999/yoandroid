package com.yo.android.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.yo.android.model.ChatMessage;
import com.yo.android.model.ChatRoom;
import com.yo.android.util.DatabaseConstant;

import java.sql.SQLException;
import java.util.List;


/**
 * Created by rdoddapaneni on 6/27/2016.
 */

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "Yo";
    private static final int DATABASE_VERSION = 1;

    private Context context;

    private Dao<ChatMessage, Integer> chatMessageDao;
    private Dao<ChatRoom, Integer> chatRoomDao;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, ChatMessage.class);
            TableUtils.createTable(connectionSource, ChatRoom.class);
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
    private Dao<ChatMessage, Integer> getChatMessageDao() throws SQLException {
        if (chatMessageDao == null) {
            chatMessageDao = getDao(ChatMessage.class);
        }
        return chatMessageDao;
    }

    private Dao<ChatRoom, Integer> getChatRoomDao() throws SQLException {
        if (chatRoomDao == null) {
            chatRoomDao = getDao(ChatRoom.class);
        }
        return chatRoomDao;
    }


    // Insert data

    /**
     * Insert chat message object to ChatMessage table
     *
     * @param obj ChatMessage object
     * @return true or false
     */
    public boolean insertChatObjectToDatabase(ChatMessage obj) {
        Dao<ChatMessage, Integer> chatDao;
        try {
            chatDao = OpenHelperManager.getHelper(context, DatabaseHelper.class).getChatMessageDao();
            chatDao.create(obj);
            Log.i(TAG, "AddedChatObjectToDatabase");
            return true;
        } catch (SQLException e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    /**
     * Insert chat room object to ChatRoom table
     *
     * @param obj ChatRoom object
     * @return true or false
     */
    public boolean insertChatRoomObjectToDatabase(ChatRoom obj) {
        Dao<ChatRoom, Integer> chatRoomDao;
        try {
            chatRoomDao = OpenHelperManager.getHelper(context, DatabaseHelper.class).getChatRoomDao();
            chatRoomDao.create(obj);
            Log.i(TAG, "Added Chat Room Object To Database");
            return true;
        } catch (SQLException e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    // Delete data

    /**
     * Delete chat message from ChatMessage table
     *
     * @param message delete selected message
     * @return true or false
     */
    public boolean deleteRowFromDatabase(String message) {
        Dao<ChatMessage, Integer> chatDao;

        try {
            chatDao = OpenHelperManager.getHelper(context, DatabaseHelper.class).getChatMessageDao();
            DeleteBuilder<ChatMessage, Integer> deleteBuilder = chatDao.deleteBuilder();
            deleteBuilder.where().eq(DatabaseConstant.MESSAGE, message);
            deleteBuilder.delete();
            return true;
        } catch (SQLException e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    // Read data

    public List<ChatMessage> getChatUsersList() {
        Dao<ChatMessage, Integer> chatDao;
        List<ChatMessage> chatUsersList = null;
        try {
            chatDao = OpenHelperManager.getHelper(context, DatabaseHelper.class).getChatMessageDao();
            QueryBuilder<ChatMessage, Integer> queryBuilder = chatDao.queryBuilder();
            for (int i = 0; i < 5; i++) {
                PreparedQuery<ChatMessage> preparedQuery = queryBuilder.where().eq(DatabaseConstant.MESSAGE, "Welcome" + i).prepare();
                chatUsersList = chatDao.query(preparedQuery);
            }
        } catch (SQLException e) {
            Log.e(TAG, e.toString());
        }
        return chatUsersList;
    }

    public String getRoomId(String yourPhoneNumber, String opponentPhoneNumber) {
        Dao<ChatRoom, Integer> chatRoomDao;
        List<ChatRoom> chatRoomList = null;
        try {
            chatRoomDao = OpenHelperManager.getHelper(context, DatabaseHelper.class).getChatRoomDao();
            QueryBuilder<ChatRoom, Integer> queryBuilder = chatRoomDao.queryBuilder();
            PreparedQuery<ChatRoom> preparedQuery = queryBuilder.where().eq(DatabaseConstant.YOUR_PHONE_NUMBER, yourPhoneNumber).and().eq(DatabaseConstant.OPPONENT_PHONE_NUMBER, opponentPhoneNumber).prepare();
            chatRoomList = chatRoomDao.query(preparedQuery);
            if(chatRoomList.size() > 0) {
                return chatRoomList.get(0).getChatRoomId();
            }
        } catch (SQLException e) {
            Log.e(TAG, e.toString());
        }
        return "";
    }
}

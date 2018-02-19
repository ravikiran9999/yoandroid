package com.yo.android.database;

import android.support.annotation.NonNull;

import com.yo.android.app.BaseApp;
import com.yo.android.database.mapper.DbChatMessageMapper;
import com.yo.android.database.model.DBChatMessage;
import com.yo.android.model.ChatMessage;

import java.util.ArrayList;

import javax.inject.Inject;

import io.realm.Realm;

/**
 * Created by Chaatz on 19-02-2018.
 */

public class ChatMessageDao {

    @Inject
    public ChatMessageDao() {
    }

    public ArrayList<ChatMessage> getAll() {
        try {
            Realm realm = Realm.getInstance(BaseApp.getRealmConfiguration());
            return DbChatMessageMapper.map(realm.where(DBChatMessage.class).findAll());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void save(ChatMessage chatMessage) {
        try {
            Realm realm = Realm.getInstance(BaseApp.getRealmConfiguration());
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(DbChatMessageMapper.map(chatMessage));
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearDatabase() {
        try {
            Realm realm = Realm.getInstance(BaseApp.getRealmConfiguration());
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm realm1) {
                    realm1.delete(DBChatMessage.class);
                    realm1.deleteAll();
                }
            });
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

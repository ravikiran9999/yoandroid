package com.yo.android.database;

import android.support.annotation.NonNull;

import com.yo.android.app.BaseApp;
import com.yo.android.database.mapper.DbRoomMapper;
import com.yo.android.database.model.DBRoom;
import com.yo.android.model.Room;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.realm.Realm;

/**
 * Created by rdoddapaneni on 14-02-2018.
 */

public final class RoomDao {

    @Inject
    public RoomDao() {
    }

    public ArrayList<Room> getAll() {
        try {
            Realm realm = Realm.getInstance(BaseApp.getRealmConfiguration());
            return DbRoomMapper.map(realm.where(DBRoom.class).findAll());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void save(Room room) {
        try {
            Realm realm = Realm.getInstance(BaseApp.getRealmConfiguration());
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(DbRoomMapper.map(room));
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
                    realm1.delete(DBRoom.class);
                    realm1.deleteAll();
                }
            });
            realm.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

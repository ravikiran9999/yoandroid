package com.yo.android.database;

import com.yo.android.database.mapper.DbRoomMapper;
import com.yo.android.database.model.DBRoom;
import com.yo.android.model.Room;

import java.util.List;

import javax.inject.Inject;

import io.realm.Realm;

//import io.realm.Realm;


/**
 * Created by rdoddapaneni on 14-02-2018.
 */

public final class RoomDao {

    @Inject
    public RoomDao() {
    }

    public List<Room> getAll() {
        Realm realm = Realm.getDefaultInstance();
        return DbRoomMapper.map(realm.where(DBRoom.class).findAll());
    }

    public void save(Room room) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(DbRoomMapper.map(room));
        realm.commitTransaction();
    }

}

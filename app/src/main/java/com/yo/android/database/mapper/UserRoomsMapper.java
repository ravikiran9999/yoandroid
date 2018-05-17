package com.yo.android.database.mapper;

import com.firebase.client.DataSnapshot;
import com.yo.android.database.model.UserRooms;
import com.yo.android.util.Constants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UserRoomsMapper {

    public static List<UserRooms> map(DataSnapshot dataSnapshot) {
        ArrayList<UserRooms> roomList = new ArrayList<>();
        try {

            Iterator<DataSnapshot> dataSnapshotIterator = dataSnapshot.child(Constants.MY_ROOMS).getChildren().iterator();
            while (dataSnapshotIterator.hasNext()) {
                String key = dataSnapshotIterator.next().getKey();
                boolean value = (Boolean) dataSnapshotIterator.next().getValue();
                roomList.add(new UserRooms(key, value));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return roomList;
    }
}

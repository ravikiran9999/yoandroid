package com.yo.android.database.mapper;

import com.yo.android.database.model.DBGroupMembers;
import com.yo.android.database.model.DBMembers;
import com.yo.android.database.model.DBRoom;
import com.yo.android.database.model.DBUserProfile;
import com.yo.android.model.GroupMembers;
import com.yo.android.model.Members;
import com.yo.android.model.Room;
import com.yo.android.model.UserProfile;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;

//import io.realm.RealmList;

/**
 * Created by rdoddapaneni on 15-02-2018.
 */

public class DbRoomMapper {

    public static DBRoom map(Room room) {
        DBRoom dbRoom = new DBRoom();
        dbRoom.setId(room.getId());
        dbRoom.setFirebaseRoomId(room.getFirebaseRoomId());
        dbRoom.setFirebaseUserId(room.getFirebaseUserId());
        dbRoom.setImage(room.getImage());
        dbRoom.setPresence(room.getPresence());
        dbRoom.setLastChat(room.getLastChat());
        dbRoom.setTimeStamp(room.getTimeStamp());
        dbRoom.setImages(room.isImages());
        dbRoom.setGroupName(room.getGroupName());

        dbRoom.setGroupCreationTime(room.getGroupCreationTime());
        dbRoom.setTime(room.getTime());
        dbRoom.setFullName(room.getFullName());
        dbRoom.setMobileNumber(room.getMobileNumber());
        dbRoom.setPhoneNumber(room.getPhoneNumber());
        dbRoom.setYouserId(room.getYouserId());
        dbRoom.setCreated_at(room.getCreated_at());
        dbRoom.setNexgeUserName(room.getNexgeUserName());

        RealmList<DBMembers> dbMembersList = new RealmList<>();
        for (Members members : room.getMembers()) {
            DBMembers dbMembers = new DBMembers();
            dbMembers.setId(members.getId());
            dbMembers.setMobileNumber(members.getMobileNumber());

            dbMembersList.add(dbMembers);
        }
        dbRoom.setMembers(dbMembersList);

        RealmList<DBGroupMembers> dbGroupMembersList = new RealmList<>();
        for (GroupMembers groupMembers : room.getGroupMembers()) {
            DBGroupMembers dbGroupMembers = new DBGroupMembers();
            dbGroupMembers.setAdmin(groupMembers.getAdmin());
            dbGroupMembers.setUserId(groupMembers.getUserId());
            dbGroupMembers.setUserProfile(mapToDbUserProfile(groupMembers.getUserProfile()));

            dbGroupMembersList.add(dbGroupMembers);
        }
        dbRoom.setGroupMembers(dbGroupMembersList);

        return dbRoom;
    }

    // map to dbUserProfile
    private static DBUserProfile mapToDbUserProfile(UserProfile userProfile) {
        DBUserProfile dbUserProfile = new DBUserProfile();
        dbUserProfile.setFullName(userProfile.getFullName());
        dbUserProfile.setCountryCode(userProfile.getCountryCode());
        dbUserProfile.setMobileNumber(userProfile.getMobileNumber());
        dbUserProfile.setPhoneNumber(userProfile.getPhoneNumber());
        dbUserProfile.setImage(userProfile.getImage());
        dbUserProfile.setFirebaseRoomId(userProfile.getFirebaseRoomId());
        dbUserProfile.setNexgeUserName(userProfile.getNexgeUserName());

        return dbUserProfile;
    }

    // retrieve list of rooms
    public static ArrayList<Room> map(List<DBRoom> dbRooms) {
        ArrayList<Room> rooms = new ArrayList<>();

        for (DBRoom dbRoom : dbRooms) {
            Room room = new Room();
            room.setId(dbRoom.getId());
            room.setFirebaseRoomId(dbRoom.getFirebaseRoomId());
            room.setFirebaseUserId(dbRoom.getFirebaseUserId());
            room.setImage(dbRoom.getImage());
            room.setPresence(dbRoom.getPresence());
            room.setLastChat(dbRoom.getLastChat());
            room.setTimeStamp(dbRoom.getTimeStamp());
            room.setImages(dbRoom.isImages());
            room.setGroupName(dbRoom.getGroupName());

            room.setGroupCreationTime(dbRoom.getGroupCreationTime());
            room.setTime(dbRoom.getTime());
            room.setFullName(dbRoom.getFullName());
            room.setMobileNumber(dbRoom.getMobileNumber());
            room.setPhoneNumber(dbRoom.getPhoneNumber());
            room.setYouserId(dbRoom.getYouserId());
            room.setCreated_at(dbRoom.getCreated_at());
            room.setNexgeUserName(dbRoom.getNexgeUserName());

            room.setMembers(mapMembers(dbRoom.getMembers()));
            room.setGroupMembers(mapGroupMembers(dbRoom.getGroupMembers()));
        }

        return rooms;
    }

    public static ArrayList<Members> mapMembers(List<DBMembers> dbMembers) {
        ArrayList<Members> members = new ArrayList<>();

        for (DBMembers dbMember : dbMembers) {
            Members member = new Members();
            member.setId(dbMember.getId());
            member.setMobileNumber(dbMember.getMobileNumber());

            members.add(member);
        }

        return members;
    }

    public static ArrayList<GroupMembers> mapGroupMembers(List<DBGroupMembers> dbGroupMembers) {
        ArrayList<GroupMembers> groupMembers = new ArrayList<>();

        for (DBGroupMembers dbGroupMember : dbGroupMembers) {
            GroupMembers groupMember = new GroupMembers();
            groupMember.setAdmin(dbGroupMember.getAdmin());
            groupMember.setUserId(dbGroupMember.getUserId());
            groupMember.setUserProfile(mapUserProfile(dbGroupMember.getUserProfile()));

            groupMembers.add(groupMember);
        }

        return groupMembers;
    }

    // map to userProfile
    private static UserProfile mapUserProfile(DBUserProfile dbUserProfile) {
        UserProfile userProfile = new UserProfile();
        userProfile.setFullName(dbUserProfile.getFullName());
        userProfile.setCountryCode(dbUserProfile.getCountryCode());
        userProfile.setMobileNumber(dbUserProfile.getMobileNumber());
        userProfile.setPhoneNumber(dbUserProfile.getPhoneNumber());
        userProfile.setImage(dbUserProfile.getImage());
        userProfile.setFirebaseRoomId(dbUserProfile.getFirebaseRoomId());
        userProfile.setNexgeUserName(dbUserProfile.getNexgeUserName());

        return userProfile;
    }
}

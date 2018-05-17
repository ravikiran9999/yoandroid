package com.yo.android.database.mapper;

import com.yo.android.database.model.UserProfile;

public class UserProfileMapper {

    public static UserProfile map(com.yo.android.model.UserProfile userProfile, String userFirebaseId) {

        UserProfile dbUserProfile = new UserProfile();
        dbUserProfile.setUserId(userFirebaseId);
        assert userProfile != null;
        dbUserProfile.setFullName(userProfile.getFullName());
        dbUserProfile.setCountryCode(userProfile.getCountryCode());
        dbUserProfile.setMobileNumber(userProfile.getMobileNumber());
        dbUserProfile.setPhoneNumber(userProfile.getPhoneNumber());
        dbUserProfile.setImage(userProfile.getImage());
        dbUserProfile.setFirebaseRoomId(userProfile.getFirebaseRoomId());
        dbUserProfile.setNexgeUserName(userProfile.getNexgeUserName());

        return dbUserProfile;
    }
}

package com.yo.android.database.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import com.yo.android.database.RoomTypeConverters;

import java.util.List;

@Entity(tableName = "UserProfile")
public class UserProfile {

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "firebase_user_id")
    private String userId;
    @ColumnInfo(name = "firebase_room_id")
    private String firebaseRoomId;
    @ColumnInfo(name = "nexge_user_name")
    private String nexgeUserName;
    @ColumnInfo(name = "full_name")
    private String fullName;
    @ColumnInfo(name = "mobile_number")
    private String mobileNumber;
    @ColumnInfo(name = "phone_number")
    private String phoneNumber;
    @ColumnInfo(name = "country_code")
    private String countryCode;
    @ColumnInfo(name = "image")
    private String image;

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getFirebaseRoomId() {
        return firebaseRoomId;
    }

    public void setFirebaseRoomId(String firebaseRoomId) {
        this.firebaseRoomId = firebaseRoomId;
    }

    public String getNexgeUserName() {
        return nexgeUserName;
    }

    public void setNexgeUserName(String nexgeUserName) {
        this.nexgeUserName = nexgeUserName;
    }
}

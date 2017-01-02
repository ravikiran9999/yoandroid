
package com.yo.android.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Contact implements Parcelable {

    private String id;
    private String name;
    private String image;
    private String phoneNo;
    private boolean yoAppUser;
    private boolean selected;
    private String firebaseRoomId;
    private String voxUserName;
    private String countryCode;


    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }


    public String getVoxUserName() {
        return voxUserName;
    }

    public void setVoxUserName(String voxUserName) {
        this.voxUserName = voxUserName;
    }


    public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel source) {
            return new Contact(source);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };

    public Contact() {
        // empty default constructor, necessary for Firebase to be able to deserialize
    }

    public Contact(String phoneNo, String name) {
        this.phoneNo = phoneNo;
        this.name = name;
    }

    private Contact(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.image = in.readString();
        this.phoneNo = in.readString();
        this.firebaseRoomId = in.readString();
        this.yoAppUser = in.readInt() == 0;
        this.voxUserName = in.readString();
        this.countryCode = in.readString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public boolean getYoAppUser() {
        return yoAppUser;
    }

    public void setYoAppUser(boolean yoAppUser) {
        this.yoAppUser = yoAppUser;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getFirebaseRoomId() {
        return firebaseRoomId;
    }

    public void setFirebaseRoomId(String firebaseRoomId) {
        this.firebaseRoomId = firebaseRoomId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(image);
        dest.writeString(phoneNo);
        dest.writeString(firebaseRoomId);
        dest.writeInt(yoAppUser ? 0 : 1);
        dest.writeString(voxUserName);
        dest.writeString(countryCode);
    }

    /*@Override
    public String toString() {
        return " Phone Number = "+phoneNo+", voxUserName = "+voxUserName+", Country Code = "+countryCode;
    }*/


}

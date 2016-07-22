
package com.yo.android.model;

public class Contact {

    private String id;
    private String name;
    private String image;
    private String phoneNo;
    private boolean yoAppUser;
    private boolean selected;
    private String firebaseRoomId;

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
    public String toString() {
        return "ClassPojo [id = " + id +", phoneNo = " + phoneNo + ", yoAppUser = " + yoAppUser + ", firebaseRoomId = " +firebaseRoomId + "]";
    }
}

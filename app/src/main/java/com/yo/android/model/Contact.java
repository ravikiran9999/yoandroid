
package com.yo.android.model;

public class Contact {


    private String name;
    private String phoneNo;
    private Boolean yoAppUser;


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
    public Boolean getYoAppUser() {
        return yoAppUser;
    }
    public void setYoAppUser(Boolean yoAppUser) {
        this.yoAppUser = yoAppUser;
    }

    @Override
    public String toString() {
        return "ClassPojo [phoneNo = " + phoneNo + ", yoAppUser = " + yoAppUser + "]";
    }
}

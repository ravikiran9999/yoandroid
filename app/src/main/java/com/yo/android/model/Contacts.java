package com.yo.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by rdoddapaneni on 7/5/2016.
 */

public class Contacts implements Parcelable {
    //attributes
    private long Id;
    private String mFirstName;
    private String mLastName;
    private ArrayList<PhNumberBean> mCotactNumber;
    private String mEmail;
    private String mAddress;
    private String mZip;

    private boolean selected = false;

    public Contacts(String mFirstName, String mLastName, String mEmail, String mAddress, String mZip) {
        this.mFirstName = mFirstName;
        this.mLastName = mLastName;
        this.mEmail = mEmail;
        this.mAddress = mAddress;
        this.mZip = mZip;
    }

    public Contacts() {
    }

    /**
     * gets list of phone numbers
     */
    public ArrayList<PhNumberBean> getmCotactNumber() {
        return mCotactNumber;
    }

    public void setmCotactNumber(ArrayList<PhNumberBean> mCotactNumber) {
        this.mCotactNumber = mCotactNumber;
    }

    public void setmLastName(String mLastName) {
        this.mLastName = mLastName;
    }

    public String getmFirstName() {
        return mFirstName;
    }

    public void setmFirstName(String mFirstName) {
        this.mFirstName = mFirstName;
    }

    public String getmLastName() {
        return mLastName;
    }

    public String getmEmail() {
        return mEmail;
    }

    public void setmEmail(String mEmail) {
        this.mEmail = mEmail;
    }

    public String getmAddress() {
        return mAddress;
    }

    public void setmAddress(String mAddress) {
        this.mAddress = mAddress;
    }

    public String getmZip() {
        return mZip;
    }

    public void setmZip(String mZip) {
        this.mZip = mZip;
    }

    public long getId() {
        return Id;
    }

    public void setId(long id2) {
        Id = id2;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private Contacts(Parcel in) {
        this.mFirstName = in.readString();
        this.mLastName = in.readString();
        this.mEmail = in.readString();
        this.mAddress = in.readString();
        this.mZip = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mFirstName);
        dest.writeString(mLastName);
        dest.writeString(mEmail);
        dest.writeString(mAddress);
        dest.writeString(mZip);

    }

    // Just cut and paste this for now
    public static final Parcelable.Creator<Contacts> CREATOR = new Parcelable.Creator<Contacts>() {
        @Override
        public Contacts createFromParcel(Parcel source) {
            return new Contacts(source);
        }

        @Override
        public Contacts[] newArray(int size) {
            return new Contacts[size];
        }
    };
}

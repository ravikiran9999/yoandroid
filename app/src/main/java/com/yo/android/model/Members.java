package com.yo.android.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by rdoddapaneni on 7/21/2016.
 */

public class Members implements Parcelable {

    private String id;
    private String mobileNumber;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(mobileNumber);
    }

    public Members(Parcel in) {
        this.id = in.readString();
        this.mobileNumber = in.readString();
    }

    public static final Parcelable.Creator<Members> CREATOR = new Parcelable.Creator<Members>() {
        @Override
        public Members createFromParcel(Parcel source) {
            return new Members(source);
        }

        @Override
        public Members[] newArray(int size) {
            return new Members[size];
        }
    };
}

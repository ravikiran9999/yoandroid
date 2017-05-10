package com.yo.android.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by rdoddapaneni on 5/10/2017.
 */

public class Share implements Parcelable {

    private String text;
    private Uri uri;
    private String type;

    public Share() {

    }

    protected Share(Parcel in) {
        text = in.readString();
        type = in.readString();
        uri = in.readParcelable(Uri.class.getClassLoader());
    }

    public static final Creator<Share> CREATOR = new Creator<Share>() {
        @Override
        public Share createFromParcel(Parcel in) {
            return new Share(in);
        }

        @Override
        public Share[] newArray(int size) {
            return new Share[size];
        }
    };

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Uri getUri() {
        return uri;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeString(text);
        dest.writeParcelable(uri, flags);
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

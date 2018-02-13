package com.yo.android.model.dialer;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CallLogsResult implements Comparable<CallLogsResult>, Parcelable {
    //    {
//        "stime": "2014­11­28  14:04:33",
//            "billsec": "0",
//            "salerate": "0.210000",
//            "salecost": "0.00000",
//            "dialnumber": "7921 181533",
//            "destination_name": "SIP2SIP Call",
//            "dialedstatus": "NOT  ANSWER"
//    }
    private String id;
    private String salerate;
    private String salecost;
    private String dialnumber;
    private String dialedstatus;
    private String stime;
    private String billsec;
    private String destination_name;
    private boolean header;
    private String headerTitle;
    private int callType;
    private String image;

    public int getAppOrPstn() {
        return appOrPstn;
    }

    public void setAppOrPstn(int appOrPstn) {
        this.appOrPstn = appOrPstn;
    }

    private int appOrPstn;


    private String duration;

    public CallLogsResult() {

    }

    protected CallLogsResult(Parcel in) {
        salerate = in.readString();
        salecost = in.readString();
        dialnumber = in.readString();
        dialedstatus = in.readString();
        stime = in.readString();
        billsec = in.readString();
        destination_name = in.readString();
        headerTitle = in.readString();
        callType = in.readInt();
        image = in.readString();
        duration = in.readString();
        appOrPstn = in.readInt();
        id = in.readString();
    }

    public static final Creator<CallLogsResult> CREATOR = new Creator<CallLogsResult>() {
        @Override
        public CallLogsResult createFromParcel(Parcel in) {
            return new CallLogsResult(in);
        }

        @Override
        public CallLogsResult[] newArray(int size) {
            return new CallLogsResult[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getSalerate() {
        return salerate;
    }

    public void setSalerate(String salerate) {
        this.salerate = salerate;
    }

    public String getSalecost() {
        return salecost;
    }

    public void setSalecost(String salecost) {
        this.salecost = salecost;
    }

    public String getDialnumber() {
        return dialnumber;
    }

    public void setDialnumber(String dialnumber) {
        this.dialnumber = dialnumber;
    }

    public String getDialedstatus() {
        return dialedstatus;
    }

    public void setDialedstatus(String dialedstatus) {
        this.dialedstatus = dialedstatus;
    }

    public String getStime() {
        return stime;
    }

    public void setStime(String stime) {
        this.stime = stime;
    }

    public String getBillsec() {
        return billsec;
    }

    public void setBillsec(String billsec) {
        this.billsec = billsec;
    }

    public String getDestination_name() {
        return destination_name;
    }

    public void setDestination_name(String destination_name) {
        this.destination_name = destination_name;
    }

    public int getCallType() {
        return callType;
    }

    public void setCallType(int callType) {
        this.callType = callType;
    }

    public boolean isHeader() {
        return header;
    }

    public void setHeader(boolean header) {
        this.header = header;
    }

    public String getHeaderTitle() {
        return headerTitle;
    }

    public void setHeaderTitle(String headerTitle) {
        this.headerTitle = headerTitle;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


    @Override
    public int compareTo(CallLogsResult another) {
        DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        try {
            Date startDate = formatter.parse(getStime());
            Date endDate = formatter.parse(another.getStime());
            return startDate.compareTo(endDate);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(salerate);
        dest.writeString(salecost);
        dest.writeString(dialnumber);
        dest.writeString(dialedstatus);
        dest.writeString(stime);
        dest.writeString(billsec);
        dest.writeString(destination_name);
        dest.writeString(headerTitle);
        dest.writeInt(callType);
        dest.writeString(image);
        dest.writeString(duration);
        dest.writeInt(appOrPstn);
        dest.writeString(id);

    }
}
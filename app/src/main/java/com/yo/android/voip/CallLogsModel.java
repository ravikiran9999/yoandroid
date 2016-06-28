package com.yo.android.voip;

import com.j256.ormlite.field.DatabaseField;

/**
 * Created by Ramesh on 28/6/16.
 */
public class CallLogsModel {

    @DatabaseField(generatedId = true, columnName = "_id")
    private int _id;

    @DatabaseField(columnName = "caller_name")
    private String callerName;

    @DatabaseField(columnName = "caller_no")
    private String callerNo;

    @DatabaseField(columnName = "call_type")
    private int callType;

    @DatabaseField(columnName = "call_mode")
    private int callMode;

    @DatabaseField(columnName = "call_time")
    private long callTime;

    public void setId(int id) {
        this._id = id;
    }

    public void setCallerName(String callerName) {
        this.callerName = callerName;
    }

    public void setCallerNo(String callerNo) {
        this.callerNo = callerNo;
    }

    public void setCallType(int callType) {
        this.callType = callType;
    }

    public void setCallMode(int callMode) {
        this.callMode = callMode;
    }

    public void setCallTime(long callTime) {
        this.callTime = callTime;
    }

    public int getId() {
        return _id;
    }

    public String getCallerName() {
        return callerName;
    }

    public String getCallerNo() {
        return callerNo;
    }

    public int getCallType() {
        return callType;
    }

    public int getCallMode() {
        return callMode;
    }

    public long getCallTime() {
        return callTime;
    }


}

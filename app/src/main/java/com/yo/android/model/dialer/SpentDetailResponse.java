package com.yo.android.model.dialer;
import com.google.gson.annotations.SerializedName;

public class SpentDetailResponse {
    @SerializedName("MESSAGE")
    private String message;
    @SerializedName("TOTALRECORDS")
    private String totalrecords;
    @SerializedName("STATUS")
    private String status;
    @SerializedName("DATA")
    private SpendDetailsData data;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTotalrecords() {
        return totalrecords;
    }

    public void setTotalrecords(String totalrecords) {
        this.totalrecords = totalrecords;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public SpendDetailsData getData() {
        return data;
    }

    public void setData(SpendDetailsData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ClassPojo [message = " + message + ", totalrecords = " + totalrecords + ", status = " + status + ", data = " + data + "]";
    }
}
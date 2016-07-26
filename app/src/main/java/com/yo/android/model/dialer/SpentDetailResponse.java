package com.yo.android.model.dialer;

public class SpentDetailResponse {
    private String message;

    private String totalrecords;

    private String status;

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
package com.yo.dialer.googlesheet;

import java.io.Serializable;

/**
 * Created by root on 4/9/17.
 */

public class UploadModel implements Serializable {
    private String name;
    private String caller;
    private String callee;
    private String statusCode;
    private String statusReason;
    private String comments;
    private String duration;
    private String callType;
    private String dateTime;
    private String currentBalance;
    private Object notificationType;
    private Object notificationDetails;
    private String regId;

    public String getRegId() {
        return regId;
    }

    public void setRegId(String regId) {
        this.regId = regId;
    }

    public Object getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(Object notificationType) {
        this.notificationType = notificationType;
    }

    public Object getNotificationDetails() {
        return notificationDetails;
    }

    public void setNotificationDetails(Object notificationDetails) {
        this.notificationDetails = notificationDetails;
    }

    public String getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(String currentBalance) {
        this.currentBalance = currentBalance;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }



    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    public String getCallee() {
        return callee;
    }

    public void setCallee(String callee) {
        this.callee = callee;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}

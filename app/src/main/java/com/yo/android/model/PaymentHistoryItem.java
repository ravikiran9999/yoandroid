package com.yo.android.model;

import com.google.gson.annotations.SerializedName;

public class PaymentHistoryItem {
    //    @SerializedName("CURRENT_CREDIT")
    private String current_credit;
    //    @SerializedName("PDATED_AT")
    private String updated_at;
    //    @SerializedName("ADDED_CREDIT")
    private String added_credit;
    //    @SerializedName("STATUS")
    private String status;
    //    @SerializedName("EXISTING_CREDIT")
    private String existing_credit;

    private String currencySymbol;
    private String currencyCode;

    private String convertedAddedCredit;

    private boolean arrowDown;

    private String message;

    public String getConvertedAddedCredit() {
        return convertedAddedCredit;
    }

    public void setConvertedAddedCredit(String convertedAddedCredit) {
        this.convertedAddedCredit = convertedAddedCredit;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }


    public String getCurrent_credit() {
        return current_credit;
    }

    public void setCurrent_credit(String current_credit) {
        this.current_credit = current_credit;
    }

    public String getUpdatedAt() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getAddedCredit() {
        return added_credit;
    }

    public void setAdded_credit(String added_credit) {
        this.added_credit = added_credit;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExisting_credit() {
        return existing_credit;
    }

    public void setExisting_credit(String existing_credit) {
        this.existing_credit = existing_credit;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    @Override
    public String toString() {
        return "ClassPojo [current_credit = " + current_credit + ", updated_at = " + updated_at + ", added_credit = " + added_credit + ", status = " + status + ", existing_credit = " + existing_credit + ", currencySymbol = " + currencySymbol + ",currencyCode = " + currencyCode + ", message = " + message + "]";
    }

    public boolean isArrowDown() {
        return arrowDown;
    }

    public void setArrowDown(boolean arrowDown) {
        this.arrowDown = arrowDown;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
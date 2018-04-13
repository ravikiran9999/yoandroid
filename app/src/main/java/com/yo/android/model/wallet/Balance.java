package com.yo.android.model.wallet;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Balance implements Serializable {

    @SerializedName("code")
    private int code;
    @SerializedName("status")
    private String status;
    @SerializedName("data")
    private String data;
    @SerializedName("accountType")
    private String accountType;
    @SerializedName("statusCode")
    private String statusCode;
    @SerializedName("Subscriber")
    private String subscriber;
    @SerializedName("Balance")
    private String balance;
    @SerializedName("creditLimit")
    private String creditLimit;
    @SerializedName("SwitchBalance")
    private String switchBalance;
    @SerializedName("WalletBalance")
    private String walletBalance;


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(String subscriber) {
        this.subscriber = subscriber;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(String creditLimit) {
        this.creditLimit = creditLimit;
    }

    public String getSwitchBalance() {
        return switchBalance;
    }

    public void setSwitchBalance(String switchBalance) {
        this.switchBalance = switchBalance;
    }

    public String getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(String walletBalance) {
        this.walletBalance = walletBalance;
    }
}

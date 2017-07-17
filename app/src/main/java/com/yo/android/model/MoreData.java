package com.yo.android.model;

/**
 * Created by root on 15/7/16.
 */
public class MoreData {


    private String name;
    private boolean hasOptions;
    private String balance;

    public MoreData() {
    }

    public MoreData(String name, boolean bool, String balance) {
        this.name = name;
        this.hasOptions = bool;
        this.balance = balance;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    public boolean isHasOptions() {
        return hasOptions;
    }

    public String getBalance() {
        return balance;
    }
}

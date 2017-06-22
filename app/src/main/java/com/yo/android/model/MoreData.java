package com.yo.android.model;

/**
 * Created by root on 15/7/16.
 */
public class MoreData {
    public void setName(String name) {
        this.name = name;
    }

    private String name;
    private boolean hasOptions;
    private String balance;

    public MoreData(String name, boolean bool, String balance) {
        this.name = name;
        this.hasOptions = bool;
        this.balance = balance;
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

package com.yo.android.model;

/**
 * Created by rdoddapaneni on 6/19/2017.
 */

public class Wallet {

    private String balance;
    private String switchBalance;
    private String walletBalance;
    private String balanceDescription;

    public Wallet(String balance, String balanceDescription) {
        this.balance = balance;
        this.balanceDescription = balanceDescription;
    }

    public String getTotalBalance() {
        return balance;
    }

    public void setTotalBalance(String totalBalance) {
        this.balance = totalBalance;
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

    public String getBalanceDescription() {
        return balanceDescription;
    }
}

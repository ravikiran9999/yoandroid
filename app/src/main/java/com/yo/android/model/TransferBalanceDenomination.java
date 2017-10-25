
package com.yo.android.model;

import java.io.Serializable;
import com.google.gson.annotations.SerializedName;

public class TransferBalanceDenomination implements Serializable
{

    @SerializedName("id")
    private String id;
    @SerializedName("denomination")
    private String denomination;
    @SerializedName("status")
    private String status;
    @SerializedName("country")
    private String country;
    @SerializedName("currencyName")
    private String currencyName;
    @SerializedName("currencySymbol")
    private String currencySymbol;

    private final static long serialVersionUID = 2021235789532166265L;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDenomination() {
        return denomination;
    }

    public void setDenomination(String denomination) {
        this.denomination = denomination;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

}

package com.yo.android.model.denominations;

import com.google.gson.annotations.SerializedName;

/**
 * Created by rajesh on 22/11/16.
 */
public class Denominations {

    @SerializedName("id")
    private String id;

    @SerializedName("denomination")
    private float denomination;

    @SerializedName("productID")
    private String productID;

    @SerializedName("country")
    private String country;

    @SerializedName("currencySymbol")
    private String currencySymbol;

    @SerializedName("currencyName")
    private String currencyName;


    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getDenomination() {
        return denomination;
    }

    public void setDenomination(float denomination) {
        this.denomination = denomination;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }


}

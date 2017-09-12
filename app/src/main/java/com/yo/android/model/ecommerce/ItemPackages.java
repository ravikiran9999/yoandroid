package com.yo.android.model.ecommerce;

import com.google.gson.annotations.SerializedName;

/**
 * Created by admin on 9/11/2017.
 */

public class ItemPackages {

    private String packagePrice;
    private boolean visibleSale;

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


    public ItemPackages(String packagePrice, boolean visibleSale) {
        this.packagePrice = packagePrice;
        this.visibleSale = visibleSale;
    }

    public String getPackagePrice() {
        return packagePrice;
    }

    public boolean isVisibleSale() {
        return visibleSale;
    }

    public float getDenomination() {
        return denomination;
    }

    public String getProductID() {
        return productID;
    }

    public String getCountry() {
        return country;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public String getCurrencyName() {
        return currencyName;
    }
}

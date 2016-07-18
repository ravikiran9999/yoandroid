package com.yo.android.model;

/**
 * Created by pcs-03.
 */
public class CountryCode {

    private String countryName;

    private String countryID;

    private String countryCode;

    public String getCountryID() {
        return countryID;
    }

    public void setCountryID(String countryID) {
        this.countryID = countryID;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    @Override
    public String toString() {
        return "(+" + getCountryCode() + ") " + this.countryName;
    }


}

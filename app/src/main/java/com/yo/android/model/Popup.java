package com.yo.android.model;

import com.yo.android.helpers.PopupHelper;

/**
 * Created by creatives on 10/6/2016.
 */
public class Popup {

    private PopupData data;
    private PopupHelper.PopupsEnum popupsEnum;

    public PopupData getData() {
        return data;
    }

    public void setData(PopupData data) {
        this.data = data;
    }

    public PopupHelper.PopupsEnum getPopupsEnum() {
        return popupsEnum;
    }

    public void setPopupsEnum(PopupHelper.PopupsEnum popupsEnum) {
        this.popupsEnum = popupsEnum;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Popup) {
            Popup popup = (Popup) obj;
            return (popup.getData().getId().equals(getData().getId()));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = getData().getId().hashCode();
        return hash;
    }
}

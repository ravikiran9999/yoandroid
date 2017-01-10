package com.yo.android.model;

import com.yo.android.helpers.PopupHelper;

/**
 * Created by creatives on 10/6/2016.
 */
public class Popup {

    //private boolean isMagazines;
    private PopupData data;
    /*private PopupData magazineData;
    private PopupData dailerData;
    private PopupData contactsData;
    private PopupData moreData;
    private PopupData yoCreditData;*/
    /*private boolean isChats;
    private boolean isDialer;
    private boolean isContacts;
    private boolean isMore;
    private boolean isYoCredit;*/
    private PopupHelper.PopupsEnum popupsEnum;

    /*public boolean isMagazines() {
        return isMagazines;
    }

    public void setIsMagazines(boolean isMagazines) {
        this.isMagazines = isMagazines;
    }


    public boolean isChats() {
        return isChats;
    }

    public void setIsChats(boolean isChats) {
        this.isChats = isChats;
    }

    public boolean isDialer() {
        return isDialer;
    }

    public void setIsDialer(boolean isDialer) {
        this.isDialer = isDialer;
    }

    public boolean isContacts() {
        return isContacts;
    }

    public void setIsContacts(boolean isContacts) {
        this.isContacts = isContacts;
    }

    public boolean isMore() {
        return isMore;
    }

    public void setIsMore(boolean isMore) {
        this.isMore = isMore;
    }

    public boolean isYoCredit() {
        return isYoCredit;
    }

    public void setIsYoCredit(boolean isYoCredit) {
        this.isYoCredit = isYoCredit;
    }*/

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

/*public PopupData getChatData() {
        return data;
    }

    public void setChatData(PopupData chatData) {
        this.data = chatData;
    }

    public PopupData getMagazineData() {
        return magazineData;
    }

    public void setMagazineData(PopupData magazineData) {
        this.magazineData = magazineData;
    }

    public PopupData getDailerData() {
        return dailerData;
    }

    public void setDailerData(PopupData dailerData) {
        this.dailerData = dailerData;
    }

    public PopupData getContactsData() {
        return contactsData;
    }

    public void setContactsData(PopupData contactsData) {
        this.contactsData = contactsData;
    }

    public PopupData getMoreData() {
        return moreData;
    }

    public void setMoreData(PopupData moreData) {
        this.moreData = moreData;
    }

    public PopupData getYoCreditData() {
        return yoCreditData;
    }

    public void setYoCreditData(PopupData yoCreditData) {
        this.yoCreditData = yoCreditData;
    }*/

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

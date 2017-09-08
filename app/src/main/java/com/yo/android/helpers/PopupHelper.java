package com.yo.android.helpers;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.chat.ui.fragments.ChatFragment;
import com.yo.android.chat.ui.fragments.ContactsFragment;
import com.yo.android.model.Popup;
import com.yo.android.model.PopupData;
import com.yo.android.ui.fragments.DialerFragment;
import com.yo.android.ui.fragments.MagazinesFragment;
import com.yo.android.ui.fragments.MoreFragment;
import com.yo.android.util.Constants;
import com.yo.android.util.DateUtil;
import com.yo.android.util.PopupDialogListener;
import com.yo.android.util.Util;
import com.yo.android.util.YODialogs;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by creatives on 10/7/2016.
 */
public class PopupHelper {

    public enum PopupsEnum {
        MAGAZINES,
        CHATS,
        DIALER,
        CONTACTS,
        MORE,
        YOCREDIT
    }

    public static void handlePop(PreferenceEndPoint preferenceEndPoint, Map data) {
        String id = data.get("id").toString();
        Type type = new TypeToken<List<Popup>>() {
        }.getType();
        List<Popup> popupList = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);

        if (popupList == null) {
            popupList = new ArrayList<Popup>();
        }

        if (("Magzines").equals(id)) {
            createPopup(data, popupList, PopupsEnum.MAGAZINES, preferenceEndPoint);
        } else if (("Chats").equals(id)) {
            createPopup(data, popupList, PopupsEnum.CHATS, preferenceEndPoint);
        } else if (("Dialer").equals(id)) {
            createPopup(data, popupList, PopupsEnum.DIALER, preferenceEndPoint);
        } else if (("Contacts").equals(id)) {
            createPopup(data, popupList, PopupsEnum.CONTACTS, preferenceEndPoint);
        } else if (("More").equals(id)) {
            createPopup(data, popupList, PopupsEnum.MORE, preferenceEndPoint);
        } else if (("YoCredit").equals(id)) {
            createPopup(data, popupList, PopupsEnum.YOCREDIT, preferenceEndPoint);
        }
    }

    private static void createPopup(Map data, List<Popup> popupList, PopupsEnum popupsEnum, PreferenceEndPoint preferenceEndPoint) {

        String id = data.get("id").toString();
        String title = data.get("title").toString();
        String message = data.get("message").toString();
        String imageUrl = data.get("image_url").toString();
        String tag = data.get("tag").toString();
        String redirectTo = data.get("redirect_to").toString();
        String liveFrom = data.get("live_from").toString();
        String liveTo = data.get("live_to").toString();

        Popup popup = new Popup();
        PopupData popupData = new PopupData();
        popupData.setTitle(title);
        popupData.setId(id);
        popupData.setMessage(message);
        popupData.setImage_url(imageUrl);
        popupData.setTag(tag);
        popupData.setRedirect_to(redirectTo);
        popupData.setLive_from(liveFrom);
        popupData.setLive_to(liveTo);
        long timestamp = System.currentTimeMillis();
        popupData.setTimestamp(timestamp);
        popup.setData(popupData);
        popup.setPopupsEnum(popupsEnum);
        popupList.add(popup);
        preferenceEndPoint.saveStringPreference(Constants.POPUP_NOTIFICATION, new Gson().toJson(popupList));
    }

    public static void getSinglePopup(PopupsEnum notificationType, Popup p, Activity activity, PreferenceEndPoint preferenceEndPoint, Fragment fragment, PopupDialogListener listener, List<Popup> popupList) {
        List<Popup> tempPopupList;
        tempPopupList = popupList;
        int i = -1;

        switch (p.getPopupsEnum()) {
            case MAGAZINES:
                if (!TextUtils.isEmpty(p.getData().getLive_to())) {
                    String liveToTime1 = p.getData().getLive_to().substring(0, p.getData().getLive_to().lastIndexOf("."));
                    Date liveToDate = DateUtil.convertUtcToGmt(liveToTime1);
                    long currentTime = System.currentTimeMillis();
                    Date currentDate = new Date(currentTime);
                    if (fragment instanceof MagazinesFragment && liveToDate.after(currentDate)) {
                        YODialogs.showPopup(preferenceEndPoint, activity, p, listener);
                    } else if (!liveToDate.after(currentDate)) {
                        preferenceEndPoint.saveStringPreference(Constants.POPUP_NOTIFICATION, new Gson().toJson(tempPopupList));
                    }
                } else {
                    if (fragment instanceof MagazinesFragment) {
                        YODialogs.showPopup(preferenceEndPoint, activity, p, listener);
                    }
                }
                break;
            case CHATS:
                if (!TextUtils.isEmpty(p.getData().getLive_to())) {
                    String liveToTime2 = p.getData().getLive_to().substring(0, p.getData().getLive_to().lastIndexOf("."));
                    Date liveToDate = DateUtil.convertUtcToGmt(liveToTime2);
                    long currentTime = System.currentTimeMillis();
                    Date currentDate = new Date(currentTime);
                    if (fragment instanceof ChatFragment && liveToDate.after(currentDate)) {
                        YODialogs.showPopup(preferenceEndPoint, activity, p, listener);
                    } else if (!liveToDate.after(currentDate)) {
                        preferenceEndPoint.saveStringPreference(Constants.POPUP_NOTIFICATION, new Gson().toJson(tempPopupList));
                    }
                } else {
                    if (fragment instanceof ChatFragment) {
                        YODialogs.showPopup(preferenceEndPoint, activity, p, listener);
                    }
                }
                break;
            case DIALER:
                if (!TextUtils.isEmpty(p.getData().getLive_to())) {
                    String liveToTime3 = p.getData().getLive_to().substring(0, p.getData().getLive_to().lastIndexOf("."));
                    Date liveToDate = DateUtil.convertUtcToGmt(liveToTime3);
                    long currentTime = System.currentTimeMillis();
                    Date currentDate = new Date(currentTime);
                    if (fragment instanceof DialerFragment && liveToDate.after(currentDate)) {
                        YODialogs.showPopup(preferenceEndPoint, activity, p, listener);
                    } else if (!liveToDate.after(currentDate)) {
                        preferenceEndPoint.saveStringPreference(Constants.POPUP_NOTIFICATION, new Gson().toJson(tempPopupList));
                    }
                } else {
                    if (fragment instanceof DialerFragment) {
                        YODialogs.showPopup(preferenceEndPoint, activity, p, listener);
                    }
                }
                break;
            case CONTACTS:
                if (!TextUtils.isEmpty(p.getData().getLive_to())) {
                    String liveToTime4 = p.getData().getLive_to().substring(0, p.getData().getLive_to().lastIndexOf("."));
                    Date liveToDate = DateUtil.convertUtcToGmt(liveToTime4);
                    long currentTime = System.currentTimeMillis();
                    Date currentDate = new Date(currentTime);
                    if (fragment instanceof ContactsFragment && liveToDate.after(currentDate)) {
                        YODialogs.showPopup(preferenceEndPoint, activity, p, listener);
                    } else if (!liveToDate.after(currentDate)) {
                        preferenceEndPoint.saveStringPreference(Constants.POPUP_NOTIFICATION, new Gson().toJson(tempPopupList));
                    }
                } else {
                    if (fragment instanceof ContactsFragment) {
                        YODialogs.showPopup(preferenceEndPoint, activity, p, listener);
                    }
                }
                break;
            case MORE:
                if (!TextUtils.isEmpty(p.getData().getLive_to())) {
                    String liveToTime5 = p.getData().getLive_to().substring(0, p.getData().getLive_to().lastIndexOf("."));
                    Date liveToDate = DateUtil.convertUtcToGmt(liveToTime5);
                    long currentTime = System.currentTimeMillis();
                    Date currentDate = new Date(currentTime);
                    if (fragment instanceof MoreFragment && liveToDate.after(currentDate)) {
                        YODialogs.showPopup(preferenceEndPoint, activity, p, listener);
                    } else if (!liveToDate.after(currentDate)) {
                        preferenceEndPoint.saveStringPreference(Constants.POPUP_NOTIFICATION, new Gson().toJson(tempPopupList));
                    }
                } else {
                    if (fragment instanceof MoreFragment) {
                        YODialogs.showPopup(preferenceEndPoint, activity, p, listener);
                    }
                }
                break;
            case YOCREDIT:
                if (!TextUtils.isEmpty(p.getData().getLive_to())) {
                    String liveToTime6 = p.getData().getLive_to().substring(0, p.getData().getLive_to().lastIndexOf("."));
                    Date liveToDate = DateUtil.convertUtcToGmt(liveToTime6);
                    long currentTime = System.currentTimeMillis();
                    Date currentDate = new Date(currentTime);
                    if (fragment == null && liveToDate.after(currentDate)) {
                        YODialogs.showPopup(preferenceEndPoint, activity, p, listener);
                    } else if (!liveToDate.after(currentDate)) {
                        preferenceEndPoint.saveStringPreference(Constants.POPUP_NOTIFICATION, new Gson().toJson(tempPopupList));
                    }
                } else {
                    if (fragment == null) {
                        YODialogs.showPopup(preferenceEndPoint, activity, p, listener);
                    }
                }
                break;
        }
    }
}

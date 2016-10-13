package com.yo.android.helpers;

import android.app.Activity;
import android.support.v4.app.Fragment;

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

    public static void getPopup(PopupsEnum notificationType, List<Popup> popupList, Activity activity, PreferenceEndPoint preferenceEndPoint, Fragment fragment) {
        if (popupList != null) {
            for (Popup popup :  popupList) {
                switch (popup.getPopupsEnum()) {
                    case MAGAZINES:
                        String modifiedTime = popup.getData().getLive_to();
                        Date date = Util.convertUtcToGmt(modifiedTime);
                        long currentTime = System.currentTimeMillis();
                        Date d1 = new Date(currentTime);
                        if(fragment instanceof MagazinesFragment && d1.before(date)) {
                            YODialogs.showPopup(preferenceEndPoint, activity, popup);
                        }
                        break;
                    case CHATS:
                        if(fragment instanceof ChatFragment) {
                            YODialogs.showPopup(preferenceEndPoint, activity, popup);
                        }
                        break;
                    case DIALER:
                        if(fragment instanceof DialerFragment) {
                            YODialogs.showPopup(preferenceEndPoint, activity, popup);
                        }
                        break;
                    case CONTACTS:
                        if(fragment instanceof ContactsFragment) {
                            YODialogs.showPopup(preferenceEndPoint, activity, popup);
                        }
                        break;
                    case MORE:
                        if(fragment instanceof MoreFragment) {
                            YODialogs.showPopup(preferenceEndPoint, activity, popup);
                        }
                        break;
                    case YOCREDIT:
                        if(fragment == null) {
                            YODialogs.showPopup(preferenceEndPoint, activity, popup);
                        }
                        break;
                }
            }
        }
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
}

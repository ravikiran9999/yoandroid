package com.yo.dialer;

import android.app.Activity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.helpers.PopupHelper;
import com.yo.android.model.Popup;
import com.yo.android.ui.BottomTabsActivity;
import com.yo.android.ui.NewDailerActivity;
import com.yo.android.ui.fragments.DialerFragment;
import com.yo.android.util.Constants;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Rajesh Babu on 11/7/17.
 */

public class DialerPopUp {
    public static void closePopup(final PreferenceEndPoint preferenceEndPoint, final boolean isSharedPreferenceShown) {
        Type type = new TypeToken<List<Popup>>() {
        }.getType();
        List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
        if (popup != null) {
            if (!isSharedPreferenceShown) {
                Collections.reverse(popup);
            }
            List<Popup> tempPopup = new ArrayList<>(popup);
            for (Popup p : popup) {
                if (p.getPopupsEnum() == PopupHelper.PopupsEnum.DIALER) {
                    tempPopup.remove(p);
                    break;
                }
            }
            popup = tempPopup;
        }
        preferenceEndPoint.saveStringPreference(Constants.POPUP_NOTIFICATION, new Gson().toJson(popup));
    }

    public static void showPopUp(final NewDialerFragment mainActivity, final boolean isVisibleToUser, final PreferenceEndPoint preferenceEndPoint, boolean isAlreadyShown, boolean isSharedPreferenceShown) {
        if (isVisibleToUser) {
            if (mainActivity.getActivity() instanceof BottomTabsActivity) {
                BottomTabsActivity activity = (BottomTabsActivity) mainActivity.getActivity();
                if (activity.getFragment() instanceof NewDialerFragment) {
                    if (preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION) != null) {
                        Type type = new TypeToken<List<Popup>>() {
                        }.getType();
                        List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
                        if (popup != null) {
                            Collections.reverse(popup);
                            isAlreadyShown = false;
                            for (Popup p : popup) {
                                if (p.getPopupsEnum() == PopupHelper.PopupsEnum.DIALER) {
                                    if (!isAlreadyShown) {
                                        PopupHelper.getSinglePopup(PopupHelper.PopupsEnum.DIALER, p, mainActivity.getActivity(), preferenceEndPoint, mainActivity, mainActivity, popup);
                                        isAlreadyShown = true;
                                        isSharedPreferenceShown = false;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}

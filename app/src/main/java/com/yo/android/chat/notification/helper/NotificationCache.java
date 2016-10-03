package com.yo.android.chat.notification.helper;


import com.yo.android.chat.notification.pojo.UserData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anitha on 15/12/15.
 */
public class NotificationCache {
    private static NotificationCache instance;
    private static List<UserData> notificationList = new ArrayList<UserData>();


    private NotificationCache() {
        //
    }

    public static synchronized NotificationCache get() {
        if (instance == null) {
            instance = new NotificationCache();
        }
        return instance;
    }

    /**
     * storing all notifications in a list
     * @param totalNotifications
     */
    public void setCacheNotifications(List<UserData> totalNotifications) {
        notificationList.addAll(totalNotifications);
    }

    /**
     * to get all notifications
     * @return notification list
     */
    public List<UserData> getCacheNotifications() {
        return notificationList;
    }

    /**
     * clear all notifications
     */
    public static void clearNotifications() {
        notificationList.clear();
    }
}
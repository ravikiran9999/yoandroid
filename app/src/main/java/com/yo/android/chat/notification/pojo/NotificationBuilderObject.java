package com.yo.android.chat.notification.pojo;

import java.util.List;

/**
 * Created by Anitha on 11/12/15.
 */
public class NotificationBuilderObject {
    private String notificationTitle;
    private String notificationText;
    private int notificationSmallIcon;
    private String notificationLargeiconUrl;
    private int notificationLargeIconDrawable;
    private String notificationTime;
    private String notificationInfo;
    private String notificationLargeText;
    private List<NotificationAction> actions;

    public String getNotificationTitle() {
        return notificationTitle;
    }

    public void setNotificationTitle(String notificationTitle) {
        this.notificationTitle = notificationTitle;
    }

    public String getNotificationText() {
        return notificationText;
    }

    public void setNotificationText(String notificationText) {
        this.notificationText = notificationText;
    }

    public int getNotificationSmallIcon() {
        return notificationSmallIcon;
    }

    public void setNotificationSmallIcon(int notificationSmallIcon) {
        this.notificationSmallIcon = notificationSmallIcon;
    }


    public int getNotificationLargeIconDrawable() {
        return notificationLargeIconDrawable;
    }

    public void setNotificationLargeIconDrawable(int notificationLargeIconDrawable) {
        this.notificationLargeIconDrawable = notificationLargeIconDrawable;
    }

    public String getNotificationTime() {
        return notificationTime;
    }

    public void setNotificationTime(String notificationTime) {
        this.notificationTime = notificationTime;
    }

    public String getNotificationInfo() {
        return notificationInfo;
    }

    public void setNotificationInfo(String notificationInfo) {
        this.notificationInfo = notificationInfo;
    }

    public List<NotificationAction> getActions() {
        return actions;
    }

    public void setActions(List<NotificationAction> actions) {
        this.actions = actions;
    }

    public String getNotificationLargeText() {
        return notificationLargeText;
    }

    public void setNotificationLargeText(String notificationLargeText) {
        this.notificationLargeText = notificationLargeText;
    }

    public String getNotificationLargeiconUrl() {
        return notificationLargeiconUrl;
    }

    public void setNotificationLargeiconUrl(String notificationLargeiconUrl) {
        this.notificationLargeiconUrl = notificationLargeiconUrl;
    }
}

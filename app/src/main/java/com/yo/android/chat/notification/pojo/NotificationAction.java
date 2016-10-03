package com.yo.android.chat.notification.pojo;

/**
 * Created by Anitha on 14/12/15.
 */
public class NotificationAction {
    private String actionTitle;
    private String actionType;
    private int actionIcon;

    public String getActionTitle() {
        return actionTitle;
    }

    public void setActionTitle(String actionTitle) {
        this.actionTitle = actionTitle;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public int getActionIcon() {
        return actionIcon;
    }

    public void setActionIcon(int actionIcon) {
        this.actionIcon = actionIcon;
    }
}

package com.yo.android.helpers;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractViewHolder;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by creatives on 7/26/2016.
 */
public class NotificationsViewHolder extends AbstractViewHolder {

    private ImageView imvNotificationsType;
    private TextView tvNotificationsDesc;
    private CircleImageView imvPeople;
    private TextView tvNotificationTime;

    public NotificationsViewHolder(View view) {
        super(view);
        imvNotificationsType = (ImageView) view.findViewById(R.id.imv_notification_type);
        tvNotificationsDesc = (TextView) view.findViewById(R.id.tv_notification_desc);
        imvPeople = (CircleImageView) view.findViewById(R.id.imv_people);
        tvNotificationTime = (TextView) view.findViewById(R.id.tv_notification_time);
    }

    public ImageView getImvNotificationsType() {
        return imvNotificationsType;
    }

    public void setImvNotificationsType(ImageView imvNotificationsType) {
        this.imvNotificationsType = imvNotificationsType;
    }

    public CircleImageView getImvPeople() {
        return imvPeople;
    }

    public void setImvPeople(CircleImageView imvPeople) {
        this.imvPeople = imvPeople;
    }

    public TextView getTvNotificationsDesc() {
        return tvNotificationsDesc;
    }

    public void setTvNotificationsDesc(TextView tvNotificationsDesc) {
        this.tvNotificationsDesc = tvNotificationsDesc;
    }

    public TextView getTvNotificationTime() {
        return tvNotificationTime;
    }

    public void setTvNotificationTime(TextView tvNotificationTime) {
        this.tvNotificationTime = tvNotificationTime;
    }
}

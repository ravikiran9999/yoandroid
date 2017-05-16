package com.yo.android.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.View;

import com.yo.android.R;
import com.yo.android.helpers.NotificationsViewHolder;
import com.yo.android.model.Notification;
import com.yo.android.util.DateUtil;
import com.yo.android.util.Util;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by creatives on 7/26/2016.
 */
public class NotificationsAdapter extends AbstractBaseAdapter<Notification, NotificationsViewHolder> {

    private Context context;

    public NotificationsAdapter(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public int getLayoutId() {
        return R.layout.notifications_list_item;
    }

    @Override
    public NotificationsViewHolder getViewHolder(View convertView) {
        return new NotificationsViewHolder(convertView);
    }

    @Override
    public void bindView(final int position, final NotificationsViewHolder holder, final Notification item) {

        holder.getTvNotificationsDesc().setText(item.getMessage());
        holder.getImvNotificationsType().setImageResource(R.drawable.ic_follow_notification);

        long time = 0;
        String modifiedTime = item.getUpdated_at().substring(0, item.getUpdated_at().lastIndexOf("."));
        Date date = DateUtil.convertUtcToGmt(modifiedTime);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        time = calendar.getTimeInMillis();

        String timeStamp = DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        holder.getTvNotificationTime().setText(timeStamp);

    }

}

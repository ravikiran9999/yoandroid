package com.yo.android.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.text.format.DateUtils;

import com.yo.android.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by rdoddapaneni on 5/15/2017.
 */

public class DateUtil {

    public final static String DATE_FORMAT_FULL = "yyyy-MM-dd'T'HH:mm:ss";
    public final static String DATE_FORMAT2 = "MMM dd, yyyy hh:mm a";
    public final static String DATE_FORMAT1 = "yyyy-MM-dd HH:mm:ss";
    public final static String DATE_FORMAT8 = "MMM dd, yyyy";




    public static Date convertUtcToGmt(String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_FULL);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date gmtTime = sdf.parse(time);
            return gmtTime;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String parseConvertUtcToGmt(String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT1);
            sdf.setTimeZone(TimeZone.getDefault());
            Date gmtTime = sdf.parse(time);
            String timeStamp = DateUtils.getRelativeTimeSpanString(gmtTime.getTime(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
            return timeStamp;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }

    public static String getDate(String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setTimeZone(TimeZone.getDefault());
            // Date gmtTime = sdf.parse(time);
            return sdf.format(sdf.parse(time));
            // String timeStamp = DateUtils.getRelativeTimeSpanString(gmtTime.getTime(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
            // return timeStamp;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }

    public static String getChatListTimeFormat(long time) {
        try {

            Calendar smsTime = Calendar.getInstance(TimeZone.getDefault());
            smsTime.setTimeInMillis(time);
            Calendar now = Calendar.getInstance(TimeZone.getDefault());
            if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
                return "Today";
            } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
                return "Yesterday";
            } else {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT8);
                simpleDateFormat.setTimeZone(TimeZone.getDefault());
                String date = simpleDateFormat.format(new Date(time));
                if (!date.equalsIgnoreCase("Jan 01, 1970")) {
                    return date;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String parseDate(String s) {
        try {
            Date date = new SimpleDateFormat(DATE_FORMAT1).parse(s);
            String timeStamp = DateUtils.getRelativeTimeSpanString(date.getTime(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
            return timeStamp;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return s;
    }

    public static long getTime(String str) {
        try {
            SimpleDateFormat sourceFormat = new SimpleDateFormat(DATE_FORMAT1);
            sourceFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = sourceFormat.parse(str);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getChatListTimeFormat(@NonNull final Context context, long time) {
        if (context == null) {
            return null;

        }
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(time);
        Calendar now = Calendar.getInstance();
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            return getTimeFormat(context, time);
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            return context.getString(R.string.yesterday);
        } else {
            SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy");
            return format.format(new Date(time));
//            Format format = android.text.format.DateFormat.getDateFormat(context);
//            return format.format(new Date(time));
        }
    }

    private static String getTimeFormat(@NonNull final Context context, long time) {
        SimpleDateFormat sFormat;
        String currentTime;
        try {
            if (DateFormat.is24HourFormat(context.getApplicationContext())) {
                sFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            } else {
                sFormat = new SimpleDateFormat("KK:mm aa", Locale.getDefault());
            }
            currentTime = sFormat.format(new Date(time));
            return currentTime;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new SimpleDateFormat("hh:mm a").format(new Date(time));
    }

    public static String getTimeFormatForChat(long time) {
        SimpleDateFormat sFormat;
        String currentTime;
        try {
            sFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            currentTime = sFormat.format(new Date(time));
            return currentTime;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new SimpleDateFormat("hh:mm").format(new Date(time));
    }
}
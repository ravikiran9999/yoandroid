package com.yo.android.util;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by kalyani on 19/1/16.
 */
public class TimeZoneUtils {
    private static Context mContext;
    private static TimeZoneUtils timezoneInstance;
    private static final int TEN = 10;

    private TimeZoneUtils() {
        //do nothing
    }

    /**
     * @param context
     * @return
     */
    public static TimeZoneUtils get(Context context) {
        if (context == null) {
            throw new NullPointerException("Context should not be null");
        } else {
            mContext = context;
            Log.i(TimeZoneUtils.class.getSimpleName(), "context: " + mContext);
        }
        if (timezoneInstance == null) {
            timezoneInstance = new TimeZoneUtils();
        }
        return timezoneInstance;
    }


    /**
     * @param format
     * @throws ArrayIndexOutOfBoundsException
     * @throws ParseException
     */
    public static long getTime(SimpleDateFormat format) {
        format.setTimeZone(TimeZone.getDefault());
        Date date = new Date();
        GregorianCalendar g = getGregorianCalendar();
        g.setTime(date);
        return g.getTimeInMillis();

    }

    /**
     * Sets the date result.
     *
     * @param view  the result date
     * @param year  the year
     * @param month the month
     * @param day   the day
     */
    public static void setDateResult(View view, int year, int month, int day) {
        if (view != null) {
            int m = month + 1;
            if (view instanceof TextView) {
                ((TextView) view).setText("" + (m < TEN ? String.format("%02d", m) : m) + "/" + (day < TEN ? String.format("%02d", day) : day) + "/" + year);
            } else if (view instanceof EditText) {
                ((EditText) view).setText("" + (m < TEN ? String.format("%02d", m) : m) + "/" + (day < TEN ? String.format("%02d", day) : day) + "/" + year);
            } else {
                throw new IllegalArgumentException("View should be either EditText or TextView");
            }
        }
    }

    /**
     * @return GregorianCalendar timezone object
     */
    private static GregorianCalendar getGregorianCalendar() {
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        int currentMonth = cal.get(Calendar.MONTH);
        int currentDay = cal.get(Calendar.DAY_OF_MONTH);
        return new GregorianCalendar(currentYear, currentMonth, currentDay);
    }
}

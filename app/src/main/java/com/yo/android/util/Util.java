package com.yo.android.util;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractBaseAdapter;
import com.yo.android.ui.BottomTabsActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Ramesh on 1/7/16.
 */
public class Util {
    public static final int DEFAULT_BUFFER_SIZE = 1024;

    public static <T> int createNotification(Context context, String title, String body, Class<T> clzz, Intent intent) {
        //
        Intent destinationIntent = new Intent(context, clzz);
        destinationIntent.putExtra("from_notification", true);
        destinationIntent.putExtras(intent);
        int notificationId = body.hashCode();
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), notificationId, destinationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.BigTextStyle notificationStyle = new NotificationCompat.BigTextStyle();
        notificationStyle.bigText(body);

        Notification notification = new NotificationCompat.Builder(context.getApplicationContext())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title == null ? "Yo App" : title)
                .setContentText(body)
                .setOngoing(true)
//                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setStyle(notificationStyle)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notification);
        return notificationId;
    }

    public static void cancelNotification(Context context, int notificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
    }

    public static String toString(InputStream inputstream) throws IOException {
        StringWriter stringwriter = new StringWriter();
        copy(inputstream, stringwriter);
        return stringwriter.toString();
    }

    /**
     * Copy.
     *
     * @param input  the input
     * @param output the output
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @SuppressWarnings("resource")
    private static void copy(final InputStream input, final Writer output) throws IOException {
        InputStreamReader in = null;
        try {
            in = new InputStreamReader(input);
            final char[] buffer = new char[DEFAULT_BUFFER_SIZE];
            long count = 0;
            int n = 0;
            while (-1 != (n = in.read(buffer))) {
                output.write(buffer, 0, n);
                count += n;
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (final IOException ex) {
                    //
                }
            }
        }
    }

    public static String removeTrailingZeros(String str) {
        if (str == null) {
            return str;
        }
        char[] chars = str.toCharArray();
        int length, index;
        length = str.length();
        index = length - 1;
        for (; index >= 0; index--) {
            if (chars[index] != '0') {
                break;
            }
        }
        return (index == length - 1) ? str : str.substring(0, index + 1);
    }

    public static String parseDate(String s) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(s);
            String timeStamp = DateUtils.getRelativeTimeSpanString(date.getTime(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
            return timeStamp;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return s;
    }

    public static long getTime(String str) {
        try {
            SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sourceFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = sourceFormat.parse(str);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getChatListTimeFormat(@NonNull final Context context, long time) {
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

    public static String getTimeFormat(@NonNull final Context context, long time) {
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

    public static void prepareSearch(final Activity activity, Menu menu, final AbstractBaseAdapter adapter) {
        final SearchManager searchManager =
                (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem;
        SearchView searchView;
        searchMenuItem = menu.findItem(R.id.menu_search);
        searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setQueryHint(Html.fromHtml("<font color = #88FFFFFF>" + "Search...." + "</font>"));
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(activity.getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            public static final String TAG = "PrepareSearch in Util";

            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.i(TAG, "onQueryTextChange: " + query);
                if (activity != null)
                    Util.hideKeyboard(activity, activity.getCurrentFocus());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.i(TAG, "onQueryTextChange: " + newText);
                if (adapter != null) {
                    adapter.performSearch(newText);
                }
                return true;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                if (activity != null)
                    Util.hideKeyboard(activity, activity.getCurrentFocus());
                if (adapter != null) {
                    adapter.performSearch("");
                }
                return true;
            }
        });
    }

    public static void prepareContactsSearch(final Activity activity, Menu menu, final AbstractBaseAdapter adapter, final String roomType) {
        final SearchManager searchManager =
                (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem;
        SearchView searchView;
        searchMenuItem = menu.findItem(R.id.menu_search);
        searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setQueryHint(Html.fromHtml("<font color = #88FFFFFF>" + "Search...." + "</font>"));
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(activity.getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            public static final String TAG = "PrepareSearch in Util";

            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.i(TAG, "onQueryTextChange: " + query);
                if (activity != null)
                    Util.hideKeyboard(activity, activity.getCurrentFocus());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.i(TAG, "onQueryTextChange: " + newText);
                if (adapter != null) {
                    if (roomType.equalsIgnoreCase(Constants.CHAT_FRAG)) {
                        adapter.performContactsSearch(newText);
                    } else {
                        adapter.performYoContactsSearch(newText);
                    }
                }
                return true;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                if (activity != null)
                    Util.hideKeyboard(activity, activity.getCurrentFocus());
                if (adapter != null) {
                    adapter.performSearch("");
                }
                return true;
            }
        });
    }

    public static void changeMenuItemsVisibility(Menu menu, int menuId, boolean visibility) {
        int size = menu.size();
        for (int i = 0; i < size; i++) {
            MenuItem item = menu.getItem(i);
            if (item.getItemId() != menuId) {
                item.setVisible(visibility);
            }
        }
    }

    public static void registerSearchLister(final Activity activity, final Menu menu) {
        MenuItem view = menu.findItem(R.id.menu_search);
        MenuItemCompat.setOnActionExpandListener(view, new MenuItemCompat.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if (activity instanceof BottomTabsActivity) {
                    ((BottomTabsActivity) activity).setToolBarColor(activity.getResources().getColor(R.color.colorPrimary));
                    ((BottomTabsActivity) activity).refresh();
                }
                Util.changeMenuItemsVisibility(menu, -1, true);
                return true;
            }
        });
    }

    public static String getChatListTimeFormat(long time) {
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(time);
        Calendar now = Calendar.getInstance();
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            return "Today";
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            return "Yesterday";
        } else {
            SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy");
            return format.format(new Date(time));
        }
    }

    public static void changeSearchProperties(Menu menu) {
        SearchView search = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        search.setQueryHint(Html.fromHtml("<font color = #88FFFFFF>" + "Search...." + "</font>"));
        AutoCompleteTextView searchTextView = (AutoCompleteTextView) search.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        try {
            searchTextView.setTextColor(Color.WHITE);
            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(searchTextView, R.drawable.red_cursor); //This sets the cursor resource ID to 0 or @null which will make it visible on white background
        } catch (Exception e) {
        }
    }

    public static void shareIntent(View view, String url, String title) {
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "Sharing Article");
            i.putExtra(Intent.EXTRA_TEXT, url);
            view.getContext().startActivity(Intent.createChooser(i, "Sharing Article"));
        } catch (ActivityNotFoundException e) {

        }
    }

    public static void hideKeyboard(Context context, View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


    public static void showSoftKeyboard(View view) {
        // requesting the view Focus
        if (view != null && view.requestFocus()) {
            // Gets the input method service ....
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public static Date convertUtcToGmt(String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date gmtTime = sdf.parse(time);
            String timeStamp = DateUtils.getRelativeTimeSpanString(gmtTime.getTime(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
            return timeStamp;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }

}

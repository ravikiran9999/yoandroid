package com.yo.android.util;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.orion.android.common.util.ToastFactory;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.adapters.AbstractBaseAdapter;
import com.yo.android.calllogs.CallerInfo;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.chat.notification.localnotificationsbuilder.Notifications;
import com.yo.android.chat.notification.pojo.NotificationBuilderObject;
import com.yo.android.chat.notification.pojo.UserData;
import com.yo.android.chat.ui.GroupContactsActivity;
import com.yo.android.model.Articles;
import com.yo.android.model.Contact;
import com.yo.android.model.UserProfileInfo;
import com.yo.android.model.dialer.OpponentDetails;
import com.yo.android.ui.BottomTabsActivity;
import com.yo.android.ui.FindPeopleActivity;
import com.yo.android.ui.TransferBalanceSelectContactActivity;
import com.yo.android.ui.fragments.DialerFragment;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Response;

/**
 * Created by Ramesh on 1/7/16.
 */
public class Util {


    public static final int DEFAULT_BUFFER_SIZE = 1024;
    private static final int SIX = 6;
    @Inject
    static ContactsSyncManager mContactsSyncManager;

    public static <T> int createNotification(Context context, String title, String body, Class<T> clzz, Intent intent) {
        return createNotification(context, title, body, clzz, intent, true);
    }

    public static <T> int createNotification(Context context, String title, String body, Class<T> clzz, Intent intent, boolean onGoing) {
        //

        if (title != null && title.contains(Constants.YO_USER)) {
            try {
                title = title.substring(title.indexOf(Constants.YO_USER) + 6, title.length() - 1);

            } catch (StringIndexOutOfBoundsException e) {
            }
        }

        Intent destinationIntent = new Intent(context, clzz);
        destinationIntent.putExtra("from_notification", true);
        destinationIntent.putExtra("type", body);
        destinationIntent.putExtras(intent);
        int notificationId = body.hashCode();
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), notificationId, destinationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.BigTextStyle notificationStyle = new NotificationCompat.BigTextStyle();
        notificationStyle.bigText(body);
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context.getApplicationContext())
                .setSmallIcon(getNotificationIcon())
                .setContentTitle(title == null ? "Yo App" : title)
                .setContentText(body)
                .setLargeIcon(largeIcon)
                .setContentIntent(pendingIntent)
                .setStyle(notificationStyle);
        if (onGoing) {
            builder.setOngoing(true);
        } else {
            builder.setAutoCancel(true);
        }
        Notification notification = builder.build();

        NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notification);
        return notificationId;
    }

    public static <T> void setBigStyleNotification(Context context, String title, String message, String tag, String id, boolean onGoing, boolean isDialer, Class<T> clzz, Intent intent) {
        Notifications notification = new Notifications();
        Intent notificationIntent = null;
        if (tag.equals("Outgoing call") || tag.equals("Incoming call")) {
            notificationIntent = intent;
        } else {
            notificationIntent = new Intent(context, BottomTabsActivity.class);
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            notificationIntent.putExtra(Constants.TYPE, Constants.YO_NOTIFICATION);
            notificationIntent.putExtra("title", title);
            notificationIntent.putExtra("message", message);
            notificationIntent.putExtra("tag", tag);
            notificationIntent.putExtra("id", id);
        }

        NotificationBuilderObject notificationsInboxData = prepareNotificationData(title, message);
        UserData data = new UserData();
        data.setDescription(message);
        //List<UserData> notificationList = NotificationCache.get().getCacheNotifications();
        List<UserData> notificationList = new ArrayList<>();
        notificationList.add(data);
        notification.buildInboxStyleNotifications(context, notificationIntent, notificationsInboxData, notificationList, SIX, onGoing, isDialer);
    }

    @NonNull
    private static NotificationBuilderObject prepareNotificationData(String title, String message) {
        NotificationBuilderObject notificationData = new NotificationBuilderObject();
        notificationData.setNotificationTitle(title);
        notificationData.setNotificationSmallIcon(getNotificationIcon());
        notificationData.setNotificationText(message);
        notificationData.setNotificationLargeIconDrawable(R.mipmap.ic_launcher);
        notificationData.setNotificationInfo("3");
        //notificationData.setNotificationLargeiconUrl(chatMessage.getImagePath());
        //notificationData.setNotificationLargeText("Hello Every one ....Welcome to Notifications Demo..we are very glade to meet you here.Android Developers ");
        return notificationData;
    }

    private static int getNotificationIcon() {
        boolean useWhiteIcon = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
        return useWhiteIcon ? R.drawable.ic_yo_notification_white : R.drawable.ic_yo_notification;
    }

    public static void cancelNotification(Context context, int notificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
    }

    public static void isNotificationAvailable(Context context, int notificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

    }

    public static void cancelReadNotification(Context context, int roomId) {
        NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(roomId);

    }

    public static void cancelAllNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
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

    public static String getTimeFormatForChat(@NonNull final Context context, long time) {
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

    public static void prepareTransferBalanceContactsSearch(final Activity activity, Menu menu, final AbstractBaseAdapter adapter) {
        final SearchManager searchManager =
                (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem;
        SearchView searchView;
        searchMenuItem = menu.findItem(R.id.menu_search);
        searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setQueryHint(Html.fromHtml("<font color = #88FFFFFF>" + "Enter atleast 3 characters...." + "</font>"));
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
                    adapter.performTransferBalanceContactsSearch(newText);
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
                    adapter.performTransferBalanceContactsSearch("");
                }
                return true;
            }
        });
    }

    public static void prepareContactsSearch(final Activity activity, Menu menu, final AbstractBaseAdapter adapter, final String roomType) {

        final SearchManager searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView;
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
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
                    } else if (roomType.equalsIgnoreCase(Constants.DAILER_FRAG)) {
                        adapter.performCallLogsSearch(newText);
                    } else if (roomType.equalsIgnoreCase(Constants.Yo_CONT_FRAG) || roomType.equalsIgnoreCase(Constants.CONT_FRAG)) {
                        String contactType = roomType.equalsIgnoreCase(Constants.Yo_CONT_FRAG) ? Constants.Yo_CONT_FRAG : Constants.CONT_FRAG;
                        adapter.performYoContactsSearch(newText, contactType);
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
                    // ((BottomTabsActivity) activity).setToolBarColor(activity.getResources().getColor(R.color.colorPrimary));
                    ((BottomTabsActivity) activity).refresh();

                } else if (activity instanceof FindPeopleActivity) {
                    ((FindPeopleActivity) activity).refresh();
                } /*else if (activity instanceof TransferBalanceSelectContactActivity) {
                    ((TransferBalanceSelectContactActivity) activity).refresh();
                }*/
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
            //This sets the cursor resource ID to 0 or @null which will make it visible on white background
            mCursorDrawableRes.set(searchTextView, R.drawable.red_cursor);
        } catch (Exception e) {
        }
    }

    public static void closeSearchView(Menu menu) {
        try {
            if (menu.findItem(R.id.menu_search) != null && menu.findItem(R.id.menu_search).isActionViewExpanded()) {
                (menu.findItem(R.id.menu_search)).collapseActionView();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void shareIntent(View view, String url, String title) {
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, title);
            i.putExtra(Intent.EXTRA_TEXT, url);
            view.getContext().startActivity(Intent.createChooser(i, title));
        } catch (ActivityNotFoundException e) {

        }
    }

    public static void shareNewIntent(View view, String url, String title, String body, Uri bmpUri) {

        try {
            // get available share intents
            List<Intent> targets = new ArrayList<Intent>();
            Intent template = new Intent(Intent.ACTION_SEND);
            template.setType("image/*");
            List<ResolveInfo> candidates = view.getContext().getPackageManager().
                    queryIntentActivities(template, 0);

            for (ResolveInfo candidate : candidates) {
                String packageName = candidate.activityInfo.packageName;
                if (packageName.equals("com.skype.raider")) {
                    Intent target = new Intent(android.content.Intent.ACTION_SEND);
                    target.setType("text/plain");
                    target.putExtra(Intent.EXTRA_SUBJECT, title);
                    target.putExtra(Intent.EXTRA_TEXT, body + "\n\n" + url);
                    target.setPackage(packageName);
                    targets.add(target);
                } else if (packageName.toLowerCase().startsWith("com.facebook.katana")) {
                    Intent target = new Intent(android.content.Intent.ACTION_SEND);
                    target.setType("text/plain");
                    target.putExtra(Intent.EXTRA_SUBJECT, title);
                    target.putExtra(Intent.EXTRA_TEXT, body + "\n\n" + url);
                    target.setPackage(candidate.activityInfo.packageName);
                    targets.add(target);

                } else {
                    Intent target = new Intent(android.content.Intent.ACTION_SEND);
                    target.setType("image/*");
                    target.putExtra(Intent.EXTRA_SUBJECT, title);
                    target.putExtra(Intent.EXTRA_TEXT, body + "\n\n" + url);
                    if (bmpUri != null) {
                        target.putExtra(Intent.EXTRA_STREAM, bmpUri);
                    }
                    target.setPackage(packageName);
                    targets.add(target);
                }
            }

            Intent chooser = Intent.createChooser(targets.remove(0), "Sharing Article");
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, targets.toArray(new Parcelable[]{}));
            view.getContext().startActivity(chooser);
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

    public static void saveUserDetails(Response<UserProfileInfo> response, PreferenceEndPoint preferenceEndPoint) {

        preferenceEndPoint.saveStringPreference(Constants.USER_NAME, response.body().getFirstName());
        preferenceEndPoint.saveStringPreference(Constants.USER_STATUS, response.body().getDescription());
        preferenceEndPoint.saveStringPreference(Constants.USER_AVATAR, response.body().getAvatar());
        preferenceEndPoint.saveBooleanPreference(Constants.SYNCE_CONTACTS, response.body().isSyncContacts());
        preferenceEndPoint.saveBooleanPreference(Constants.NOTIFICATION_ALERTS, response.body().isNotificationAlert());

    }

    public static void inviteFriend(Context context, String phoneNo) {
        try {
            String url = context.getString(R.string.invite_link);
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
            smsIntent.addCategory(Intent.CATEGORY_DEFAULT);
            smsIntent.setType("vnd.android-dir/mms-sms");
            smsIntent.putExtra("sms_body", url);
            smsIntent.setData(Uri.parse("sms:" + phoneNo));
            smsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(smsIntent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static class ImageLoaderTask extends AsyncTask<String, Void, Bitmap> {

        private View v;
        private Articles data;

        public ImageLoaderTask(View v, Articles data) {
            this.v = v;
            this.data = data;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            URL url = null;
            try {
                url = new URL(params[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            HttpURLConnection connection = null;
            try {
                if (url != null) {
                    connection = (HttpURLConnection) url.openConnection();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (connection != null) {
                connection.setDoInput(true);
            }

            try {
                if (connection != null) {
                    connection.connect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            InputStream input = null;
            try {
                if (connection != null) {
                    input = connection.getInputStream();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


            Bitmap immutableBpm = BitmapFactory.decodeStream(input);

            if (immutableBpm != null) {
                Bitmap mutableBitmap = immutableBpm.copy(Bitmap.Config.ARGB_8888, true);
                return mutableBitmap;
            }

            return null;

        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            if (bitmap != null) {
                View view = new View(v.getContext());

                view.draw(new Canvas(bitmap));

                String path = MediaStore.Images.Media.insertImage(v.getContext().getContentResolver(), bitmap, "Yo", null);

                if (path != null) {
                    Uri uri = Uri.parse(path);
                    shareNewIntent(v, data.getGenerated_url(), "Article: " + data.getTitle(), data.getSummary(), uri);
                }
            } else {
                shareNewIntent(v, data.getGenerated_url(), "Article: " + data.getTitle(), data.getSummary(), null);
            }

        }
    }

    public static void copyFile(String inputPath, String outputPath) {

        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(inputPath);
            out = new FileOutputStream(outputPath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            // write the output file (You have now copied the file)
            out.flush();

        } catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isKb(long length) {
        double size = length / 1024.0;
        return size <= 1;
    }

    public static void setDynamicHeight(ListView mListView) {
        ListAdapter mListAdapter = mListView.getAdapter();
        if (mListAdapter == null) {
            // when adapter is null
            return;
        }
        int height = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(mListView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        for (int i = 0; i < mListAdapter.getCount(); i++) {
            View listItem = mListAdapter.getView(i, null, mListView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            height += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = mListView.getLayoutParams();
        params.height = height + (mListView.getDividerHeight() * (mListAdapter.getCount() - 1));
        mListView.setLayoutParams(params);
        mListView.requestLayout();
    }

    public static void setBigStyleNotificationForBalance(Context context, String title, String message, String tag, String id) {
        Notifications notification = new Notifications();

        Intent notificationIntent = new Intent(context, BottomTabsActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.putExtra(Constants.TYPE, Constants.YO_NOTIFICATION);
        notificationIntent.putExtra("title", title);
        notificationIntent.putExtra("message", message);
        notificationIntent.putExtra("tag", tag);
        notificationIntent.putExtra("id", id);

        NotificationBuilderObject notificationsInboxData = prepareNotificationData(title, message);
        UserData data = new UserData();
        data.setDescription(message);
        //List<UserData> notificationList = NotificationCache.get().getCacheNotifications();
        List<UserData> notificationList = new ArrayList<>();
        notificationList.add(data);
        notification.buildInboxStyleNotifications(context, notificationIntent, notificationsInboxData, notificationList, SIX, false, true);
    }

    public static void showErrorMessages(final OpponentDetails details, final Context context, final ToastFactory mToastFactory) {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int statusCode = details.getStatusCode();
                    switch (statusCode) {
                        case 603:
                            mToastFactory.showToast(R.string.busy);
                            break;
                        case 404:
                            mToastFactory.showToast(R.string.no_network);
                            break;
                        case 503:
                            if (mContactsSyncManager != null && details != null) {
                                Contact mContact = details.getContact();
                                if (mContact != null) {
                                    Log.w(Util.class.getSimpleName(), mContact.toString());
                                } else {
                                    Log.w(Util.class.getSimpleName(), "Contact object is null");
                                }
                            } else {
                                Log.w(Util.class.getSimpleName(), "mContactsSyncManager or details object is null");
                            }
                            mToastFactory.showToast(R.string.not_online);
                            break;
                        case 487:
                            //Missed call
                            break;
                        case 181:
                            mToastFactory.showToast(R.string.call_forwarded);
                            break;
                        case 182:
                        case 480:
                            mToastFactory.showToast(R.string.temporerly_unavailable);
                            break;
                        case 180:
                            mToastFactory.showToast(R.string.ringing);
                            break;
                        case 486:
                            mToastFactory.showToast(R.string.busy);
                            break;
                        case 600:
                            mToastFactory.showToast(R.string.all_busy);
                            break;
                        case 403:
                            mToastFactory.showToast(R.string.unknown_error);
                            /*if (details!=null&& details.getVoxUserName()!=null && details.getVoxUserName().contains(BuildConfig.RELEASE_USER_TYPE)) {
                                YODialogs.redirectToPSTN((Activity) context,details, new DialerFragment.CallLogClearListener() {
                                    @Override
                                    public void clear() {

                                    }
                                });
                            }*/
                            break;
                    }
                }
            });
        }
    }

    public static void sendMediaButton(Context context, int keyCode) {
        KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
        context.sendOrderedBroadcast(intent, null);

        keyEvent = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
        intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
        context.sendOrderedBroadcast(intent, null);
    }
}

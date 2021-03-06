package com.yo.android.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.adapters.AbstractBaseAdapter;
import com.yo.android.chat.notification.localnotificationsbuilder.Notifications;
import com.yo.android.chat.notification.pojo.NotificationBuilderObject;
import com.yo.android.chat.notification.pojo.UserData;
import com.yo.android.model.Articles;
import com.yo.android.model.UserProfileInfo;
import com.yo.android.photo.TextDrawable;
import com.yo.android.photo.util.ColorGenerator;
import com.yo.android.ui.BaseActivity;
import com.yo.android.ui.BottomTabsActivity;
import com.yo.android.ui.CreateMagazineActivity;
import com.yo.android.ui.FindPeopleActivity;
import com.yo.android.ui.FollowersActivity;
import com.yo.android.ui.FollowingsActivity;
import com.yo.android.ui.MyCollections;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Response;

/**
 * Created by Ramesh on 1/7/16.
 */
public class Util {


    public static final int DEFAULT_BUFFER_SIZE = 1024;
    private static final int SIX = 6;
    public static final String ServerTimeStamp = "serverTimeStamp";
    public static final String ServerTimeStampReceived = "serverTimeStampReceived";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

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

    public static void prepareSearch(final Activity activity, Menu menu, final AbstractBaseAdapter adapter, final TextView noData, final ListView listView, final GridView gridView, final TextView networkFailureText) {
        final SearchManager searchManager =
                (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView;
        searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setQueryHint(Html.fromHtml("<font color = #88FFFFFF>" + "Search...." + "</font>"));
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(activity.getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            public List list;
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
                    list = adapter.performSearch(newText);
                    if (list != null && noData != null && activity != null) {
                        boolean isListEmpty = list.isEmpty();
                        if (isListEmpty) {
                            if ((activity instanceof MyCollections && TextUtils.isEmpty(newText) && ((MyCollections) activity).isNetworkFailure) || (activity instanceof MyCollections && TextUtils.isEmpty(newText) && ((MyCollections) activity).isNetworkFailure)) {
                                noData.setVisibility(View.GONE);
                                networkFailureText.setVisibility(View.VISIBLE);
                                if (listView != null) {
                                    listView.setVisibility(View.GONE);
                                }
                                if (gridView != null) {
                                    gridView.setVisibility(View.GONE);
                                }
                            } else if ((activity instanceof CreateMagazineActivity && TextUtils.isEmpty(newText) && ((CreateMagazineActivity) activity).isNetworkFailure) || (activity instanceof CreateMagazineActivity && TextUtils.isEmpty(newText) && ((CreateMagazineActivity) activity).isNetworkFailure)) {
                                noData.setVisibility(View.GONE);
                                networkFailureText.setVisibility(View.VISIBLE);
                                if (listView != null) {
                                    listView.setVisibility(View.GONE);
                                }
                                if (gridView != null) {
                                    gridView.setVisibility(View.GONE);
                                }
                            } else {
                                noData.setVisibility(View.VISIBLE);
                                if (listView != null) {
                                    listView.setVisibility(View.GONE);
                                }
                                if (gridView != null) {
                                    gridView.setVisibility(View.GONE);
                                }
                                noData.setText(activity.getResources().getString(R.string.no_result_found));
                            }
                        } else {
                            if (listView != null) {
                                listView.setVisibility(View.VISIBLE);
                            }
                            if (gridView != null) {
                                gridView.setVisibility(View.VISIBLE);
                            }
                            noData.setVisibility(View.GONE);
                        }

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

    public static void preparePeopleSearch(final Activity activity, Menu menu, final AbstractBaseAdapter adapter, final TextView noData, final ListView listView, final GridView gridView, final LinearLayout llNoPeople, final TextView networkFailureText) {
        final SearchManager searchManager =
                (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView;
        searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setQueryHint(Html.fromHtml("<font color = #88FFFFFF>" + "Search...." + "</font>"));
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(activity.getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            public List list;
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
                    list = adapter.performSearch(newText);
                    if (list != null && noData != null && activity != null) {
                        boolean isListEmpty = list.isEmpty();
                        if (isListEmpty) {
                            if (TextUtils.isEmpty(newText) && llNoPeople.getVisibility() == View.VISIBLE) {
                                llNoPeople.setVisibility(View.VISIBLE);
                                noData.setVisibility(View.GONE);
                                networkFailureText.setVisibility(View.GONE);
                                if (listView != null) {
                                    listView.setVisibility(View.GONE);
                                }
                                if (gridView != null) {
                                    gridView.setVisibility(View.GONE);
                                }
                            } else if ((activity instanceof FollowersActivity && TextUtils.isEmpty(newText) && ((FollowersActivity) activity).isEmptyDataSet) || (activity instanceof FollowingsActivity && TextUtils.isEmpty(newText) && ((FollowingsActivity) activity).isEmptyDataSet)) {
                                llNoPeople.setVisibility(View.VISIBLE);
                                noData.setVisibility(View.GONE);
                                networkFailureText.setVisibility(View.GONE);
                                if (listView != null) {
                                    listView.setVisibility(View.GONE);
                                }
                                if (gridView != null) {
                                    gridView.setVisibility(View.GONE);
                                }
                            } else if ((activity instanceof FollowersActivity && TextUtils.isEmpty(newText) && ((FollowersActivity) activity).isNetworkFailure) || (activity instanceof FollowingsActivity && TextUtils.isEmpty(newText) && ((FollowingsActivity) activity).isNetworkFailure)) {
                                llNoPeople.setVisibility(View.GONE);
                                noData.setVisibility(View.GONE);
                                networkFailureText.setVisibility(View.VISIBLE);
                                if (listView != null) {
                                    listView.setVisibility(View.GONE);
                                }
                                if (gridView != null) {
                                    gridView.setVisibility(View.GONE);
                                }
                            } else {
                                noData.setVisibility(View.VISIBLE);
                                if (listView != null) {
                                    listView.setVisibility(View.GONE);
                                }
                                if (gridView != null) {
                                    gridView.setVisibility(View.GONE);
                                }
                                noData.setText(activity.getResources().getString(R.string.no_result_found));
                                llNoPeople.setVisibility(View.GONE);
                                networkFailureText.setVisibility(View.GONE);
                            }
                        } else {
                            if (listView != null) {
                                listView.setVisibility(View.VISIBLE);
                            }
                            if (gridView != null) {
                                gridView.setVisibility(View.VISIBLE);
                            }
                            noData.setVisibility(View.GONE);
                            llNoPeople.setVisibility(View.GONE);
                            networkFailureText.setVisibility(View.GONE);
                        }

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

    public static void prepareTransferBalanceContactsSearch(final Activity activity, final Menu menu, final AbstractBaseAdapter adapter, final TextView noData, final ListView listView, final LinearLayout llNoPeople) {
        final SearchManager searchManager =
                (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView;
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setQueryHint(Html.fromHtml("<font color = #88FFFFFF>" + "Enter atleast 4 characters...." + "</font>"));
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
                    Log.i(TAG, "The list count is " + adapter.getCount());
                    if (noData != null && activity != null && llNoPeople != null) {
                        if (adapter.getCount() == 0 && menu.findItem(R.id.menu_search).isActionViewExpanded()) {
                            noData.setVisibility(View.VISIBLE);
                            llNoPeople.setVisibility(View.VISIBLE);
                            if (listView != null) {
                                listView.setVisibility(View.GONE);
                            }
                            noData.setText(activity.getResources().getString(R.string.no_result_found));
                        } else {
                            if (listView != null) {
                                listView.setVisibility(View.VISIBLE);
                            }
                            llNoPeople.setVisibility(View.GONE);
                            noData.setVisibility(View.GONE);
                        }

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
                    adapter.performTransferBalanceContactsSearch("");
                }
                return true;
            }
        });
    }

    /**
     * Search in Chats, Contacts, Dialer
     *
     * @param activity
     * @param menu
     * @param adapter
     * @param roomType
     * @param noSearchResult
     * @param noContactsFound
     * @return
     */
    public static void prepareContactsSearch(final Activity activity, final Menu menu, final AbstractBaseAdapter adapter, final String roomType, final TextView noSearchResult, final TextView noContactsFound) {

        final SearchManager searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView;
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setQueryHint(Html.fromHtml("<font color = #88FFFFFF>" + "Search...." + "</font>"));
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(activity.getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            private static final String TAG = "PrepareSearch in Util";

            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.i(TAG, "onQueryTextChange: " + query);
                if (activity != null)
                    Util.hideKeyboard(activity, activity.getCurrentFocus());
                return true;
            }


            @Override
            public boolean onQueryTextChange(String newText) {
                boolean isFromClose = false;
                if (TextUtils.isEmpty(newText)) {
                    if (menu.findItem(R.id.menu_search).isActionViewExpanded()) {
                        noSearchResult.setVisibility(View.GONE);
                        isFromClose = true;
                    } else {
                        if (noContactsFound != null) {
                            noContactsFound.setVisibility(View.GONE);
                        }
                        noSearchResult.setVisibility(View.VISIBLE);
                        noSearchResult.setText(activity.getResources().getString(R.string.no_result_found));
                    }
                } else {
                    noSearchResult.setVisibility(View.GONE);
                }
                if (adapter != null) {
                    if (roomType.equalsIgnoreCase(Constants.CHAT_FRAG)) {
                        adapter.performContactsSearch(newText, noSearchResult, isFromClose);
                    } else if (roomType.equalsIgnoreCase(Constants.DAILER_FRAG)) {
                        adapter.performCallLogsSearch(newText, noSearchResult, isFromClose);
                    } else if (roomType.equalsIgnoreCase(Constants.Yo_CONT_FRAG) || roomType.equalsIgnoreCase(Constants.CONT_FRAG)) {
                        String contactType = roomType.equalsIgnoreCase(Constants.Yo_CONT_FRAG) ? Constants.Yo_CONT_FRAG : Constants.CONT_FRAG;
                        adapter.performYoContactsSearch(newText, contactType, noSearchResult, isFromClose, noContactsFound);
                    } else if (roomType.equalsIgnoreCase(Constants.INVITE_FRAG)) {
                        adapter.performContactsSearch(newText, noSearchResult, isFromClose);
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
                    List list = adapter.performSearch("");
                    if (list.size() > 0) {
                        noSearchResult.setVisibility(View.GONE);
                    }
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
            if (menu != null && menu.findItem(R.id.menu_search) != null && menu.findItem(R.id.menu_search).isActionViewExpanded()) {
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
            List<Intent> targets = new ArrayList<>();
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

    public static boolean hideKeyboard(Context context, View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            return imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
        return false;
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
                    String summary = Html.fromHtml(data.getSummary()).toString();
                    shareNewIntent(v, data.getGenerated_url(), "Article: " + data.getTitle(), summary, uri);
                }
            } else {
                String summary = Html.fromHtml(data.getSummary()).toString();
                shareNewIntent(v, data.getGenerated_url(), "Article: " + data.getTitle(), summary, null);
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
        notificationIntent.putExtra("fromLowBalNotification", true);

        NotificationBuilderObject notificationsInboxData = prepareNotificationData(title, message);
        UserData data = new UserData();
        data.setDescription(message);
        //List<UserData> notificationList = NotificationCache.get().getCacheNotifications();
        List<UserData> notificationList = new ArrayList<>();
        notificationList.add(data);
        notification.buildInboxStyleNotifications(context, notificationIntent, notificationsInboxData, notificationList, SIX, false, true);
    }

    public static void showLowBalanceNotification(Context context, PreferenceEndPoint preferenceEndPoint) {
        long currentTime = System.currentTimeMillis();
        if (preferenceEndPoint.getLongPreference(Constants.LOW_BALANCE_NOTIFICATION_TIME, 0) == 0) {
            Util.setBigStyleNotificationForBalance(context, "Credit", context.getString(R.string.low_balance), "Credit", "");
            preferenceEndPoint.saveLongPreference(Constants.LOW_BALANCE_NOTIFICATION_TIME, currentTime);
        } else {
            long lastShownTime = preferenceEndPoint.getLongPreference(Constants.LOW_BALANCE_NOTIFICATION_TIME, 0);
            if (currentTime - lastShownTime >= Constants.LOW_BALANCE_NOTIFICATION_FREQUENCY) {
                Util.setBigStyleNotificationForBalance(context, "Credit", context.getString(R.string.low_balance), "Credit", "");
                preferenceEndPoint.saveLongPreference(Constants.LOW_BALANCE_NOTIFICATION_TIME, currentTime);
            }
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

    public static Drawable showFirstLetter(Context mContext, String name) {
        String title = String.valueOf(name.charAt(0)).toUpperCase();
        Pattern p = Pattern.compile("^[a-zA-Z]");
        Matcher m = p.matcher(title);
        boolean b = m.matches();
        if (b) {
            Drawable drawable = TextDrawable.builder().round().build(title, ColorGenerator.MATERIAL.getColor(name));
            return drawable;
        }
        return mContext.getResources().getDrawable(R.drawable.dynamic_profile);
    }

    public static String numericValueFromString(Context context, String string) {
        try {
            if (string.contains(Constants.YO_USER)) {
                return String.format(context.getResources().getString(R.string.plus_number), string.replaceAll("[^0-9]", ""));
            } else if (TextUtils.isDigitsOnly(string)) {
                return String.format(context.getResources().getString(R.string.plus_number), string);
            } else {
                return string;
            }
        } catch (NumberFormatException e) {
            return string;
        }
    }

    public static String numberFromNexgeFormat(String string) {
        try {
            if (string != null) {
                String number = string.replaceAll("[^0-9]", "");
                if (!number.startsWith("+")) {
                    return "+" + number;
                } else {
                    return number;
                }
            } else {
                return "+1234567890";
            }
        } catch (NumberFormatException e) {
            return string;
        }
    }

    public static String convertSecToHMmSs(long totalSecs) {

        long hours = totalSecs / 3600;
        long minutes = (totalSecs % 3600) / 60;
        long seconds = totalSecs % 60;
        /*if (minutes == 0) {
            return String.format("%02d secs", seconds);
        }
        if (hours == 0) {
            return String.format("%02d mins %02d secs", minutes, seconds);
        }
        return String.format("%02d h %02d mins %02d secs", hours, minutes, seconds);*/

        if (hours == 0 && minutes == 0) {
            return String.format("%02d secs", seconds);
        } else if (hours == 0 && seconds == 0) {
            return String.format("%02d mins", minutes, seconds);
        } else if (hours == 0) {
            return String.format("%02d mins %02d secs", minutes, seconds);
        } else {
            return String.format("%02d h %02d mins %02d secs", hours, minutes, seconds);
        }
    }

    public static String addDenomination(String amount, String amountWithDenomination) {
        try {
            amount = amountLookUp(amount);
            if (amountWithDenomination != null) {
                String denomination = amountWithDenomination.replaceAll("[0-9]", "");
                return String.format("%s %s", currencySymbolUpLook(denomination), amount);
            } else {
                return String.format("%s %s", currencySymbolUpLook("$"), amount);
            }
        } catch (Exception e) {
            return String.format("%s %s", currencySymbolUpLook("$"), amount);
        }
    }

    private static String currencySymbolUpLook(String currencyCode) {
        if (currencyCode.contains("INR") || currencyCode.contains("₨")) {
            return "₹";
        } else {
            return "$";
        }
    }

    private static String amountLookUp(String amount) {
        if (amount.startsWith(".")) {
            return "0" + amount;
        }

        return amount;
    }

    public static void appendLog(String text) {
        File logFile = new File("sdcard/calldump.file");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkPlayServices(Context context) {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(context);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog((Activity) context, result,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }

            return false;
        }

        return true;
    }

    public static void initBar(SeekBar bar, final AudioManager audioManager, final int stream) {
        int currentVolume = audioManager.getStreamVolume(stream);
        Log.i("Volume", "currentVolume : " + currentVolume);
        int maxVolume = audioManager.getStreamMaxVolume(stream);
        Log.i("Volume", "maxVolume : " + maxVolume);

        bar.setMax(audioManager.getStreamMaxVolume(stream));
        bar.setProgress(audioManager.getStreamVolume(stream));
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(stream, progress, AudioManager.FLAG_PLAY_SOUND);
            }

            public void onStartTrackingTouch(SeekBar bar) {
            }

            public void onStopTrackingTouch(SeekBar bar) {
            }
        });
    }

    public static void initVolumeToSixty(final AudioManager audioManager) {
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        float percent = 0.6f;
        final int sixtyVolume = (int) (maxVolume * percent);
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, sixtyVolume, AudioManager.FLAG_PLAY_SOUND);
        //bar.setProgress(currentVolume);
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }

    public static void hideSideIndex(MenuItem menuItem, final ListView listView) {
        // Hide right side alphabets when search is opened.
        MenuItemCompat.setOnActionExpandListener(menuItem, new MenuItemCompat.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                listView.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                listView.setVisibility(View.VISIBLE);
                return true;
            }
        });
    }

    public static boolean isAppRunning(final Context context) {
        String packageName = BuildConfig.APPLICATION_ID;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> processInfos;
        if (am != null) {
            processInfos = am.getRunningAppProcesses();
            if (processInfos != null) {
                for (final ActivityManager.RunningAppProcessInfo processInfo : processInfos) {
                    if (processInfo.processName.equals(packageName)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean appRunningStatus(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        String[] strings = cn.getShortClassName().split(Pattern.quote("."));
        int i = strings.length - 1;
        if (strings[i].equalsIgnoreCase("BaseActivity")) {
            return true;
        }

        return false;
    }

}
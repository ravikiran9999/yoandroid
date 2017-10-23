package com.yo.android.ui.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.orion.android.common.util.ConnectivityHelper;
import com.yo.android.R;
import com.yo.android.adapters.ContactsListAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.helpers.Helper;
import com.yo.android.helpers.PopupHelper;
import com.yo.android.model.Contact;
import com.yo.android.model.Popup;
import com.yo.android.provider.YoAppContactContract;
import com.yo.android.ui.BottomTabsActivity;
import com.yo.android.ui.UserProfileActivity;
import com.yo.android.util.Constants;
import com.yo.android.util.PopupDialogListener;
import com.yo.android.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class NewContactsFragment extends BaseFragment implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener, PopupDialogListener {
    private static final String TAG = NewContactsFragment.class.getSimpleName();
    @Inject
    ConnectivityHelper mHelper;

    /**
     * Project used when querying content provider. Returns all known fields.
     */
    private static final String[] PROJECTION = new String[]{
            YoAppContactContract.YoAppContactsEntry._ID,
            YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_USER_ID,
            YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_NAME,
            YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_PHONE_NUMBER,
            YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_IMAGE,
            YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_FIREBASE_ROOM_ID,
            YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_IS_YOAPP_USER,
            YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_VOX_USER_NAME,
            YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_COUNTRY_CODE,

    };
    // Constants representing column positions from PROJECTION.
    public static final int COLUMN_ID = 0;
    public static final int COLUMN_ENTRY_ID = 1;
    public static final int COLUMN_NAME = 2;
    public static final int COLUMN_PHONE = 3;
    public static final int COLUMN_IMAGE = 4;
    public static final int COLUMN_FIREBASE_ROOM_ID = 5;
    public static final int COLUMN_YO_USER = 6;

    private ContactsListAdapter contactsListAdapter;
    ContentObserver contentObserver;
    private ListView listView;

    private Menu menu;
    @Inject
    ContactsSyncManager mSyncManager;
    @Inject
    protected YoApi.YoService yoService;

    private static final int PICK_CONTACT_REQUEST = 100;

    private boolean CONTACT_SYNC = true;
    private ListView layout;
    private boolean isAlreadyShown;
    private TextView noSearchResult;
    //private boolean isRemoved;
    private boolean isSharedPreferenceShown;
    private SearchView searchView;
    private Button btnAllContacts;
    private Button btnYoContacts;
    private TextView tvContactsCount;
    private LinearLayout llTabsLayout;

    public NewContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        preferenceEndPoint.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_contacts, container, false);
        listView = (ListView) view.findViewById(R.id.lv_contacts);
        layout = (ListView) view.findViewById(R.id.side_index);
        noSearchResult = (TextView) view.findViewById(R.id.no_search_results);
        btnAllContacts = (Button) view.findViewById(R.id.allContactsSection);
        btnYoContacts = (Button) view.findViewById(R.id.yoContactsSection);
        tvContactsCount = (TextView) view.findViewById(R.id.tv_contacts_count);
        llTabsLayout = (LinearLayout) view.findViewById(R.id.tabs_layout);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //EventBus.getDefault().register(this);
        btnAllContacts.setTextColor(getResources().getColor(R.color.contacts_selected_red));
        btnYoContacts.setTextColor(getResources().getColor(R.color.contacts_unselected_red));
        contactsListAdapter = new ContactsListAdapter(getActivity().getApplicationContext(), preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER));
        listView.setAdapter(contactsListAdapter);
        listView.setOnItemClickListener(this);

        btnAllContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnAllContacts.setTextColor(getResources().getColor(R.color.contacts_selected_red));
                btnYoContacts.setTextColor(getResources().getColor(R.color.contacts_unselected_red));
                /*contactsListAdapter = new ContactsListAdapter(getActivity().getApplicationContext(), preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER));
                listView.setAdapter(contactsListAdapter);*/

                setYoContacts(false);
            }
        });

        btnYoContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnYoContacts.setTextColor(getResources().getColor(R.color.contacts_selected_red));
                btnAllContacts.setTextColor(getResources().getColor(R.color.contacts_unselected_red));
                setYoContacts(true);
            }
        });
    }

    private void syncContacts(final boolean isYoUser) {
        List<Contact> contactsList = mSyncManager.getContacts();
        if (!contactsList.isEmpty()) {
            loadAlphabetOrder(contactsList, isYoUser);
        }

        if (contactsList.isEmpty()) {
            showProgressDialog();
        }
        mSyncManager.loadContacts(new Callback<List<Contact>>() {
            @Override
            public void onResponse(Call<List<Contact>> call, Response<List<Contact>> response) {
                noSearchResult.setVisibility(View.GONE);
                llTabsLayout.setVisibility(View.VISIBLE);
                loadAlphabetOrder(response.body(), isYoUser);
                dismissProgressDialog();
            }

            @Override
            public void onFailure(Call<List<Contact>> call, Throwable t) {
                dismissProgressDialog();
                noSearchResult.setVisibility(View.VISIBLE);
                llTabsLayout.setVisibility(View.GONE);
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    noSearchResult.setText(activity.getResources().getString(R.string.connectivity_network_settings));
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mHelper.isConnected()) {
            if (searchView != null && !TextUtils.isEmpty(searchView.getQuery())) {
                searchView.setQuery(searchView.getQuery(), false);
            } else {
                if (btnAllContacts.getCurrentTextColor() == getResources().getColor(R.color.contacts_selected_red)) {
                    syncContacts(false);
                } else {
                    syncContacts(true);
                }
            }
        } else if (!mHelper.isConnected()) {
            List<Contact> cacheContactsList = mSyncManager.getCachContacts();
            if (btnAllContacts.getCurrentTextColor() == getResources().getColor(R.color.contacts_selected_red)) {
                loadAlphabetOrder(cacheContactsList, false);
            } else {
                loadAlphabetOrder(cacheContactsList, true);
            }
        }
    }

    private void loadContacts(Cursor c) {
        try {
            List<Contact> list = new ArrayList<>();
            if (c != null && c.moveToFirst()) {
                do {
                    Contact contact = ContactsSyncManager.prepareContact(c);
                    list.add(contact);
                } while (c.moveToNext());
            }
            if (btnAllContacts.getCurrentTextColor() == getResources().getColor(R.color.contacts_selected_red)) {
                loadAlphabetOrder(list, false);
            } else {
                loadAlphabetOrder(list, true);
            }
        } finally {
            if (c != null && !c.isClosed()) c.close();
        }
    }

    private void loadAlphabetOrder(List<Contact> list, boolean isYoUser) {

        if (list != null) {
            if (isYoUser && btnYoContacts.getCurrentTextColor() == getResources().getColor(R.color.contacts_selected_red)) {
                List<Contact> yoList = new ArrayList<>();
                for (Contact contact : list) {
                    if (contact.getYoAppUser()) {
                        yoList.add(contact);
                    }
                }
                list = yoList;
            }

            Collections.sort(list, new Comparator<Contact>() {
                @Override
                public int compare(Contact lhs, Contact rhs) {
                    return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
                }
            });

            contactsListAdapter.addItems(list);
            tvContactsCount.setText("CONTACTS " + "(" + contactsListAdapter.getCount() + ")");
            listView.setAdapter(contactsListAdapter);
            Helper.displayIndex(getActivity(), layout, list, listView);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_contacts, menu);
        this.menu = menu;
        Util.changeSearchProperties(menu);
        /*MenuItem view = menu.findItem(R.id.menu_search);
        // Hide right side alphabets when search is opened.
        MenuItemCompat.setOnActionExpandListener(view, new MenuItemCompat.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                layout.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                layout.setVisibility(View.VISIBLE);
                return true;
            }
        });*/
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MenuItem view = menu.findItem(R.id.menu_search);
        // Hide right side alphabets when search is opened.
        MenuItemCompat.setOnActionExpandListener(view, new MenuItemCompat.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                layout.setVisibility(View.GONE);
                llTabsLayout.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                layout.setVisibility(View.VISIBLE);
                llTabsLayout.setVisibility(View.VISIBLE);
                getActivity().invalidateOptionsMenu();
                if (btnYoContacts.getCurrentTextColor() == getResources().getColor(R.color.contacts_selected_red)) {
                    setYoContacts(true);
                }
                return true;
            }
        });
        Util.prepareContactsSearch(getActivity(), menu, contactsListAdapter, Constants.CONT_FRAG, noSearchResult, null);
        searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        if (item.getItemId() == R.id.invite) {
            Intent i = new Intent(Intent.ACTION_INSERT);
            i.setType(ContactsContract.Contacts.CONTENT_TYPE);
            if (Integer.valueOf(Build.VERSION.SDK) > 14)
                i.putExtra("finishActivityOnSaveCompleted", true); // Fix for 4.0.3 +
            startActivityForResult(i, PICK_CONTACT_REQUEST);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CONTACT_REQUEST && data != null) {
            String nameAndNumber = uploadContact(data.getData());
            Toast.makeText(getActivity(), "Contect added " + nameAndNumber, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void showProgressDialog() {
        if (getView() != null) {
            getView().findViewById(R.id.progress).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void dismissProgressDialog() {
        if (getView() != null) {
            getView().findViewById(R.id.progress).setVisibility(View.GONE);
        }
    }

    public Menu getMenu() {
        return menu;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Contact contact = (Contact) listView.getItemAtPosition(position);
        if (contact.getYoAppUser()) {
            Intent intent = new Intent(getActivity(), UserProfileActivity.class);
            intent.putExtra(Constants.CONTACT, contact);
            startActivity(intent);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        showProgressDialog();
        return new CursorLoader(getActivity(),
                YoAppContactContract.YoAppContactsEntry.CONTENT_URI,
                PROJECTION,
                null,
                null,
                YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_IS_YOAPP_USER + " desc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        dismissProgressDialog();
        loadContacts(data);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {

            if (preferenceEndPoint != null) {
                // Capture user id
                Map<String, String> contactsParams = new HashMap<String, String>();
                String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
                //param keys and values have to be of String type
                contactsParams.put("UserId", userId);

                FlurryAgent.logEvent("Contacts", contactsParams, true);

                if (getActivity() instanceof BottomTabsActivity) {
                    BottomTabsActivity activity = (BottomTabsActivity) getActivity();
                    if (activity.getFragment() instanceof NewContactsFragment) {
                        if (preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION) != null) {
                            Type type = new TypeToken<List<Popup>>() {
                            }.getType();
                            List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
                            if (popup != null) {
                                Collections.reverse(popup);
                                isAlreadyShown = false;
                                for (Popup p : popup) {
                                    if (p.getPopupsEnum() == PopupHelper.PopupsEnum.CONTACTS) {
                                        if (!isAlreadyShown) {
                                            PopupHelper.getSinglePopup(PopupHelper.PopupsEnum.CONTACTS, p, getActivity(), preferenceEndPoint, this, this, popup);
                                            isAlreadyShown = true;
                                            isSharedPreferenceShown = false;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } else {
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (getActivity() instanceof BottomTabsActivity) {
            BottomTabsActivity activity = (BottomTabsActivity) getActivity();
            if (activity.getFragment() instanceof NewContactsFragment) {
                if (preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION) != null) {
                    Type type = new TypeToken<List<Popup>>() {
                    }.getType();
                    List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
                    if (popup != null) {
                        for (Popup p : popup) {
                            if (p.getPopupsEnum() == PopupHelper.PopupsEnum.CONTACTS) {
                                if (!isAlreadyShown) {
                                    //PopupHelper.getPopup(PopupHelper.PopupsEnum.CONTACTS, popup, getActivity(), preferenceEndPoint, this, this);
                                    PopupHelper.getSinglePopup(PopupHelper.PopupsEnum.CONTACTS, p, getActivity(), preferenceEndPoint, this, this, popup);
                                    isAlreadyShown = true;
                                    isSharedPreferenceShown = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        preferenceEndPoint.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void closePopup() {
        Type type = new TypeToken<List<Popup>>() {
        }.getType();
        List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
        if (popup != null) {
            if (!isSharedPreferenceShown) {
                Collections.reverse(popup);
            }
            List<Popup> tempPopup = new ArrayList<>(popup);
            for (Popup p : popup) {
                if (p.getPopupsEnum() == PopupHelper.PopupsEnum.CONTACTS) {
                    tempPopup.remove(p);
                    break;
                }
            }
            popup = tempPopup;
        }
        preferenceEndPoint.saveStringPreference(Constants.POPUP_NOTIFICATION, new Gson().toJson(popup));
    }

    public void onEventMainThread(String action) {
        if (Constants.CONTACTS_REFRESH.equals(action)) {
            if (btnAllContacts.getCurrentTextColor() == getResources().getColor(R.color.contacts_selected_red)) {
                syncContacts(false);
            } else {
                syncContacts(true);
            }
        }
    }

    private void setYoContacts(boolean isYoUser) {
        if (mHelper.isConnected()) {
            syncContacts(isYoUser);
        } else if (!mHelper.isConnected()) {
            List<Contact> cacheContactsList = mSyncManager.getCachContacts();
            if (cacheContactsList != null && !cacheContactsList.isEmpty()) {
                loadAlphabetOrder(cacheContactsList, isYoUser);
            } else {
                        /*noResults.setText(getString(R.string.no_contacts_found));
                        noResults.setVisibility(View.VISIBLE);*/
            }

        }
    }

    private String uploadContact(Uri uri) {
        Cursor cursor;  // Cursor object
        String mime;    // MIME type
        int dataIdx;    // Index of DATA1 column
        int mimeIdx;    // Index of MIMETYPE column
        int nameIdx;    // Index of DISPLAY_NAME column
        String contactName = null;
        String contactPhoneNumber = null;
        // Get the name
        cursor = getActivity().getContentResolver().query(uri,
                new String[]{ContactsContract.Contacts.DISPLAY_NAME},
                null, null, null);
        if (cursor.moveToFirst()) {
            nameIdx = cursor.getColumnIndex(
                    ContactsContract.Contacts.DISPLAY_NAME);
            contactName = cursor.getString(nameIdx);
            // Set up the projection
            String[] projection = {
                    ContactsContract.Data.DISPLAY_NAME,
                    ContactsContract.Contacts.Data.DATA1,
                    ContactsContract.Contacts.Data.MIMETYPE};

            // Query ContactsContract.Data
            cursor = getActivity().getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI, projection,
                    ContactsContract.Data.DISPLAY_NAME + " = ?",
                    new String[]{contactName},
                    null);

            if (cursor.moveToFirst()) {
                // Get the indexes of the MIME type and data
                mimeIdx = cursor.getColumnIndex(
                        ContactsContract.Contacts.Data.MIMETYPE);
                dataIdx = cursor.getColumnIndex(
                        ContactsContract.Contacts.Data.DATA1);

                // Match the data to the MIME type, store in variables
                do {
                    mime = cursor.getString(mimeIdx);
                    if (ContactsContract.CommonDataKinds.Email
                            .CONTENT_ITEM_TYPE.equalsIgnoreCase(mime)) {
                        String email = cursor.getString(dataIdx);
                    }
                    if (ContactsContract.CommonDataKinds.Phone
                            .CONTENT_ITEM_TYPE.equalsIgnoreCase(mime)) {
                        String phone = cursor.getString(dataIdx);
                        contactPhoneNumber = PhoneNumberUtils.formatNumber(phone);
                    }
                } while (cursor.moveToNext());
            }
        }

        String access = preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
        List<JSONObject> nameAndNumber = new ArrayList<>();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.NUMBER, contactPhoneNumber);
            jsonObject.put(Constants.NAME, contactName);
            nameAndNumber.add(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        yoService.syncContactsWithNameAPI(access, nameAndNumber).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {

            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {

            }
        });
        return contactName + " - " + contactPhoneNumber;
    }
}
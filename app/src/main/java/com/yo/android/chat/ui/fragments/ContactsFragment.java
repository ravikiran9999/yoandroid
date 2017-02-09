package com.yo.android.chat.ui.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orion.android.common.util.ConnectivityHelper;
import com.yo.android.R;
import com.yo.android.adapters.ContactsListAdapter;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.helpers.Helper;
import com.yo.android.helpers.PopupHelper;
import com.yo.android.model.Contact;
import com.yo.android.model.Popup;
import com.yo.android.provider.YoAppContactContract;
import com.yo.android.sync.SyncUtils;
import com.yo.android.ui.BottomTabsActivity;
import com.yo.android.ui.NotificationsActivity;
import com.yo.android.ui.UserProfileActivity;
import com.yo.android.util.Constants;
import com.yo.android.util.PopupDialogListener;
import com.yo.android.util.Util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */

public class ContactsFragment extends BaseFragment implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener, PopupDialogListener {

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
    private static final int PICK_CONTACT_REQUEST = 100;

    private boolean CONTACT_SYNC = true;
    private ListView layout;
    private boolean isAlreadyShown;
    private TextView noSearchResult;
    //private boolean isRemoved;
    private boolean isSharedPreferenceShown;

    public ContactsFragment() {
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
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        listView = (ListView) view.findViewById(R.id.lv_contacts);
        layout = (ListView) view.findViewById(R.id.side_index);
        noSearchResult = (TextView) view.findViewById(R.id.no_search_results);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //EventBus.getDefault().register(this);
        contactsListAdapter = new ContactsListAdapter(getActivity().getApplicationContext(), preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER));
        listView.setAdapter(contactsListAdapter);
        listView.setOnItemClickListener(this);
    }

    private void syncContacts() {
        List<Contact> contactsList = mSyncManager.getContacts();
        if (!contactsList.isEmpty()) {
            loadAlphabetOrder(contactsList);
        }

        if (contactsList.isEmpty()) {
            showProgressDialog();
        }
        mSyncManager.loadContacts(new Callback<List<Contact>>() {
            @Override
            public void onResponse(Call<List<Contact>> call, Response<List<Contact>> response) {
                noSearchResult.setVisibility(View.GONE);
                loadAlphabetOrder(response.body());
                dismissProgressDialog();
            }

            @Override
            public void onFailure(Call<List<Contact>> call, Throwable t) {
                dismissProgressDialog();
                noSearchResult.setVisibility(View.VISIBLE);
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
            syncContacts();
        } else if (!mHelper.isConnected()) {
            List<Contact> cacheContactsList = mSyncManager.getCachContacts();
            loadAlphabetOrder(cacheContactsList);
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
            loadAlphabetOrder(list);
        } finally {
            if (c != null) c.close();
        }
    }

    private void loadAlphabetOrder(List<Contact> list) {

        Collections.sort(list, new Comparator<Contact>() {
            @Override
            public int compare(Contact lhs, Contact rhs) {
                return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
            }
        });

        contactsListAdapter.addItems(list);
        Helper.displayIndex(getActivity(), layout, list, listView);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_contacts, menu);
        this.menu = menu;
        Util.changeSearchProperties(menu);
        MenuItem view = menu.findItem(R.id.menu_search);
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
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Util.prepareContactsSearch(getActivity(), menu, contactsListAdapter, Constants.CONT_FRAG, noSearchResult);
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
        if (requestCode == PICK_CONTACT_REQUEST) {
           
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
            if (getActivity() instanceof BottomTabsActivity) {
                BottomTabsActivity activity = (BottomTabsActivity) getActivity();
                if (activity.getFragment() instanceof ContactsFragment) {
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
                                        //PopupHelper.getPopup(PopupHelper.PopupsEnum.CONTACTS, popup, getActivity(), preferenceEndPoint, this, this);
                                        PopupHelper.getSinglePopup(PopupHelper.PopupsEnum.CONTACTS, p, getActivity(), preferenceEndPoint, this, this, popup);
                                        isAlreadyShown = true;
                                        isSharedPreferenceShown = false;
                                        break;
                                    }
                                }
                            }
                        }
                        /*if (popup != null && popup.size() > 0 && popup.get(0).getPopupsEnum() == PopupHelper.PopupsEnum.CONTACTS) {
                            if (!isAlreadyShown) {
                                PopupHelper.getPopup(PopupHelper.PopupsEnum.CONTACTS, popup, getActivity(), preferenceEndPoint, this, this);
                                isAlreadyShown = true;
                            }
                        }*/
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
            if (activity.getFragment() instanceof ContactsFragment) {
                if (preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION) != null) {
                    //if (!isRemoved) {
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
                        /*if (popup != null && popup.size() > 0 && popup.get(0).getPopupsEnum() == PopupHelper.PopupsEnum.CONTACTS) {
                            if (!isAlreadyShown) {
                                PopupHelper.getPopup(PopupHelper.PopupsEnum.CONTACTS, popup, getActivity(), preferenceEndPoint, this, this);
                                isAlreadyShown = true;
                            }
                        }*/
                    /*} else {
                        isRemoved = false;
                    }*/
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
        //isAlreadyShown = false;
        //isRemoved = true;
        //preferenceEndPoint.removePreference(Constants.POPUP_NOTIFICATION);
        Type type = new TypeToken<List<Popup>>() {
        }.getType();
        List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
        if (popup != null) {
            if(!isSharedPreferenceShown) {
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
        //popup.remove(0);
        preferenceEndPoint.saveStringPreference(Constants.POPUP_NOTIFICATION, new Gson().toJson(popup));
    }

    public void onEventMainThread(String action) {
        if (Constants.CONTACTS_REFRESH.equals(action)) {
            syncContacts();
        }
    }
}

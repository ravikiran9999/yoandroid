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
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import com.yo.android.util.Util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */

public class ContactsFragment extends BaseFragment implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {
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

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        preferenceEndPoint.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        listView = (ListView) view.findViewById(R.id.lv_contacts);
        layout = (ListView) view.findViewById(R.id.side_index);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        contactsListAdapter = new ContactsListAdapter(getActivity().getApplicationContext(), preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER));
        listView.setAdapter(contactsListAdapter);
        listView.setOnItemClickListener(this);
        /*if (CONTACT_SYNC) {
            getLoaderManager().initLoader(0, null, this);
            //Manual refresh
            SyncUtils.triggerRefresh();
        } else {*/
        if (!mSyncManager.getContacts().isEmpty()) {
            loadAlphabetOrder(mSyncManager.getContacts());
        }
        if (mSyncManager.getContacts().isEmpty()) {
            showProgressDialog();
        }
        mSyncManager.loadContacts(new Callback<List<Contact>>() {
            @Override
            public void onResponse(Call<List<Contact>> call, Response<List<Contact>> response) {
                loadAlphabetOrder(mSyncManager.getContacts());

                dismissProgressDialog();
            }

            @Override
            public void onFailure(Call<List<Contact>> call, Throwable t) {
                dismissProgressDialog();
            }
        });

        //}
    }

    private void loadContacts(Cursor c) {
        List<Contact> list = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            do {
                Contact contact = ContactsSyncManager.prepareContact(c);
                list.add(contact);
            } while (c.moveToNext());
        }
        loadAlphabetOrder(list);

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
        inflater.inflate(R.menu.menu_contacts, menu);
        this.menu = menu;
        Util.prepareContactsSearch(getActivity(), menu, contactsListAdapter, Constants.CONT_FRAG);
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
        if (item.getItemId() == R.id.invite) {
            Intent i = new Intent(Intent.ACTION_INSERT);
            i.setType(ContactsContract.Contacts.CONTENT_TYPE);
            if (Integer.valueOf(Build.VERSION.SDK) > 14)
                i.putExtra("finishActivityOnSaveCompleted", true); // Fix for 4.0.3 +
            startActivityForResult(i, PICK_CONTACT_REQUEST);
            return true;
        } /*else if (item.getItemId() == R.id.notification_icon) {
            startActivity(new Intent(getActivity(), NotificationsActivity.class));
        }*/


        return super.onOptionsItemSelected(item);
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
            if(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION) != null) {
                Type type = new TypeToken<List<Popup>>() {
                }.getType();
                List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
                PopupHelper.getPopup(PopupHelper.PopupsEnum.CONTACTS, popup, getActivity(), preferenceEndPoint, this);
            }
        }
        else {
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(((BottomTabsActivity)getActivity()).getSupportActionBar().getTitle().equals(getString(R.string.contacts))) {
            if(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION) != null) {
                Type type = new TypeToken<List<Popup>>() {
                }.getType();
                List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
                PopupHelper.getPopup(PopupHelper.PopupsEnum.CONTACTS, popup, getActivity(), preferenceEndPoint, this);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        preferenceEndPoint.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}

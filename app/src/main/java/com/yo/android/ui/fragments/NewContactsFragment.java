package com.yo.android.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.telephony.PhoneNumberUtils;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TypefaceSpan;
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
import com.yo.android.api.ApiCallback;
import com.yo.android.api.YoApi;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.helpers.Helper;
import com.yo.android.helpers.PopupHelper;
import com.yo.android.model.Contact;
import com.yo.android.model.Popup;
import com.yo.android.provider.YoAppContactContract;
import com.yo.android.typeface.CustomTypefaceSpan;
import com.yo.android.ui.BottomTabsActivity;
import com.yo.android.ui.UserProfileActivity;
import com.yo.android.usecase.ContactsSyncWithNameUsecase;
import com.yo.android.usecase.WebserviceUsecase;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class NewContactsFragment extends BaseFragment implements AdapterView.OnItemClickListener, SharedPreferences.OnSharedPreferenceChangeListener, PopupDialogListener {
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
            YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_FIREBASE_USER_ID

    };
    private ContactsListAdapter contactsListAdapter;
    private ListView listView;
    private Menu menu;

    @Inject
    ContactsSyncManager mSyncManager;
    @Inject
    protected YoApi.YoService yoService;
    @Inject
    ContactsSyncWithNameUsecase contactsSyncWithNameUsecase;

    private static final int PICK_CONTACT_REQUEST = 100;

    private boolean isAlreadyShown;

    @Bind(R.id.no_search_results)
    TextView noSearchResult;
    @Bind(R.id.allContactsSection)
    Button btnAllContacts;
    @Bind(R.id.yoContactsSection)
    Button btnYoContacts;
    @Bind(R.id.tv_contacts_count)
    TextView tvContactsCount;
    @Bind(R.id.tabs_layout)
    LinearLayout llTabsLayout;
    @Bind(R.id.no_contacts)
    TextView tvNoContacts;
    @Bind(R.id.side_index)
    ListView sideLayout;

    private boolean isAllContactsSelected = true;
    private boolean isSharedPreferenceShown;
    private SearchView searchView;
    private Activity activity;

    private List<Contact> allContacts;

    public NewContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        preferenceEndPoint.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_contacts, container, false);
        ButterKnife.bind(this, view);
        listView = (ListView) view.findViewById(R.id.lv_contacts);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Typeface alexBrushRegular = getAlexBrushRegular();
        btnYoContacts.setAlpha(0.5f);
        btnYoContacts.setTypeface(alexBrushRegular);


        contactsListAdapter = new ContactsListAdapter(activity, preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER));
        listView.setAdapter(contactsListAdapter);
        listView.setOnItemClickListener(this);

        btnAllContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAllContactsSelected = true;
                btnAllContacts.setAlpha(1);
                btnYoContacts.setAlpha(0.5f);

                loadAlphabetOrder(allContacts);
            }
        });

        btnYoContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAllContactsSelected = false;
                btnAllContacts.setAlpha(0.5f);
                btnYoContacts.setAlpha(1);
                List<Contact> onlyYoUsers = filterYoContacts(allContacts);
                updateYoUsers(onlyYoUsers);
            }
        });
        loadContactsFromPref();
    }

    private void loadContactsFromPref() {
        String storedContacts = preferenceEndPoint.getStringPreference(Constants.STORED_CONTACTS, null);
        if (TextUtils.isEmpty(storedContacts)) {
            //There are no contacts data from the server, send request to get contacts from the server.
            syncContactsFromServer();
        } else {
            List<Contact> contactList = readContactsObjFromPref(storedContacts);
            allContacts = contactList;
            //for caching contacts
            mSyncManager.setContacts(allContacts);
            loadAlphabetOrder(contactList);
            //To get newly added contacts - after loading from cache loading for new contacts.
            if (mHelper.isConnected()) {
                syncContactsFromServer();
            }
        }
    }

    private List<Contact> readContactsObjFromPref(String storedContacts) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<Contact>>() {
        }.getType();
        return gson.fromJson(storedContacts, type);
    }


    private void updateYoUsers(List<Contact> onlyYoUsers) {
        if (onlyYoUsers != null) {
            if (onlyYoUsers.size() == 0) {
                //Display NO yo users.
            } else {
                loadAlphabetOrder(onlyYoUsers);
            }
        }
    }

    /**
     * @param allContacts
     * @return null if no yo contacts available.
     */
    private List<Contact> filterYoContacts(List<Contact> allContacts) {
        if (allContacts != null) {
            List<Contact> onlyYoUsers = new ArrayList<>();
            for (Contact contact : allContacts) {
                if (contact.isYoAppUser()) {
                    onlyYoUsers.add(contact);
                }
            }
            return onlyYoUsers.size() == 0 ? null : onlyYoUsers;
        }
        return null;
    }

    public <T> void storeContactsIntoPref(String key, List<T> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        set(key, json);
    }

    public void set(String key, String value) {
        SharedPreferences.Editor editor = preferenceEndPoint.getSharedPreferences().edit();
        editor.putString(key, value);
        editor.commit();
    }

    private void syncContactsFromServer() {
        if(contactsListAdapter.getCount() <= 10 ) {
            showProgressDialog();
        }
        mSyncManager.loadContacts(new Callback<List<Contact>>() {
            @Override
            public void onResponse(Call<List<Contact>> call, Response<List<Contact>> response) {
                tvNoContacts.setVisibility(View.GONE);
                llTabsLayout.setVisibility(View.VISIBLE);
                List<Contact> list = response.body();
                loadAlphabetOrder(list);
                allContacts = list;
                // for caching contacts.
                mSyncManager.setContacts(allContacts);
                storeContactsIntoPref(Constants.STORED_CONTACTS, list);
                dismissProgressDialog();
            }

            @Override
            public void onFailure(Call<List<Contact>> call, Throwable t) {
                dismissProgressDialog();
                if (activity != null && allContacts != null && allContacts.size() == 0) {
                    tvNoContacts.setVisibility(View.VISIBLE);
                    tvNoContacts.setText(activity.getResources().getString(R.string.connectivity_network_settings));
                }
            }
        });
    }

    private void loadAlphabetOrder(List<Contact> list) {
        if (list != null) {
            Collections.sort(list, new Comparator<Contact>() {
                @Override
                public int compare(Contact lhs, Contact rhs) {
                    return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
                }
            });
            contactsListAdapter.addItems(list);
            tvContactsCount.setText("CONTACTS " + "(" + contactsListAdapter.getCount() + ")");
            listView.setAdapter(contactsListAdapter);
            Helper.displayIndex(getActivity(), sideLayout, list, listView);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_contacts, menu);
        this.menu = menu;
        Util.changeSearchProperties(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Util.prepareContactsSearch(activity, menu, contactsListAdapter, Constants.CONT_FRAG, noSearchResult, null);
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        MenuItem view = menu.findItem(R.id.menu_search);
        // Hide right side alphabets when search is opened.
        MenuItemCompat.setOnActionExpandListener(view, new MenuItemCompat.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                sideLayout.setVisibility(View.GONE);
                llTabsLayout.setVisibility(View.GONE);
                return true;
            }

            // Display All contacts or Yo contacts based on selection after closing search view
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                sideLayout.setVisibility(View.VISIBLE);
                llTabsLayout.setVisibility(View.VISIBLE);
                getActivity().invalidateOptionsMenu();
                if (isAllContactsSelected) {
                    loadAlphabetOrder(allContacts);
                    contactsListAdapter.updateItems(allContacts);
                } else {
                    List<Contact> onlyYoUsers = filterYoContacts(allContacts);
                    updateYoUsers(onlyYoUsers);
                    contactsListAdapter.updateItems(onlyYoUsers);
                }
                return true;
            }
        });
        //contactsListAdapter.updateItems(allContacts);

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
            //Toast.makeText(getActivity(), "Contect added " + nameAndNumber, Toast.LENGTH_LONG).show();
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
        if (contact.isYoAppUser()) {
            Intent intent = new Intent(getActivity(), UserProfileActivity.class);
            intent.putExtra(Constants.CONTACT, contact);
            startActivity(intent);
        }
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {

            sideLayout.setVisibility(View.VISIBLE);
            llTabsLayout.setVisibility(View.VISIBLE);
            getActivity().invalidateOptionsMenu();
            if (isAllContactsSelected) {
                loadAlphabetOrder(allContacts);
                contactsListAdapter.updateItems(allContacts);
            } else {
                List<Contact> onlyYoUsers = filterYoContacts(allContacts);
                updateYoUsers(onlyYoUsers);
                contactsListAdapter.updateItems(onlyYoUsers);
            }

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


    private String uploadContact(Uri uri) {
        Cursor cursor;  // Cursor object
        String mime;    // MIME type
        int dataIdx;    // Index of DATA1 column
        int mimeIdx;    // Index of MIMETYPE column
        int nameIdx;    // Index of DISPLAY_NAME column
        String contactName = null;
        String contactPhoneNumber = null;
        // Get the name
        cursor = activity.getContentResolver().query(uri,
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
            cursor = activity.getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI, projection,
                    ContactsContract.Data.DISPLAY_NAME + " = ?",
                    new String[]{contactName},
                    null);

            if (cursor != null && cursor.moveToFirst()) {
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

        final List<JSONObject> nameAndNumber = new ArrayList<>();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.NUMBER, contactPhoneNumber);
            jsonObject.put(Constants.NAME, contactName);
            nameAndNumber.add(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (activity != null && Util.isOnline(activity)) {
            contactsSyncWithNameUsecase.contactsSyncWithName(nameAndNumber, new ApiCallback<JsonElement>() {
                @Override
                public void onResult(JsonElement result) {
                    syncContactsFromServer();
                }

                @Override
                public void onFailure(String message) {
                    //if its failed to add try later to add failed contacts.
                    storeOfflineAddedContacts(nameAndNumber);
                }
            });

        } else {
            //Contact added but not synced to sever, when network is back, contact should be synced.
            storeOfflineAddedContacts(nameAndNumber);
        }

        return contactName + " - " + contactPhoneNumber;
    }

    private void storeOfflineAddedContacts(List<JSONObject> nameAndNumber) {
        String offlineContacts = preferenceEndPoint.getStringPreference(Constants.OFFLINE_ADDED_CONTACTS);
        Gson gson = new Gson();
        if (TextUtils.isEmpty(offlineContacts)) {
            String json = gson.toJson(nameAndNumber);
            set(Constants.OFFLINE_ADDED_CONTACTS, json);
        } else {
            Type type = new TypeToken<List<JSONObject>>() {
            }.getType();
            List<JSONObject> jsonObjects = gson.fromJson(offlineContacts, type);
            for (JSONObject object : nameAndNumber) {
                jsonObjects.add(object);
            }
            String json = gson.toJson(jsonObjects);
            set(Constants.OFFLINE_ADDED_CONTACTS, json);
        }
    }
}

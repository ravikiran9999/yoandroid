package com.yo.android.chat.firebase;

import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.api.YoApi;
import com.yo.android.model.Contact;
import com.yo.android.provider.YoAppContactContract;
import com.yo.android.sync.YoContactsSyncAdapter;
import com.yo.android.util.Constants;
import com.yo.android.util.ContactSyncHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by rdoddapaneni on 7/15/2016.
 */

@Singleton
public class ContactsSyncManager {

    private static final String TAG = "ContactsSyncManager";
    private YoApi.YoService yoService;
    private Context context;
    private List<Contact> cacheList;
    private Object lock = new Object();
    private PreferenceEndPoint loginPrefs;
    final boolean IS_CONTACT_SYNC_ON = false;
    public static final String[] PROJECTION = new String[]{
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
    // Constants representing column positions from PROJECTION.
    public static final int COLUMN_ID = 0;
    public static final int COLUMN_ENTRY_ID = 1;
    public static final int COLUMN_NAME = 2;
    public static final int COLUMN_PHONE = 3;
    public static final int COLUMN_IMAGE = 4;
    public static final int COLUMN_FIREBASE_ROOM_ID = 5;
    public static final int COLUMN_YO_USER = 6;
    public static final int COLUMN_VOX_USERNAME = 7;
    public static final int COLUMN_COUNTRY_CODE = 8;
    public static final int COLUMN_FIREBASE_USER_ID = 9;


    @Inject
    public ContactsSyncManager(YoApi.YoService yoService, Context context, @Named("login") PreferenceEndPoint loginPrefs) {
        this.yoService = yoService;
        this.context = context;
        this.loginPrefs = loginPrefs;
    }

    public void checkContacts() {

        syncContacts();
    }

    public void syncContacts() {
        /*if (!IS_CONTACT_SYNC_ON) {
            return;
        }*/
        new AsyncTask<Void, Void, List<Contact>>() {
            @Override
            protected List<Contact> doInBackground(Void... params) {
                List<Contact> contactList = readContacts();
                return contactList;
            }

            @Override
            protected void onPostExecute(List<Contact> contactsObject) {
                super.onPostExecute(contactsObject);
                try {
                    syncContactsAPI(contactsObject);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
    }

    public void syncContactsAPI(List<Contact> contacts) throws IOException {
        String access = loginPrefs.getStringPreference(YoApi.ACCESS_TOKEN);
        List<JSONObject> nameAndNumber = new ArrayList<>();
        for (int i = 0; i < contacts.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(Constants.NUMBER, contacts.get(i).getPhoneNo());
                jsonObject.put(Constants.NAME, contacts.get(i).getName());
                nameAndNumber.add(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //Response<List<Contact>> response = yoService.syncContactsWithNameAPI(access, nameAndNumber).execute().body();
        //Asynchronoss call, which will not get stuck UI.
        yoService.syncContactsWithNameAPI(access, nameAndNumber).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {

            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {

            }
        });

    }

    private List<Contact> readContacts() {
        List<Contact> contactList = new ArrayList<>();

        Cursor contactsCursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (contactsCursor != null) {
            while (contactsCursor.moveToNext()) {
                Contact contact = new Contact();
                String contactId = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts._ID));
                contact.setName(contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                if (Integer.parseInt(contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor phoneNumberCursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{contactId}, null);
                    if (phoneNumberCursor != null) {

                        while (phoneNumberCursor.moveToNext()) {
                            String phoneNumber = phoneNumberCursor.getString(phoneNumberCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            contact.setPhoneNo(phoneNumber);
                        }
                        phoneNumberCursor.close();
                        contactList.add(contact);
                    }
                }
            }
            contactsCursor.close();

        }
        return contactList;
    }

    public void setContacts(List<Contact> list) {
        synchronized (lock) {
            if (list == null) {
                list = new ArrayList<>();
            }
            this.cacheList = new ArrayList<>(list);
            Collections.sort(cacheList, new Comparator<Contact>() {
                @Override
                public int compare(Contact lhs, Contact rhs) {
                    return Boolean.valueOf(rhs.isYoAppUser()).compareTo(lhs.isYoAppUser());
                }
            });
        }
    }

    public List<Contact> getContacts() {
        synchronized (lock) {
            if (cacheList == null) {
                cacheList = new ArrayList<>();
            }
        }
        return cacheList;
    }

    public Contact getContactByVoxUserName(String voxUserName) {
        Cursor c = null;
        try {
            if (voxUserName != null) {
                Uri uri = YoAppContactContract.YoAppContactsEntry.CONTENT_URI;
                c = context.getContentResolver().query(uri, PROJECTION, YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_VOX_USER_NAME + "= '" + voxUserName + "'", null, YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_IS_YOAPP_USER + " desc");
                if (c != null && c.moveToFirst()) {
                    return ContactsSyncManager.prepareContact(c);
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return null;
    }

    public Contact getContactPSTN(int countrycode, String pstnnumber) {
        if (pstnnumber != null) {
            Uri uri = YoAppContactContract.YoAppContactsEntry.CONTENT_URI;
            Cursor c = context.getContentResolver().query(uri, PROJECTION, YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_COUNTRY_CODE + "= '" + countrycode + "' and " + YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_PHONE_NUMBER + " = '" + pstnnumber + "'", null, null);
            if (c != null && c.moveToFirst()) {
                Contact contact = ContactsSyncManager.prepareContact(c);
                return contact;

            }
        }
        return null;
    }

    public Contact getContactByPhoneNumber(String phoneNumber) {
        if (phoneNumber != null) {
            Uri uri = YoAppContactContract.YoAppContactsEntry.CONTENT_URI;
            Cursor c = context.getContentResolver().query(uri, PROJECTION, YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_PHONE_NUMBER + "= '" + phoneNumber + "'", null, YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_IS_YOAPP_USER + " desc");
            if (c != null && c.moveToFirst()) {
                return ContactsSyncManager.prepareContact(c);
            }
        }
        return null;
    }

    public String getContactNameByPhoneNumber(String phoneNumber) {
        if (phoneNumber != null) {
            Uri uri = YoAppContactContract.YoAppContactsEntry.CONTENT_URI;
            Cursor c = context.getContentResolver().query(uri, PROJECTION, YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_PHONE_NUMBER + "= '" + phoneNumber + "'", null, YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_IS_YOAPP_USER + " desc");

            if (c != null && c.moveToFirst()) {
                Contact contact = ContactsSyncManager.prepareContact(c);
                if (contact.getName() != null && !TextUtils.isEmpty(contact.getName())) {
                    return contact.getName();
                } else {
                    return contact.getPhoneNo();
                }
            }
        }
        return phoneNumber;
    }

    public Map<String, Contact> getCachedContacts() {
        Uri uri = YoAppContactContract.YoAppContactsEntry.CONTENT_URI;
        //Cursor c = context.getContentResolver().query(uri, PROJECTION, null, null, YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_IS_YOAPP_USER + " desc");
        Cursor c = context.getContentResolver().query(uri, PROJECTION, null, null, null);
        List<Contact> contactList = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            do {
                Contact contact = ContactsSyncManager.prepareContact(c);
                contactList.add(contact);
            } while (c.moveToNext());
        }
        if (c != null) {
            c.close();
        }
        Map<String, Contact> cacheYoAppContacts = new HashMap<>();
        for (Contact contact : contactList) {
            String number = ContactSyncHelper.stripExceptNumbers(contact.getPhoneNo(), false);
            cacheYoAppContacts.put(number, contact);
        }
        return cacheYoAppContacts;
    }

    public List<Contact> getCachContacts() {
        Uri uri = YoAppContactContract.YoAppContactsEntry.CONTENT_URI;
        Cursor c = context.getContentResolver().query(uri, PROJECTION, null, null, null);
        List<Contact> contactList = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            do {
                Contact contact = ContactsSyncManager.prepareContact(c);
                contactList.add(contact);
            } while (c.moveToNext());
        }
        setContacts(contactList);
        if (c != null) {
            c.close();
        }

        return contactList;
    }

    public void loadContacts(final Callback<List<Contact>> callback) {

        String access = loginPrefs.getStringPreference(YoApi.ACCESS_TOKEN);
        yoService.getContacts(access).enqueue(new Callback<List<Contact>>() {
            @Override
            public void onResponse(Call<List<Contact>> call, Response<List<Contact>> response) {
                setContacts(response.body());
                if (callback != null) {
                    callback.onResponse(call, response);
                }
                try {
                    YoContactsSyncAdapter.updateLocalFeedData(context, cacheList, new SyncResult());
                } catch (RemoteException | OperationApplicationException e) {
                    e.printStackTrace();
                }
                //response.raw().close();
            }

            @Override
            public void onFailure(Call<List<Contact>> call, Throwable t) {
                if (callback != null) {
                    callback.onFailure(call, t);
                }
            }
        });
    }

    public static Contact prepareContact(Cursor c) {
        String entryId = c.getString(COLUMN_ENTRY_ID);
        String name = c.getString(COLUMN_NAME);
        String phone = c.getString(COLUMN_PHONE);
        String image = c.getString(COLUMN_IMAGE);
        String roomId = c.getString(COLUMN_FIREBASE_ROOM_ID);
        String firebaseUserId = c.getString(COLUMN_FIREBASE_USER_ID);
        boolean yoAppUser = c.getInt(COLUMN_YO_USER) != 0;
        String voxUserName = c.getString(COLUMN_VOX_USERNAME);
        String countryCode = c.getString(COLUMN_COUNTRY_CODE);
        //
        Contact contact = new Contact();
        contact.setId(entryId);
        contact.setName(name);
        //contact.setName(getName(name, phone));
        contact.setPhoneNo(phone);
        contact.setImage(image);
        contact.setFirebaseRoomId(roomId);
        contact.setFirebaseUserId(firebaseUserId);
        contact.setYoAppUser(yoAppUser);
        contact.setCountryCode(countryCode);
        contact.setNexgieUserName(voxUserName);
        return contact;
    }

    private static String getName(String name, String number) {
        String formatedName = name.replaceAll("\\s+", "");
        if (!formatedName.equalsIgnoreCase(number)) {
            return name;
        }
        return "";
    }
}

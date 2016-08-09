package com.yo.android.chat.firebase;

import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.provider.ContactsContract;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.api.YoApi;
import com.yo.android.model.Contact;
import com.yo.android.sync.YoContactsSyncAdapter;
import com.yo.android.util.ContactSyncHelper;

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

    private YoApi.YoService yoService;
    private Context context;
    private List<Contact> cacheList;
    private Object lock = new Object();
    PreferenceEndPoint loginPrefs;
    final boolean IS_CONTACT_SYNC_ON = false;

    @Inject
    public ContactsSyncManager(YoApi.YoService yoService, Context context, @Named("login") PreferenceEndPoint loginPrefs) {
        this.yoService = yoService;
        this.context = context;
        this.loginPrefs = loginPrefs;
    }


    public void syncContacts() {
        if (!IS_CONTACT_SYNC_ON) {
            return;
        }
        new AsyncTask<Void, Void, List<String>>() {
            @Override
            protected List<String> doInBackground(Void... params) {
                return readContacts();
            }

            @Override
            protected void onPostExecute(List<String> strings) {
                super.onPostExecute(strings);
                String access = loginPrefs.getStringPreference(YoApi.ACCESS_TOKEN);
                yoService.syncContactsAPI(access, strings).enqueue(new Callback<List<Contact>>() {
                    @Override
                    public void onResponse(Call<List<Contact>> call, Response<List<Contact>> response) {
                        if (response != null) {
                            setContacts(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Contact>> call, Throwable t) {

                    }
                });
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);

    }

    public Response<List<Contact>> syncContactsAPI(List<String> contacts) throws IOException {
        String access = loginPrefs.getStringPreference(YoApi.ACCESS_TOKEN);
        Response<List<Contact>> response = yoService.syncContactsAPI(access, contacts).execute();
        setContacts(response.body());
        return response;
    }

    private List<String> readContacts() {
        List<String> nc = new ArrayList<>();
        Cursor contactsCursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (contactsCursor != null) {
            while (contactsCursor.moveToNext()) {
                String contactId = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts._ID));
                if (Integer.parseInt(contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor phoneNumberCursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{contactId}, null);
                    if (phoneNumberCursor != null) {
                        while (phoneNumberCursor.moveToNext()) {
                            String phoneNumber = phoneNumberCursor.getString(phoneNumberCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            nc.add(phoneNumber);
                        }
                        phoneNumberCursor.close();
                    }
                }
            }
            contactsCursor.close();

        }
        return nc;
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
                    return Boolean.valueOf(rhs.getYoAppUser()).compareTo(Boolean.valueOf(lhs.getYoAppUser()));
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

    public Map<String, Contact> getCachedContacts() {
        List<Contact> contactList = new ArrayList<>(getContacts());
        Map<String, Contact> cacheYoAppContacts = new HashMap<>();
        for (Contact contact : contactList) {
            String number = ContactSyncHelper.stripExceptNumbers(contact.getPhoneNo(), false);
            cacheYoAppContacts.put(number, contact);
        }
        return cacheYoAppContacts;
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

            }

            @Override
            public void onFailure(Call<List<Contact>> call, Throwable t) {
                if (callback != null) {
                    callback.onFailure(call, t);
                }
            }
        });
    }
}

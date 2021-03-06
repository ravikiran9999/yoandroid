/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yo.android.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.api.YoApi;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.di.Injector;
import com.yo.android.model.Contact;
import com.yo.android.provider.YoAppContactContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Define a sync adapter for the app.
 * <p/>
 * <p>This class is instantiated in {@link YoContactsSyncAdapter}, which also binds SyncAdapter to the system.
 * SyncAdapter should only be initialized in SyncService, never anywhere else.
 * <p/>
 * <p>The system calls onPerformSync() via an RPC call through the IBinder object supplied by
 * SyncService.
 */
public class YoContactsSyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String TAG = "SyncAdapter";
    @Inject
    protected YoApi.YoService mYoService;
    @Inject
    @Named("login")
    PreferenceEndPoint loginPrefs;
    @Inject
    ContactsSyncManager mContactsSyncManager;

    private static Object lock = new Object();

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

    private List<Contact> contacts;

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public YoContactsSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        Injector.obtain(context.getApplicationContext()).inject(this);
    }

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public YoContactsSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        Injector.obtain(context.getApplicationContext()).inject(this);
    }

    /**
     * Called by the Android system in response to a request to run the sync adapter. The work
     * required to read data from the network, parse it, and store it in the content provider is
     * done here. Extending AbstractThreadedSyncAdapter ensures that all methods within SyncAdapter
     * run on a background thread. For this reason, blocking I/O and other long-running tasks can be
     * run <em>in situ</em>, and you don't have to set up a separate thread for them.
     * .
     * <p/>
     * <p>This is where we actually perform any work required to perform a sync.
     * {@link AbstractThreadedSyncAdapter} guarantees that this will be called on a non-UI thread,
     * so it is safe to peform blocking I/O here.
     * <p/>
     * <p>The syncResult argument allows you to pass information back to the method that triggered
     * the sync.
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, final SyncResult syncResult) {
        //   Log.i(TAG, "Beginning network synchronization");
        contacts = null;
        String access = loginPrefs == null ? "" : loginPrefs.getStringPreference(YoApi.ACCESS_TOKEN);
        if (TextUtils.isEmpty(access)) {
            return;
        }

        mYoService.getContacts(access).enqueue(new Callback<List<Contact>>() {
            @Override
            public void onResponse(Call<List<Contact>> call, Response<List<Contact>> list) {
                try {
                    if (list.isSuccessful()) {
                        contacts = list.body();
                    } else {
                        contacts = mContactsSyncManager.getCachContacts();
                    }
                    //Store them in cache
                    mContactsSyncManager.setContacts(contacts);
                    new AsyncTask<Void, Void, Void>() {

                        @Override
                        protected Void doInBackground(Void... params) {
                            try {
                                updateLocalFeedData(getContext(), contacts, syncResult);
                            } catch (RemoteException | OperationApplicationException e) {
                                Log.e(TAG, "Error updating database: " + e.toString());
                                syncResult.databaseError = true;
                                e.printStackTrace();
                            }
                            return null;
                        }
                    };
                } finally {
                    //list.raw().close();
                }
            }

            @Override
            public void onFailure(Call<List<Contact>> call, Throwable t) {
                t.printStackTrace();
                Log.e(TAG, "Error Get Contacts: " + t.getMessage());
            }
        });


        //  Log.i(TAG, "Network synchronization complete");
    }

    /**
     * <p/>
     * <p>This is where incoming data is persisted, committing the results of a sync. In order to
     * minimize (expensive) disk operations, we compare incoming data with what's already in our
     * database, and compute a merge. Only changes (insert/update/delete) will result in a database
     * write.
     * <p/>
     * <p>As an additional optimization, we use a batch operation to perform all database writes at
     * once.
     * <p/>
     * <p>Merge strategy:
     * 1. Get cursor to all items in feed<br/>
     * 2. For each item, check if it's in the incoming data.<br/>
     * a. YES: Remove from "incoming" list. Check if data has mutated, if so, perform
     * database UPDATE.<br/>
     * b. NO: Schedule DELETE from database.<br/>
     * (At this point, incoming database only contains missing items.)<br/>
     * 3. For any items remaining in incoming list, ADD to database.
     */
    public static synchronized void updateLocalFeedData(Context context, List<Contact> contacts, final SyncResult syncResult) throws RemoteException, OperationApplicationException {
        final ContentResolver contentResolver = context.getContentResolver();
        ContentResolver mContentResolver = context.getContentResolver();
        // Log.i(TAG, "Parsing stream as Atom feed");
        final List<Entry> entries = prepareEntries(contacts);
        //Log.i(TAG, "Parsing complete. Found " + entries.size() + " entries");


        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

        // Build hash table of incoming entries
        HashMap<String, Entry> entryMap = new HashMap<String, Entry>();
        for (Entry e : entries) {
            entryMap.put(e.phone, e);
        }

        // Get list of all items
        //  Log.i(TAG, "Fetching local entries for merge");
        Uri uri = YoAppContactContract.YoAppContactsEntry.CONTENT_URI; // Get all entries
        Cursor c = contentResolver.query(uri, PROJECTION, null, null, YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_IS_YOAPP_USER + " desc");
        assert c != null;
        // Log.i(TAG, "Found " + c.getCount() + " local entries. Computing merge solution...");

        // Find stale data
        int id;
        String entryId;
        String name;
        String phone;
        String image;
        String roomId;
        boolean yoAppUser;
        String voxUserName;
        String countryCode;
        String firebaseUserId;

        while (c.moveToNext()) {
            syncResult.stats.numEntries++;
            id = c.getInt(COLUMN_ID);
            entryId = c.getString(COLUMN_ENTRY_ID);
            name = c.getString(COLUMN_NAME);
            phone = c.getString(COLUMN_PHONE);
            image = c.getString(COLUMN_IMAGE);
            roomId = c.getString(COLUMN_FIREBASE_ROOM_ID);
            firebaseUserId = c.getString(COLUMN_FIREBASE_USER_ID);
            yoAppUser = c.getInt(COLUMN_YO_USER) != 0;
            voxUserName = c.getString(COLUMN_VOX_USERNAME);
            countryCode = c.getString(COLUMN_COUNTRY_CODE);
            Entry match = entryMap.get(phone);
            if (match != null) {
                // Entry exists. Remove from entry map to prevent insert later.
                entryMap.remove(phone);
                // Check to see if the entry needs to be updated
                Uri existingUri = YoAppContactContract.YoAppContactsEntry.CONTENT_URI.buildUpon()
                        .appendPath(Integer.toString(id)).build();
                if ((match.yoappuser != yoAppUser) ||
                        (match.phone != null && !match.phone.equals(phone)) ||
                        (match.firebaseRoomId != null && !match.firebaseRoomId.equals(roomId)) ||
                        (match.firebaseUserId != null && !match.firebaseUserId.equals(firebaseUserId)) ||
                        (roomId != null && !roomId.equals(match.firebaseRoomId)) ||
                        (match.name != null && !match.name.equals(name)) ||
                        (match.image != null && !match.image.equals(image)) ||
                        (match.voxUserName != null && !match.voxUserName.equals(voxUserName)) ||
                        (match.countryCode != null && !match.countryCode.equals(countryCode))
                        ) {
                    // Update existing record
                    //    Log.i(TAG, "Scheduling update: " + existingUri);
                    batch.add(ContentProviderOperation.newUpdate(existingUri)
                            .withValue(YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_NAME, match.name)
                            .withValue(YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_IMAGE, match.image)
                            .withValue(YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_FIREBASE_ROOM_ID, match.firebaseRoomId)
                            .withValue(YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_FIREBASE_USER_ID, match.firebaseUserId)
                            .withValue(YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_IS_YOAPP_USER, match.yoappuser)
                            .withValue(YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_VOX_USER_NAME, match.voxUserName)
                            .withValue(YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_COUNTRY_CODE, match.countryCode)
                            .build());
                    syncResult.stats.numUpdates++;
                }
            } else {
                // Entry doesn't exist. Remove it from the database.
                Uri deleteUri = YoAppContactContract.YoAppContactsEntry.CONTENT_URI.buildUpon()
                        .appendPath(Integer.toString(id)).build();
                // Log.i(TAG, "Scheduling delete: " + deleteUri);
                batch.add(ContentProviderOperation.newDelete(deleteUri).build());
                syncResult.stats.numDeletes++;
            }
        }
        c.close();

        // Add new items
        for (Entry e : entryMap.values()) {
            batch.add(ContentProviderOperation.newInsert(YoAppContactContract.YoAppContactsEntry.CONTENT_URI)
                    .withValue(YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_USER_ID, e.entryId)

                    .withValue(YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_NAME, e.name)
                    .withValue(YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_PHONE_NUMBER, e.phone)
                    .withValue(YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_IMAGE, e.image)
                    .withValue(YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_FIREBASE_ROOM_ID, e.firebaseRoomId)
                    .withValue(YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_FIREBASE_USER_ID, e.firebaseUserId)
                    .withValue(YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_IS_YOAPP_USER, e.yoappuser)
                    .withValue(YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_VOX_USER_NAME, e.voxUserName)
                    .withValue(YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_COUNTRY_CODE, e.countryCode)
                    .build());
            syncResult.stats.numInserts++;
        }
        mContentResolver.applyBatch(YoAppContactContract.CONTENT_AUTHORITY, batch);
        mContentResolver.notifyChange(
                YoAppContactContract.YoAppContactsEntry.CONTENT_URI, // URI where data was modified
                null,                           // No local observer
                false);                         // IMPORTANT: Do not sync to network
        // This Sample doesn't support uploads, but if *your* code does, make sure you set
        // syncToNetwork=false in the line above to prevent duplicate syncs.
    }

    private static List<Entry> prepareEntries(List<Contact> contacts) {
        List<Entry> list = new ArrayList<>();
        for (Contact contact : contacts) {
            Entry entry = new Entry(contact.getId(),
                    contact.getName(), contact.getPhoneNo(), contact.getImage(), contact.getFirebaseRoomId(), contact.isYoAppUser(), contact.getFirebaseUserId(), contact.getNexgieUserName(), contact.getCountryCode());
            list.add(entry);
        }
        return list;
    }

    /**
     * This class represents a single entry (post) in the XML feed.
     * <p/>
     * <p>It includes the data members "title," "link," and "summary."
     */
    public static class Entry {
        public final String entryId;
        public final String name;
        public final String phone;
        public final String image;
        public final boolean yoappuser;
        public final String firebaseRoomId;
        public final String firebaseUserId;
        public final String voxUserName;
        public final String countryCode;

        Entry(String id, String name, String phone, String image, String firebaseRoomId, boolean yoappuser, String firebaseUserId, String voxUserName, String countryCode) {
            this.entryId = id;
            this.name = name;
            this.phone = phone;
            this.image = image;
            this.yoappuser = yoappuser;
            this.firebaseRoomId = firebaseRoomId;
            this.firebaseUserId = firebaseUserId;
            this.voxUserName = voxUserName;
            this.countryCode = countryCode;

        }
    }

}

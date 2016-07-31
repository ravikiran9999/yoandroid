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
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.api.YoApi;
import com.yo.android.di.Injector;
import com.yo.android.model.Contact;
import com.yo.android.provider.YoAppContactContract;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

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
    @Named("login")
    PreferenceEndPoint loginPrefs;
    /**
     * Content resolver, for performing database operations.
     */
    private final ContentResolver mContentResolver;

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
    };

    // Constants representing column positions from PROJECTION.
    public static final int COLUMN_ID = 0;
    public static final int COLUMN_ENTRY_ID = 1;
    public static final int COLUMN_NAME = 2;
    public static final int COLUMN_PHONE = 3;
    public static final int COLUMN_IMAGE = 4;
    public static final int COLUMN_FIREBASE_ROOM_ID = 5;
    public static final int COLUMN_YO_USER = 6;

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public YoContactsSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
        Injector.obtain(context.getApplicationContext()).inject(this);
    }

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public YoContactsSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
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
                              ContentProviderClient provider, SyncResult syncResult) {
        Log.i(TAG, "Beginning network synchronization");
        try {
            String access = loginPrefs.getStringPreference(YoApi.ACCESS_TOKEN);
            if (TextUtils.isEmpty(access)) {
                return;
            }
            Response<List<Contact>> list = mYoService.getContacts(access).execute();
            if (list.isSuccessful()) {
                List<Contact> contacts = list.body();
                updateLocalFeedData(contacts, syncResult);
            }

        } catch (IOException e) {
            syncResult.stats.numIoExceptions++;
            return;
        } catch (RemoteException | OperationApplicationException e) {
            Log.e(TAG, "Error updating database: " + e.toString());
            syncResult.databaseError = true;
            return;
        }
        Log.i(TAG, "Network synchronization complete");
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
    public void updateLocalFeedData(List<Contact> contacts, final SyncResult syncResult) throws RemoteException, OperationApplicationException {
        final ContentResolver contentResolver = getContext().getContentResolver();

        Log.i(TAG, "Parsing stream as Atom feed");
        final List<Entry> entries = prepareEntries(contacts);
        Log.i(TAG, "Parsing complete. Found " + entries.size() + " entries");


        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

        // Build hash table of incoming entries
        HashMap<String, Entry> entryMap = new HashMap<String, Entry>();
        for (Entry e : entries) {
            entryMap.put(e.entryId, e);
        }

        // Get list of all items
        Log.i(TAG, "Fetching local entries for merge");
        Uri uri = YoAppContactContract.YoAppContactsEntry.CONTENT_URI; // Get all entries
        Cursor c = contentResolver.query(uri, PROJECTION, null, null, null);
        assert c != null;
        Log.i(TAG, "Found " + c.getCount() + " local entries. Computing merge solution...");

        // Find stale data
        int id;
        String entryId;
        String name;
        String phone;
        String image;
        String roomId;
        boolean yoAppUser;
        while (c.moveToNext()) {
            syncResult.stats.numEntries++;
            id = c.getInt(COLUMN_ID);
            entryId = c.getString(COLUMN_ENTRY_ID);
            name = c.getString(COLUMN_NAME);
            phone = c.getString(COLUMN_PHONE);
            image = c.getString(COLUMN_IMAGE);
            roomId = c.getString(COLUMN_FIREBASE_ROOM_ID);
            yoAppUser = c.getInt(COLUMN_YO_USER) != 0;
            Entry match = entryMap.get(entryId);
            if (match != null) {
                // Entry exists. Remove from entry map to prevent insert later.
                entryMap.remove(entryId);
                // Check to see if the entry needs to be updated
                Uri existingUri = YoAppContactContract.YoAppContactsEntry.CONTENT_URI.buildUpon()
                        .appendPath(Integer.toString(id)).build();
                if ((match.entryId != null && !match.entryId.equals(entryId)) ||
                        (match.phone != null && !match.phone.equals(phone))
                        ) {
                    // Update existing record
                    Log.i(TAG, "Scheduling update: " + existingUri);
                    batch.add(ContentProviderOperation.newUpdate(existingUri)
                            .withValue(YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_NAME, name)
                            .withValue(YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_IMAGE, image)
                            .withValue(YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_FIREBASE_ROOM_ID, roomId)
                            .withValue(YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_IS_YOAPP_USER, yoAppUser)
                            .build());
                    syncResult.stats.numUpdates++;
                } else {
                    Log.i(TAG, "No action: " + existingUri);
                }
            } else {
                // Entry doesn't exist. Remove it from the database.
                Uri deleteUri = YoAppContactContract.YoAppContactsEntry.CONTENT_URI.buildUpon()
                        .appendPath(Integer.toString(id)).build();
                Log.i(TAG, "Scheduling delete: " + deleteUri);
                batch.add(ContentProviderOperation.newDelete(deleteUri).build());
                syncResult.stats.numDeletes++;
            }
        }
        c.close();

        // Add new items
        for (Entry e : entryMap.values()) {
            Log.i(TAG, "Scheduling insert: entry_id=" + e.entryId);
            batch.add(ContentProviderOperation.newInsert(YoAppContactContract.YoAppContactsEntry.CONTENT_URI)
                    .withValue(YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_USER_ID, e.entryId)
                    .withValue(YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_NAME, e.name)
                    .withValue(YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_PHONE_NUMBER, e.phone)
                    .withValue(YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_IMAGE, e.image)
                    .withValue(YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_FIREBASE_ROOM_ID, e.firebaseRoomId)
                    .withValue(YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_IS_YOAPP_USER, e.yoappuser)
                    .build());
            syncResult.stats.numInserts++;
        }
        Log.i(TAG, "Merge solution ready. Applying batch update");
        mContentResolver.applyBatch(YoAppContactContract.CONTENT_AUTHORITY, batch);
        mContentResolver.notifyChange(
                YoAppContactContract.YoAppContactsEntry.CONTENT_URI, // URI where data was modified
                null,                           // No local observer
                false);                         // IMPORTANT: Do not sync to network
        // This sample doesn't support uploads, but if *your* code does, make sure you set
        // syncToNetwork=false in the line above to prevent duplicate syncs.
    }

    private List<Entry> prepareEntries(List<Contact> contacts) {
        List<Entry> list = new ArrayList<>();
        for (Contact contact : contacts) {
            Entry entry = new Entry(contact.getId(),
                    contact.getName(), contact.getPhoneNo(), contact.getImage(), contact.getFirebaseRoomId(), contact.getYoAppUser());
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

        Entry(String id, String name, String phone, String image, String firebaseRoomId, boolean yoappuser) {
            this.entryId = id;
            this.name = name;
            this.phone = phone;
            this.image = image;
            this.yoappuser = yoappuser;
            this.firebaseRoomId = firebaseRoomId;
        }
    }

}

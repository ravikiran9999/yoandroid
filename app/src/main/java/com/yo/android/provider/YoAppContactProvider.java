package com.yo.android.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.yo.android.calllogs.CallLog;
import com.yo.android.calllogs.CallLogContract;

public class YoAppContactProvider extends ContentProvider {
    private YoAppContactDatabase mDatabaseHelper;

    /**
     * Content authority for this provider.
     */
    private static final String AUTHORITY = YoAppContactContract.CONTENT_AUTHORITY;
    /**
     * URI ID for route: /contacts
     */
    public static final int CONTACTS_ENTRIES = 1;

    /**
     * URI ID for route: /contacts/{ID}
     */
    public static final int ROUTE_CONTACTS_ENTRIES_ID = 2;
    public static final int CALL_LOGS = 3;


    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY, "contacts", CONTACTS_ENTRIES);
        sUriMatcher.addURI(AUTHORITY, "contacts/*", ROUTE_CONTACTS_ENTRIES_ID);
        sUriMatcher.addURI(AUTHORITY, "callLogs", CALL_LOGS);
    }

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new YoAppContactDatabase(getContext());
        return true;
    }

    /**
     * Determine the mime type for entries returned by a given URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CONTACTS_ENTRIES:
                return YoAppContactContract.YoAppContactsEntry.CONTENT_TYPE;
            case ROUTE_CONTACTS_ENTRIES_ID:
                return YoAppContactContract.YoAppContactsEntry.CONTENT_ITEM_TYPE;
            case CALL_LOGS:
                return CallLogContract.CALL_LOG_CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Perform a database query by URI.
     * <p/>
     * <p>Currently supports returning all entries (/entries) and individual entries by ID
     * (/entries/{ID}).
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        SelectionBuilder builder = new SelectionBuilder();
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch) {
            case CALL_LOGS:
                builder.table(CallLogContract.TABLE_NAME)
                        .where(selection, selectionArgs);
                return builder.query(db,projection,sortOrder);
            case ROUTE_CONTACTS_ENTRIES_ID:
                // Return a single entry, by ID.
                String id = uri.getLastPathSegment();
                builder.where(YoAppContactContract.YoAppContactsEntry._ID + "=?", id);
            case CONTACTS_ENTRIES:
                // Return all known entries.
                builder.table(YoAppContactContract.YoAppContactsEntry.TABLE_NAME)
                        .where(selection, selectionArgs);
                Cursor c = builder.query(db, projection, sortOrder);
                // Note: Notification URI must be manually set here for loaders to correctly
                // register ContentObservers.
                Context ctx = getContext();
                assert ctx != null;
                c.setNotificationUri(ctx.getContentResolver(), uri);
                return c;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Insert a new entry into the database.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        assert db != null;
        final int match = sUriMatcher.match(uri);
        Uri result;
        switch (match) {
            case CONTACTS_ENTRIES:
                long id = db.insertOrThrow(YoAppContactContract.YoAppContactsEntry.TABLE_NAME, null, values);
                result = Uri.parse(YoAppContactContract.YoAppContactsEntry.CONTENT_URI + "/" + id);
                break;
            case CALL_LOGS:
                id = db.insertOrThrow(CallLogContract.TABLE_NAME, null, values);
                result = Uri.parse(CallLogContract.CALL_LOG_CONTENT_TYPE + "/" + id);
                break;
            case ROUTE_CONTACTS_ENTRIES_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return result;
    }

    /**
     * Delete an entry by database by URI.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        switch (match) {
            case CONTACTS_ENTRIES:
                count = builder.table(YoAppContactContract.YoAppContactsEntry.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case CALL_LOGS:
                count = builder.table(CallLogContract.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_CONTACTS_ENTRIES_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(YoAppContactContract.YoAppContactsEntry.TABLE_NAME)
                        .where(YoAppContactContract.YoAppContactsEntry._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    /**
     * Update an etry in the database by URI.
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        switch (match) {
            case CONTACTS_ENTRIES:
                count = builder.table(YoAppContactContract.YoAppContactsEntry.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case CALL_LOGS:
                count = builder.table(CallLogContract.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_CONTACTS_ENTRIES_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(YoAppContactContract.YoAppContactsEntry.TABLE_NAME)
                        .where(YoAppContactContract.YoAppContactsEntry._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    /**
     * SQLite backend for @{link FeedProvider}.
     * <p/>
     * Provides access to an disk-backed, SQLite datastore which is utilized by FeedProvider. This
     * database should never be accessed by other parts of the application directly.
     */
    static class YoAppContactDatabase extends SQLiteOpenHelper {
        /**
         * Schema version.
         */
        public static final int DATABASE_VERSION = 1;
        /**
         * Filename for SQLite file.
         */
        public static final String DATABASE_NAME = "yoappcontacts.db";

        private static final String TYPE_TEXT = " TEXT";
        private static final String TYPE_BOOLEAN = " BOOLEAN";
        private static final String TYPE_INTEGER = " INTEGER";
        private static final String COMMA_SEP = ",";
        /**
         * SQL statement to create "entry" table.
         */
        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + YoAppContactContract.YoAppContactsEntry.TABLE_NAME + " (" +
                        YoAppContactContract.YoAppContactsEntry._ID + " INTEGER PRIMARY KEY," +
                        YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_USER_ID + TYPE_TEXT + COMMA_SEP +
                        YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_NAME + TYPE_TEXT + COMMA_SEP +
                        YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_PHONE_NUMBER + TYPE_TEXT + COMMA_SEP +
                        YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_IMAGE + TYPE_TEXT + COMMA_SEP +
                        YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_FIREBASE_ROOM_ID + TYPE_TEXT + COMMA_SEP +
                        YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_IS_YOAPP_USER + TYPE_INTEGER + COMMA_SEP +
                        YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_VOX_USER_NAME + TYPE_TEXT  +COMMA_SEP +
                        YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_COUNTRY_CODE + TYPE_TEXT  +
                        ")";
        private static final String SQL_CREATE_CALL_LOG =
                "CREATE TABLE " + CallLogContract.TABLE_NAME + " (" +
                        YoAppContactContract.YoAppContactsEntry._ID + " INTEGER PRIMARY KEY," +
                        CallLog.Calls.NUMBER + TYPE_TEXT + COMMA_SEP +
                        CallLog.Calls.TYPE + TYPE_TEXT + COMMA_SEP +
                        CallLog.Calls.DATE + TYPE_TEXT + COMMA_SEP +
                        CallLog.Calls.DURATION + TYPE_TEXT + COMMA_SEP +
                        CallLog.Calls.CACHED_NAME + TYPE_TEXT + COMMA_SEP +
                        CallLog.Calls.CACHED_NUMBER_LABEL + TYPE_TEXT + COMMA_SEP +
                        CallLog.Calls.CACHED_NUMBER_TYPE + TYPE_TEXT + COMMA_SEP +
                        CallLog.Calls.NEW + TYPE_INTEGER + COMMA_SEP +
                        CallLog.Calls.CALLTYPE + TYPE_INTEGER + COMMA_SEP +
                        CallLog.Calls.APP_OR_PSTN + TYPE_INTEGER +
                        ")";

        /**
         * SQL statement to drop "entry" table.
         */
        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + YoAppContactContract.YoAppContactsEntry.TABLE_NAME;
        private static final String SQL_DELETE_CALL_LOGS =
                "DROP TABLE IF EXISTS " + YoAppContactContract.YoAppContactsEntry.TABLE_NAME;


        public YoAppContactDatabase(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
            db.execSQL(SQL_CREATE_CALL_LOG);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            db.execSQL(SQL_DELETE_CALL_LOGS);
            onCreate(db);
        }
    }
}

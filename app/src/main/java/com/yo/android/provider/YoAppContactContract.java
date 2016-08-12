package com.yo.android.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Field and table name constants for
 */
public class YoAppContactContract {

    /**
     * Content provider authority.
     */
    public static final String CONTENT_AUTHORITY = "com.yo.android.provider.contacts";

    /**
     * Base URI. (content://com.example.android.network.sync.basicsyncadapter)
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Path component for "entry"-type resources..
     */
    private static final String PATH_ENTRIES = "contacts";

    /**
     * Columns supported by "entries" records.
     */
    public static class YoAppContactsEntry implements BaseColumns {
        /**
         * MIME type for lists of entries.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.yoappcontacts.entries";
        /**
         * MIME type for individual entries.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.yoappcontacts.entry";

        /**
         * Fully qualified URI for "entry" resources.
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ENTRIES).build();

        /**
         * Table name where records are stored for "entry" resources.
         */
        public static final String TABLE_NAME = "contacts";
        //
        public static final String COLUMN_NAME_USER_ID = "user_id";

        public static final String COLUMN_NAME_NAME = "name";

        public static final String COLUMN_NAME_IMAGE = "image";

        public static final String COLUMN_NAME_PHONE_NUMBER = "phone_no";

        public static final String COLUMN_NAME_IS_YOAPP_USER = "is_yoapp_user";

        public static final String COLUMN_NAME_FIREBASE_ROOM_ID = "firebase_room_id";

    }

    private YoAppContactContract() {
    }

}
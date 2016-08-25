package com.yo.android.calllogs;

import android.content.ContentResolver;

/**
 * Created by rajesh on 23/8/16.
 */
public class CallLogContract {
    /**
     * MIME type for call logs.
     */
    public static final String CALL_LOG_CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.yoappcontacts.calllogs";

    /**
     * Table name where records are stored for "entry" resources.
     */
    public static final String TABLE_NAME = "calllogs";


    private CallLogContract() {

    }

}

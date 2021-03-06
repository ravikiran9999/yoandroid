

package com.yo.android.calllogs;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWindow;
import android.net.Uri;
import android.provider.BaseColumns;

import com.yo.android.BuildConfig;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.helpers.Helper;
import com.yo.android.model.dialer.CallLogsResult;
import com.yo.android.provider.YoAppContactContract;
import com.yo.android.util.DateUtil;
import com.yo.android.util.TimeZoneUtils;
import com.yo.android.util.Util;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
 * The CallLog provider contains information about placed and received calls.
 */
public class CallLog {
    @Inject
    ContactsSyncManager mContactsSyncManager;

    public static final String AUTHORITY = YoAppContactContract.CONTENT_AUTHORITY;


    /**
     * The content:// style URL for this provider
     */
    public static final Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY);

    /**
     * Contains the recent calls.
     */
    public static class Calls implements BaseColumns {

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
                Uri.parse("content://" + AUTHORITY + "/callLogs");


        /**
         * An optional URI parameter which instructs the provider to allow the operation to be
         * applied to voicemail records as well.
         * <p/>
         * TYPE: Boolean
         * <p/>
         * Using this parameter with a value of {@code true} will result in a security error if the
         * calling package does not have appropriate permissions to access voicemails.
         *
         * @hide
         */
        public static final String ALLOW_VOICEMAILS_PARAM_KEY = "allow_voicemails";

        /**
         * Content uri with {@link #ALLOW_VOICEMAILS_PARAM_KEY} set. This can directly be used to
         * access call log entries that includes voicemail records.
         *
         * @hide
         */
        public static final Uri CONTENT_URI_WITH_VOICEMAIL = CONTENT_URI.buildUpon()
                .appendQueryParameter(ALLOW_VOICEMAILS_PARAM_KEY, "true")
                .build();

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "date DESC";


        /**
         * The type of the call (incoming, outgoing or missed).
         * <P>Type: INTEGER (int)</P>
         */
        public static final String TYPE = "type";

        /**
         * Call log type for incoming calls.
         */
        public static final int INCOMING_TYPE = 1;
        /**
         * Call log type for outgoing calls.
         */
        public static final int OUTGOING_TYPE = 2;
        /**
         * Call log type for missed calls.
         */
        public static final int MISSED_TYPE = 3;

        /**
         * App to app call.
         */
        public static final int APP_TO_APP_CALL = 1;
        /**
         * App to app call.
         */
        public static final int APP_TO_PSTN_CALL = 2;
        /**
         * Call log type for voicemails.
         *
         * @hide
         */
        public static final int VOICEMAIL_TYPE = 4;

        /**
         * The phone number as the user entered it.
         * <P>Type: TEXT</P>
         */
        public static final String NUMBER = "number";

        /**
         * The ISO 3166-1 two letters country code of the country where the
         * user received or made the call.
         * <p/>
         * Type: TEXT
         * </P>
         *
         * @hide
         */
        public static final String COUNTRY_ISO = "countryiso";

        /**
         * The date the call occured, in milliseconds since the epoch
         * <P>Type: INTEGER (long)</P>
         */
        public static final String DATE = "date";

        /**
         * The duration of the call in seconds
         * <P>Type: INTEGER (long)</P>
         */
        public static final String DURATION = "duration";

        /**
         * Whether or not the call has been acknowledged
         * <P>Type: INTEGER (boolean)</P>
         */
        public static final String NEW = "new";

        public static final String CALLTYPE = "calltype";

        public static final String APP_OR_PSTN = "app_or_pstn";


        /**
         * The cached name associated with the phone number, if it exists.
         * This value is not guaranteed to be current, if the contact information
         * associated with this number has changed.
         * <P>Type: TEXT</P>
         */
        public static final String CACHED_NAME = "name";

        /**
         * The cached number type (Home, Work, etc) associated with the
         * phone number, if it exists.
         * This value is not guaranteed to be current, if the contact information
         * associated with this number has changed.
         * <P>Type: INTEGER</P>
         */
        public static final String CACHED_NUMBER_TYPE = "numbertype";

        /**
         * The cached number label, for a custom number type, associated with the
         * phone number, if it exists.
         * This value is not guaranteed to be current, if the contact information
         * associated with this number has changed.
         * <P>Type: TEXT</P>
         */
        public static final String CACHED_NUMBER_LABEL = "numberlabel";

        /**
         * URI of the voicemail entry. Populated only for {@link #VOICEMAIL_TYPE}.
         * <P>Type: TEXT</P>
         *
         * @hide
         */
        public static final String VOICEMAIL_URI = "voicemail_uri";

        /**
         * Whether this item has been read or otherwise consumed by the user.
         * <p/>
         * Unlike the {@link #NEW} field, which requires the user to have acknowledged the
         * existence of the entry, this implies the user has interacted with the entry.
         * <P>Type: INTEGER (boolean)</P>
         */
        public static final String IS_READ = "is_read";

        /**
         * A geocoded location for the number associated with this call.
         * <p/>
         * The string represents a city, state, or country associated with the number.
         * <P>Type: TEXT</P>
         *
         * @hide
         */
        public static final String GEOCODED_LOCATION = "geocoded_location";

        /**
         * The cached URI to look up the contact associated with the phone number, if it exists.
         * This value is not guaranteed to be current, if the contact information
         * associated with this number has changed.
         * <P>Type: TEXT</P>
         *
         * @hide
         */
        public static final String CACHED_LOOKUP_URI = "lookup_uri";

        /**
         * The cached phone number of the contact which matches this entry, if it exists.
         * This value is not guaranteed to be current, if the contact information
         * associated with this number has changed.
         * <P>Type: TEXT</P>
         *
         * @hide
         */
        public static final String CACHED_MATCHED_NUMBER = "matched_number";

        /**
         * The cached normalized version of the phone number, if it exists.
         * This value is not guaranteed to be current, if the contact information
         * associated with this number has changed.
         * <P>Type: TEXT</P>
         *
         * @hide
         */
        public static final String CACHED_NORMALIZED_NUMBER = "normalized_number";

        /**
         * The cached photo id of the picture associated with the phone number, if it exists.
         * This value is not guaranteed to be current, if the contact information
         * associated with this number has changed.
         * <P>Type: INTEGER (long)</P>
         *
         * @hide
         */
        public static final String CACHED_PHOTO_ID = "photo_id";

        /**
         * The cached formatted phone number.
         * This value is not guaranteed to be present.
         * <P>Type: TEXT</P>
         *
         * @hide
         */
        public static final String CACHED_FORMATTED_NUMBER = "formatted_number";

        /**
         * Adds a call to the call log.
         *
         * @param ci       the CallerInfo object to get the target contact from.  Can be null
         *                 if the contact is unknown.
         * @param context  the context used to get the ContentResolver
         * @param number   the phone number to be added to the calls db
         *                 S1Q
         * @param callType enumerated values for "incoming", "outgoing", or "missed"
         * @param start    time stamp for the call in milliseconds
         * @param duration call duration in seconds
         *                 <p/>
         *                 {@hide}
         */
        public static Uri addCall(CallerInfo ci, Context context, String number,
                                  int callType, long start, long duration, int pstnorapp) {
            final ContentResolver resolver = context.getContentResolver();


            ContentValues values = new ContentValues(5);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Date currentDate = null;
            if(start == 0) {
                currentDate = new Date(TimeZoneUtils.getTime(dateFormat));
            } else {
                currentDate = new Date(start);
            }

            values.put(NUMBER, number);


            values.put(TYPE, Integer.valueOf(callType));
            values.put(DATE, dateFormat.format(currentDate));
            values.put(DURATION, duration + "");
            values.put(NEW, Integer.valueOf(1));
            values.put(CALLTYPE, callType);
            values.put(APP_OR_PSTN, pstnorapp);
            if (number != null && number.contains(BuildConfig.RELEASE_USER_TYPE)) {
                values.put(APP_OR_PSTN, Calls.APP_TO_APP_CALL);
            }
            if (ci != null) {
                if (ci.name != null) {
                    values.put(CACHED_NAME, ci.name);
                }
                values.put(CACHED_NUMBER_TYPE, ci.numberType);
                values.put(CACHED_NUMBER_LABEL, ci.numberLabel);
            }
            Uri result = resolver.insert(CONTENT_URI, values);

            //removeExpiredEntries(context);

            return result;
        }

        /**
         * Query the call log database for the last dialed number.
         *
         * @param context Used to get the content resolver.
         * @return The last phone number dialed (outgoing) or an empty
         * string if none exist yet.
         */
        public static String getLastOutgoingCall(Context context) {
            final ContentResolver resolver = context.getContentResolver();
            Cursor c = null;
            try {
                c = resolver.query(
                        CONTENT_URI,
                        new String[]{NUMBER},
                        TYPE + " = " + OUTGOING_TYPE,
                        null,
                        DEFAULT_SORT_ORDER + " LIMIT 1");
                if (c == null || !c.moveToFirst()) {
                    return "";
                }
                return c.getString(0);
            } finally {
                if (c != null) c.close();
            }
        }

        /**
         * Query the call log database for the last dialed number.
         *
         * @param context Used to get the content resolver.
         * @return The last phone number dialed (outgoing) or an empty
         * string if none exist yet.
         */
        public static ArrayList<Map.Entry<String, List<CallLogsResult>>> getCallLog(Context context) {

            ArrayList<Map.Entry<String, List<CallLogsResult>>> callerInfos = new ArrayList<Map.Entry<String, List<CallLogsResult>>>();
            LinkedHashMap<String, List<CallLogsResult>> hashMap = new LinkedHashMap<String, List<CallLogsResult>>();
            Cursor c = null;
            if (context != null) {
                final ContentResolver resolver = context.getContentResolver();
                c = resolver.query(
                        CONTENT_URI,
                        null,
                        null,
                        null,
                        DEFAULT_SORT_ORDER);
            }
            try {
                if (c == null || !c.moveToFirst()) {
                    return callerInfos;
                } else {
                    do {
                        CallLogsResult info = new CallLogsResult();
                        String voxuser = c.getString(c.getColumnIndex(Calls.NUMBER));
                        if (voxuser == null) {
                            continue;
                        }
                        String phoneName = Helper.getContactName(context, voxuser);
                        info.setDialnumber(voxuser);
                        info.setCallType(c.getInt(c.getColumnIndex(Calls.CALLTYPE)));
                        info.setStime(c.getString(c.getColumnIndex(Calls.DATE)));
                        String tempDate = DateUtil.getDate(c.getString(c.getColumnIndex(Calls.DATE)));
                        info.setDestination_name(c.getString(c.getColumnIndex(Calls.CACHED_NAME)));
                        info.setAppOrPstn(c.getInt(c.getColumnIndex(Calls.APP_OR_PSTN)));
                        if (phoneName != null && !phoneName.equalsIgnoreCase(voxuser)) {
                            info.setDestination_name(phoneName);
                        }
                        String duration = c.getString(c.getColumnIndex(Calls.DURATION));
                        info.setDuration(duration);
                        info.setImage(getImagePath(context, voxuser));

                        if (!hashMap.containsKey(voxuser + tempDate)) {
                            List<CallLogsResult> list = new ArrayList<CallLogsResult>();
                            list.add(info);
                            hashMap.put(voxuser + tempDate, list);
                        } else {
                            hashMap.get(voxuser + tempDate).add(info);
                        }
                    } while (c.moveToNext());
                    callerInfos = new ArrayList(hashMap.entrySet());
                    return callerInfos;

                }
            } finally {
                if (c != null && !c.isClosed()) c.close();
            }
        }

        /**
         * Query the call log database for the last dialed number.
         *
         * @param context Used to get the content resolver.
         * @return The last phone number dialed (outgoing) or an empty
         * string if none exist yet.
         */
        public static ArrayList<Map.Entry<String, List<CallLogsResult>>> getPSTNCallLog(Context context) {
            ArrayList<Map.Entry<String, List<CallLogsResult>>> callerInfos = new ArrayList<Map.Entry<String, List<CallLogsResult>>>();
            LinkedHashMap<String, List<CallLogsResult>> hashMap = new LinkedHashMap<String, List<CallLogsResult>>();
            Cursor c = null;
            if (context != null) {
                final ContentResolver resolver = context.getContentResolver();
                c = resolver.query(
                        CONTENT_URI,
                        null,
                        Calls.APP_OR_PSTN + " = " + APP_TO_PSTN_CALL,
                        null,
                        DEFAULT_SORT_ORDER);
            }
            try {
                if (c == null || !c.moveToFirst()) {
                    return callerInfos;
                } else {
                    do {
                        CallLogsResult info = new CallLogsResult();
                        String voxuser = c.getString(c.getColumnIndex(Calls.NUMBER));
                        if (voxuser == null) {
                            continue;
                        }
                        String phoneName = Helper.getContactName(context, voxuser);

                        info.setDialnumber(voxuser);
                        info.setCallType(c.getInt(c.getColumnIndex(Calls.CALLTYPE)));
                        info.setStime(c.getString(c.getColumnIndex(Calls.DATE)));
                        info.setDestination_name(c.getString(c.getColumnIndex(Calls.CACHED_NAME)));
                        info.setAppOrPstn(c.getInt(c.getColumnIndex(Calls.APP_OR_PSTN)));
                        if (phoneName != null && !phoneName.equalsIgnoreCase(voxuser)) {
                            info.setDestination_name(phoneName);
                        }
                        String duration = c.getString(c.getColumnIndex(Calls.DURATION));
                        info.setDuration(duration);
                        info.setImage(getImagePath(context, voxuser));
                        if (!hashMap.containsKey(voxuser)) {
                            List<CallLogsResult> list = new ArrayList<CallLogsResult>();
                            list.add(info);
                            hashMap.put(voxuser, list);
                        } else {
                            hashMap.get(voxuser).add(info);
                        }
                    } while (c.moveToNext());
                    callerInfos = new ArrayList(hashMap.entrySet());

                    return callerInfos;

                }
            } finally {
                if (c != null && !c.isClosed()) c.close();
            }
        }

        /**
         * Query the call log database for the last dialed number.
         *
         * @param context Used to get the content resolver.
         * @return The last phone number dialed (outgoing) or an empty
         * string if none exist yet.
         */
        public static ArrayList<Map.Entry<String, List<CallLogsResult>>> getAppToAppCallLog(Context context) {
            Cursor c = null;
            ArrayList<Map.Entry<String, List<CallLogsResult>>> callerInfos = new ArrayList<>();
            LinkedHashMap<String, List<CallLogsResult>> hashMap = new LinkedHashMap<String, List<CallLogsResult>>();

            if (context != null) {
                final ContentResolver resolver = context.getContentResolver();
                c = resolver.query(
                        CONTENT_URI,
                        null,
                        Calls.APP_OR_PSTN + " = " + APP_TO_APP_CALL,
                        null,
                        DEFAULT_SORT_ORDER);
            }
            try {
                if (c == null || !c.moveToFirst()) {
                    return callerInfos;
                } else {
                    do {

                        CallLogsResult info = new CallLogsResult();
                        String voxuser = c.getString(c.getColumnIndex(Calls.NUMBER));
                        if (voxuser == null) {
                            continue;
                        }
                        info.setDialnumber(voxuser);
                        info.setCallType(c.getInt(c.getColumnIndex(Calls.CALLTYPE)));
                        String date = c.getString(c.getColumnIndex(Calls.DATE));
                        info.setStime(date);
                        String duration = c.getString(c.getColumnIndex(Calls.DURATION));
                        info.setDuration(duration);
                        String tempDate = DateUtil.getDate(date);
                        info.setDestination_name(c.getString(c.getColumnIndex(Calls.CACHED_NAME)));
                        info.setAppOrPstn(c.getInt(c.getColumnIndex(Calls.APP_OR_PSTN)));
                        info.setImage(getImagePath(context, voxuser));

                        if (!hashMap.containsKey(voxuser + tempDate)) {
                            List<CallLogsResult> list = new ArrayList<CallLogsResult>();
                            list.add(info);
                            hashMap.put(voxuser + tempDate, list);

                        } else {
                            hashMap.get(voxuser + tempDate).add(info);
                        }
                    } while (c.moveToNext());
                    callerInfos = new ArrayList(hashMap.entrySet());
                    return callerInfos;

                }
            } finally {
                if (c != null && !c.isClosed()) c.close();
            }
        }

        private static void removeExpiredEntries(Context context) {
            final ContentResolver resolver = context.getContentResolver();
            resolver.delete(CONTENT_URI, "_id IN " +
                    "(SELECT _id FROM " + CallLogContract.TABLE_NAME + " ORDER BY " + DEFAULT_SORT_ORDER
                    + " LIMIT -1 OFFSET 500)", null);
        }

        public static void deleteCallLogByDate(Context context, String date, String number) {
            final ContentResolver resolver = context.getContentResolver();
            String selection = "DATE(date) = DATE('" + date + "') and " + NUMBER + " = '" + number + "'";
            resolver.delete(CONTENT_URI, selection, null);
        }

        public static void clearCallHistory(Context context) {
            final ContentResolver resolver = context.getContentResolver();
            resolver.delete(CONTENT_URI, null, null);
        }

        public static String getImagePath(Context context, String voxUserName) {
            final ContentResolver resolver = context.getContentResolver();
            Cursor imageCursor = null;
            try {
                imageCursor = resolver.query(
                        YoAppContactContract.YoAppContactsEntry.CONTENT_URI,
                        new String[]{YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_IMAGE},
                        YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_VOX_USER_NAME + " = '" + voxUserName + "'",
                        null,
                        null);
                if (imageCursor != null && imageCursor.moveToFirst()) {
                    return imageCursor.getString(0);
                }
                return null;
            } finally {
                if (imageCursor != null && !imageCursor.isClosed()) {
                    imageCursor.close();
                }
            }
        }
    }
}

package com.yo.android.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.orion.android.common.logger.Log;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.api.YoApi;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.sync.SyncUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Created by Ramesh on 3/8/16.
 */
@Singleton
public class ContactSyncHelper {
    //    1. Store All Contacts in Map
//    2. Upload them to Server
//    3. update contacts in local DB once fetched data from remote
//    4. Register to contact observer
//    5. Follow the step 1 to 3 if any contact change happens
    private static final String TAG = "ContactSyncHelper";
    final Context context;
    final Log mLog;
    private String lastContactsVersions = "";
    ContactsSyncManager contactsSyncManager;
    ContentObserver contentObserver;
    HashMap<Integer, Contact> contactsBook = new HashMap<>();
    HashMap<Integer, Contact> cacheContacts = new HashMap<>();
    public static volatile DispatchQueue globalQueue = new DispatchQueue("globalQueue");
    public int syncMode = NONE;
    public static final int NONE = 0;
    public static final int INIT = 1;
    public static final int PROCESSING = 2;
    public static final int FINISHED = 3;
    PreferenceEndPoint loginPrefs;

    @Inject
    public ContactSyncHelper(Context context, Log log, ContactsSyncManager contactsSyncManager, @Named("login") PreferenceEndPoint loginPrefs) {
        this.context = context;
        mLog = log;
        this.contactsSyncManager = contactsSyncManager;
        contentObserver = new MyContentObserver();
        context.getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, contentObserver);
        this.loginPrefs = loginPrefs;
    }

    //Call this method after validate OTP,
    public void init() {
        context.getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, contentObserver);
    }

    //Clean this
    public void clean() {
//        context.getContentResolver().unregisterContentObserver(contentObserver);
        globalQueue.cleanupQueue();
        contactsBook.clear();
        cacheContacts.clear();
        syncMode = NONE;
    }

    private class MyContentObserver extends ContentObserver {

        public MyContentObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            //Allow this if user is logged only.
            String access = loginPrefs.getStringPreference(YoApi.ACCESS_TOKEN);
            if (!TextUtils.isEmpty(access)) {
                checkContacts();
            }
        }

    }

    public void checkContacts() {
        globalQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                if (cacheContacts.isEmpty()) {
                    setSyncMode(INIT);
                } else {
                    setSyncMode(PROCESSING);
                }
                mLog.i(TAG, "detected contacts change>>>start");
                performContactSync(cacheContacts);
                mLog.i(TAG, "detected contacts change>>>End");
                setSyncMode(FINISHED);
            }
        });
    }

    private void setSyncMode(int mode) {
        syncMode = mode;
    }

    public int getSyncMode() {
        return syncMode;
    }

    private void performContactSync(HashMap<Integer, Contact> cachePhoneBookHashMap) {
        HashMap<Integer, Contact> contactPhoneBookMap = readContactsFromPhoneBook();
        if (contactsBook.isEmpty()) {
            contactsBook.putAll(contactPhoneBookMap);
        }
        ArrayList<com.yo.android.model.Contact> toImport = new ArrayList<>();
        HashMap<String, Contact> contactShortHashMap = new HashMap<>();
        for (HashMap.Entry<Integer, Contact> entry : cachePhoneBookHashMap.entrySet()) {
            Contact c = entry.getValue();
            for (String sphone : c.shortPhones) {
                contactShortHashMap.put(sphone, c);
            }
        }

        for (HashMap.Entry<Integer, Contact> pair : contactPhoneBookMap.entrySet()) {
            Integer id = pair.getKey();
            Contact value = pair.getValue();
            Contact existing = cachePhoneBookHashMap.get(id);
            boolean contactModify = false;
            if (existing == null) {
                for (String s : value.shortPhones) {
                    Contact c = contactShortHashMap.get(s);
                    if (c != null) {
                        existing = c;
                        id = existing.id;
                        break;
                    }
                }
            } else {
                boolean contactDeleted = false;
                for (String s : value.shortPhones) {
                    Contact c = contactShortHashMap.get(s);
                    if (c == null) {
                        mLog.i("TAG", "Contact modified");
                        contactModify = true;
                        break;
                    }
                }
            }

            boolean nameChanged = existing != null
                    && (value.first_name != null
                    && value.first_name.length() != 0
                    && !existing.first_name.equals(value.first_name)
                    || value.last_name != null
                    && existing.last_name != null
                    && !existing.last_name.equals(value.last_name));
            if (contactModify || existing == null || nameChanged) {
                for (int a = 0; a < value.phones.size(); a++) {
                    toImport.add(new com.yo.android.model.Contact(value.phones.get(a), ""));
                }
            }
        }

        mLog.i(TAG, "Import Size before check>>>:" + toImport.size());
        //1. Upload to server
        //2.toImport
        Iterator<com.yo.android.model.Contact> toImportIterator1 = toImport.iterator();
        Map<String, com.yo.android.model.Contact> cachedYoContacts = contactsSyncManager.getCachedContacts();
        while (toImportIterator1.hasNext()) {
            com.yo.android.model.Contact contact1 = toImportIterator1.next();
            com.yo.android.model.Contact contact = cachedYoContacts.get(contact1.getPhoneNo());
            //TODO:Require to check names too
            if (contact != null) {
                toImportIterator1.remove();
            }
        }
        List<String> contacts = new ArrayList<>();
        for (com.yo.android.model.Contact contact : toImport) {
            contacts.add(contact.getPhoneNo());
        }
        try {
            if (!contacts.isEmpty()) {
                //up to server
                contactsSyncManager.syncContactsAPI(contacts);
                mLog.i(TAG, "Import Size after check>>>:" + toImport.size());
                //Call sync - get contact
                SyncUtils.triggerRefresh();
            }
            cacheContacts = contactPhoneBookMap;
        } catch (Exception e) {
            mLog.i(TAG, "Exception:>>", e);
        }

    }


    public static class Contact {
        public int id;
        public ArrayList<String> phones = new ArrayList<>();
        public ArrayList<String> phoneTypes = new ArrayList<>();
        public ArrayList<String> shortPhones = new ArrayList<>();
        public ArrayList<Integer> phoneDeleted = new ArrayList<>();
        public String first_name;
        public String last_name;
    }

    private String[] projectionPhones = {
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.LABEL
    };

    private String[] projectionNames = {
            ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID,
            ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
            ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
            ContactsContract.Data.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME
    };


    //===
    private HashMap<Integer, Contact> readContactsFromPhoneBook() {
        HashMap<Integer, Contact> contactsMap = new HashMap<>();
        try {
            if (!hasContactsPermission()) {
                return contactsMap;
            }
            ContentResolver cr = context.getContentResolver();

            HashMap<String, Contact> shortContacts = new HashMap<>();
            ArrayList<Integer> idsArr = new ArrayList<>();
            Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projectionPhones, null, null, null);
            if (pCur != null) {
                if (pCur.getCount()
                        > 0) {
                    while (pCur.moveToNext()) {
                        String number = pCur.getString(1);
                        if (number == null || number.length() == 0) {
                            continue;
                        }
                        number = stripExceptNumbers(number, true);
                        if (number.length() == 0) {
                            continue;
                        }
                        String shortNumber = number;

                        if (number.startsWith("+")) {
                            shortNumber = number.substring(1);
                        }

                        if (shortContacts.containsKey(shortNumber)) {
                            continue;
                        }

                        Integer id = pCur.getInt(0);
                        if (!idsArr.contains(id)) {
                            idsArr.add(id);
                        }

                        int type = pCur.getInt(2);
                        Contact contact = contactsMap.get(id);
                        if (contact == null) {
                            contact = new Contact();
                            contact.first_name = "";
                            contact.last_name = "";
                            contact.id = id;
                            contactsMap.put(id, contact);
                        }

                        contact.shortPhones.add(shortNumber);
                        contact.phones.add(number);
                        contact.phoneDeleted.add(0);

                        if (type == ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM) {
                            contact.phoneTypes.add(pCur.getString(3));
                        } else if (type == ContactsContract.CommonDataKinds.Phone.TYPE_HOME) {
                            contact.phoneTypes.add("PhoneHome");
                        } else if (type == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                            contact.phoneTypes.add("PhoneMobile");
                        } else if (type == ContactsContract.CommonDataKinds.Phone.TYPE_WORK) {
                            contact.phoneTypes.add("PhoneWork");
                        } else if (type == ContactsContract.CommonDataKinds.Phone.TYPE_MAIN) {
                            contact.phoneTypes.add("PhoneMain");
                        } else {
                            contact.phoneTypes.add("PhoneOther");
                        }
                        shortContacts.put(shortNumber, contact);
                    }
                }
                pCur.close();
            }
            String ids = TextUtils.join(",", idsArr);

            pCur = cr.query(ContactsContract.Data.CONTENT_URI, projectionNames,
                    ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID + " IN (" + ids + ") AND " + ContactsContract.Data.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE + "'", null, null);
            if (pCur != null && pCur.getCount() > 0) {
                while (pCur.moveToNext()) {
                    int id = pCur.getInt(0);
                    String fname = pCur.getString(1);
                    String sname = pCur.getString(2);
                    String sname2 = pCur.getString(3);
                    String mname = pCur.getString(4);
                    Contact contact = contactsMap.get(id);
                    if (contact != null && contact.first_name.length() == 0 && contact.last_name.length() == 0) {
                        contact.first_name = fname;
                        contact.last_name = sname;
                        if (contact.first_name == null) {
                            contact.first_name = "";
                        }
                        if (mname != null && mname.length() != 0) {
                            if (contact.first_name.length() != 0) {
                                contact.first_name += " " + mname;
                            } else {
                                contact.first_name = mname;
                            }
                        }
                        if (contact.last_name == null) {
                            contact.last_name = "";
                        }
                        if (contact.last_name.length() == 0 && contact.first_name.length() == 0 && sname2 != null && sname2.length() != 0) {
                            contact.first_name = sname2;
                        }
                    }
                }
                pCur.close();
            }

            try {
                pCur = cr.query(ContactsContract.RawContacts.CONTENT_URI, new String[]{"display_name", ContactsContract.RawContacts.SYNC1, ContactsContract.RawContacts.CONTACT_ID}, ContactsContract.RawContacts.ACCOUNT_TYPE + " = " + "'com.whatsapp'", null, null);
                if (pCur != null) {
                    while ((pCur.moveToNext())) {
                        String phone = pCur.getString(1);
                        if (phone == null || phone.length() == 0) {
                            continue;
                        }
                        boolean withPlus = phone.startsWith("+");
                        phone = parseIntToString(phone);
                        if (phone == null || phone.length() == 0) {
                            continue;
                        }
                        String shortPhone = phone;
                        if (!withPlus) {
                            phone = "+" + phone;
                        }

                        if (shortContacts.containsKey(shortPhone)) {
                            continue;
                        }

                        String name = pCur.getString(0);
                        if (name == null || name.length() == 0) {
                            name = "";//PhoneFormat.getInstance().format(phone);
                        }

                        Contact contact = new Contact();
                        contact.first_name = name;
                        contact.last_name = "";
                        contact.id = pCur.getInt(2);
                        contactsMap.put(contact.id, contact);

                        contact.phoneDeleted.add(0);
                        contact.shortPhones.add(shortPhone);
                        contact.phones.add(phone);
                        contact.phoneTypes.add("PhoneMobile");
                        shortContacts.put(shortPhone, contact);
                    }
                    pCur.close();
                }
            } catch (Exception e) {
                mLog.w(TAG, e);
            }
        } catch (Exception e) {
            mLog.w(TAG, e);
            contactsMap.clear();
        }
        return contactsMap;
    }

    private boolean hasContactsPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            return context.checkSelfPermission(android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
        }
        Cursor cursor = null;
        try {
            ContentResolver cr = context.getContentResolver();
            cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projectionPhones, null, null, null);
            if (cursor == null || cursor.getCount() == 0) {
                return false;
            }
        } catch (Exception e) {
            mLog.w(TAG, e);
        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                mLog.w(TAG, e);
            }
        }
        return true;
    }

    public static String stripExceptNumbers(String str, boolean includePlus) {
        StringBuilder res = new StringBuilder(str);
        String phoneChars = "0123456789";
        if (includePlus) {
            phoneChars += "+";
        }
        for (int i = res.length() - 1; i >= 0; i--) {
            if (!phoneChars.contains(res.substring(i, i + 1))) {
                res.deleteCharAt(i);
            }
        }
        return res.toString();
    }

    private boolean checkContactsInternal() {
        boolean reload = false;
        try {
            if (!hasContactsPermission()) {
                return false;
            }
            ContentResolver cr = context.getContentResolver();
            Cursor pCur = null;
            try {
                pCur = cr.query(ContactsContract.RawContacts.CONTENT_URI, new String[]{ContactsContract.RawContacts.VERSION}, null, null, null);
                if (pCur != null) {
                    StringBuilder currentVersion = new StringBuilder();
                    while (pCur.moveToNext()) {
                        int col = pCur.getColumnIndex(ContactsContract.RawContacts.VERSION);
                        currentVersion.append(pCur.getString(col));
                    }
                    String newContactsVersion = currentVersion.toString();
                    if (lastContactsVersions.length() != 0 && !lastContactsVersions.equals(newContactsVersion)) {
                        reload = true;
                    }
                    lastContactsVersions = newContactsVersion;
                }
            } catch (Exception e) {
                mLog.w(TAG, e);
            } finally {
                if (pCur != null) {
                    pCur.close();
                }
            }
        } catch (Exception e) {
            mLog.w(TAG, e);
        }
        return reload;
    }

    //
    public static Pattern pattern = Pattern.compile("[0-9]+");


    public static String parseIntToString(String value) {
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }


}

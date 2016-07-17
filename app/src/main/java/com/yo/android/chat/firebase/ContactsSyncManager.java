package com.yo.android.chat.firebase;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.google.gson.JsonObject;
import com.orion.android.common.logger.Log;
import com.yo.android.api.YoApi;
import com.yo.android.model.Contact;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.ResponseBody;
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

    @Inject
    public ContactsSyncManager(YoApi.YoService yoService, Context context) {
        this.yoService = yoService;
        this.context = context;
    }

    public ArrayList<String> readContacts() {
        ArrayList<String> nc = new ArrayList<>();
        try {
            Cursor contactsCursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            while (contactsCursor.moveToNext()) {

                String contactId = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts._ID));
                if (Integer.parseInt(contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor phoneNumberCursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{contactId}, null);

                    while (phoneNumberCursor.moveToNext()) {
                        String phoneNumber = phoneNumberCursor.getString(phoneNumberCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        nc.add(phoneNumber);
                    }
                }
            }
            contactsCursor.close();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return nc;
    }
}


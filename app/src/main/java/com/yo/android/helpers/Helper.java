package com.yo.android.helpers;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.provider.ContactsContract;

/**
 * Created by rajesh on 20/9/16.
 */
public class Helper {
    public static void createNewContactWithPhoneNumber(Activity activity, String phoneNumber) {
        Intent i = new Intent(Intent.ACTION_INSERT);
        i.putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber);
        i.setType(ContactsContract.Contacts.CONTENT_TYPE);
        if (Integer.valueOf(Build.VERSION.SDK) > 14) {
            i.putExtra("finishActivityOnSaveCompleted", true); // Fix for 4.0.3 +
        }
        activity.startActivity(i);

    }

    public static void addContactWithPhoneNumber(Activity activity, String phoneNumber) {

        Intent intentInsertEdit = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        intentInsertEdit.putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber);
        if (Integer.valueOf(Build.VERSION.SDK) > 14) {
            intentInsertEdit.putExtra("finishActivityOnSaveCompleted", true); // Fix for 4.0.3 +
        }
        intentInsertEdit.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
        activity.startActivity(intentInsertEdit);
    }
}

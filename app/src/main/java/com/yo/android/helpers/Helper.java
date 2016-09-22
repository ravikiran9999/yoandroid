package com.yo.android.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AlphabetAdapter;
import com.yo.android.model.Contact;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static void displayIndex(Activity context, final ListView indexLayout, final List<Contact> contactList, final ListView listview) {
        final LinkedHashMap mapIndex = getIndexList(contactList);
        final List<String> indexList = new ArrayList<String>(mapIndex.keySet());
        AlphabetAdapter adapter = new AlphabetAdapter(context, indexList);
        indexLayout.setAdapter(adapter);
        indexLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listview.setSelection((Integer) mapIndex.get(indexList.get(position).substring(0, 1)));
            }
        });
    }

    public static LinkedHashMap getIndexList(List<Contact> list) {
        LinkedHashMap mapIndex = new LinkedHashMap<String, Integer>();
        int i = -1;
        for (Contact contact : list) {
            String fruit = contact.getName();
            if (fruit != null && fruit.length() >= 1) {
                String index = fruit.substring(0, 1).toUpperCase();
                // Pattern p = Pattern.compile("^[a-zA-Z]");
                // Matcher m = p.matcher(index);
                // boolean b = m.matches();
                i = i + 1;
                // if (b) {
                if (mapIndex.get(index) == null) {
                    mapIndex.put(index, i);
                }
            }
            // }
        }
        return mapIndex;

    }
}

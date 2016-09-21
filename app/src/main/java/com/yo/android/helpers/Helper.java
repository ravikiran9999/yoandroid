package com.yo.android.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.yo.android.R;
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

    public static void displayIndex(Activity context, LinearLayout indexLayout, final List<Contact> contactList, final ListView listview) {
        final LinkedHashMap mapIndex = getIndexList(contactList);

        TextView textView;
        List<String> indexList = new ArrayList<String>(mapIndex.keySet());
        for (String index : indexList) {
            textView = (TextView) context.getLayoutInflater().inflate(
                    R.layout.side_index_item, null);
            textView.setText(index.substring(0, 1));
            final TextView finalTextView = textView;
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView selectedIndex = (TextView) finalTextView;
                    listview.setSelection((Integer) mapIndex.get(selectedIndex.getText().toString().substring(0, 1)));
                }
            });
            indexLayout.addView(textView);
        }
    }

    public static LinkedHashMap getIndexList(List<Contact> list) {
        LinkedHashMap mapIndex = new LinkedHashMap<String, Integer>();
        int i = -1;
        for (Contact contact : list) {
            String fruit = contact.getName();
            String index = fruit.substring(0, 1).toUpperCase();
            Pattern p = Pattern.compile("^[a-zA-Z]");
            Matcher m = p.matcher(index);
            boolean b = m.matches();
            i = i + 1;
            if (b) {
                if (mapIndex.get(index) == null) {
                    mapIndex.put(index, i);
                }
            }
        }
        return mapIndex;

    }
}

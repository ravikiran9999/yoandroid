package com.yo.android.chat.firebase;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;

import com.yo.android.util.Constants;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * Created by rdoddapaneni on 7/8/2016.
 */

public class Clipboard {

    private static ClipboardManager myClipboard;

    public Clipboard(Context context) {
        myClipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @SuppressLint("NewApi")
    public void copy(String copiedText) {
        ClipData myClip = ClipData.newPlainText(Constants.SELECTED_TEXT, copiedText);
        myClipboard.setPrimaryClip(myClip);
    }

    @SuppressLint("NewApi")
    public String paste(View view) {
        ClipData cp = myClipboard.getPrimaryClip();
        ClipData.Item item = cp.getItemAt(0);
        String text = null;
        if (item.getText() != null) {
            text = item.getText().toString();
        }
        if (text != null) {
            return text;
        }
        return "";
    }
}

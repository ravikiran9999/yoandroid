package com.yo.android.inapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.yo.android.R;

/**
 * Created by Ramesh on 14/4/16.
 */
public class CommonUtils {
    public static void showAlert(final Activity activity, String message, final DialogInterface.OnClickListener clickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(false);
        builder.setTitle(activity.getString(R.string.app_name));
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setMessage(message);
        builder.setPositiveButton("OK", clickListener);
        AlertDialog alert = builder.create();
        alert.show();
    }
}

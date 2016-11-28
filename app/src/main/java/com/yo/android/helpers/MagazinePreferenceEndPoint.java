package com.yo.android.helpers;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by creatives on 11/24/2016.
 */
public class MagazinePreferenceEndPoint {

    private static MagazinePreferenceEndPoint instance;

    public static MagazinePreferenceEndPoint getInstance() {
        if (instance == null) {
            instance = new MagazinePreferenceEndPoint();
        }
        return instance;
    }

    public SharedPreferences.Editor get(Context context, String userId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(userId, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        return editor;
    }

    public SharedPreferences getPref(Context context, String userId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(userId, Context.MODE_PRIVATE);
        return sharedPreferences;
    }
}

package com.yo.android.ui;

import android.content.Context;

/**
 * Created by RajeshBabu on 1/11/17.
 */

class BitmapCache {


    private static BitmapCache instance;

    public static BitmapCache getInstance(Context context) {
        if (instance == null) {
            instance = new BitmapCache();
        }
        return instance;
    }


}

package com.yo.android.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;
import android.widget.ImageView;

/**
 * Created by RajeshBabu on 1/11/17.
 */

class BitmapCache {

    private static BitmapCache instance;
    private static LruCache<String, Bitmap> mMemoryCache;


    public static BitmapCache getInstance(Context context) {
        if (instance == null) {
            instance = new BitmapCache();
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

            // Use 1/8th of the available memory for this memory cache.
            final int cacheSize = maxMemory / 8;

            mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    // The cache size will be measured in kilobytes rather than
                    // number of items.
                    return bitmap.getByteCount() / 1024;
                }
            };

        }
        return instance;
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    private void userCall(Context context, String url, ImageView photoView) {
        Bitmap bitmapFromMemCache = BitmapCache.getInstance(context).getBitmapFromMemCache(url);
        if (bitmapFromMemCache != null) {
            photoView.setImageBitmap(bitmapFromMemCache);
        } else {
            // Resize a Bitmap maintaining aspect ratio based on screen width
        }
    }
}

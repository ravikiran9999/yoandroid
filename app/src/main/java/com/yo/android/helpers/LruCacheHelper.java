package com.yo.android.helpers;

import android.util.LruCache;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by MYPC on 8/13/2016.
 */
@Singleton
public class LruCacheHelper extends LruCache{

    @Inject
    public LruCacheHelper() {
        super(4*1024*1024);
    }
}

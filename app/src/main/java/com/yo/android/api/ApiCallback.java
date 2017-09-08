package com.yo.android.api;

/**
 * Created by rdoddapaneni on 7/12/2017.
 */

public interface ApiCallback<T> {
    void onResult(T result);
}

package com.yo.android.widgets;

/**
 * Created by rdoddapaneni on 5/18/2017.
 */

public interface ScrollTabHolder {

    void adjustScroll(int scrollHeight, int headerTranslationY, int minHeaderTranslation);
    void onScroll(int currentScrollY);
}

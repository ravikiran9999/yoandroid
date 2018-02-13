package com.orion.android.common.util;

/**
 * Created by Ramesh on 9/2/16.
 */
public interface ToastFactory {

    void newToast(final CharSequence text, @Duration final int duration);

    void newToast(final int textResId, @Duration final int duration);

    void showToast(final CharSequence text);

    void showToast(final int textResId);

}

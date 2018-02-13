/*
 * Copyright 2014 Synchronoss Technologies, Inc.  All Rights Reserved.
 *
 * This source code is the confidential and proprietary information of
 * Synchronoss Technologies, Inc.
 *
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Synchronoss Technologies.
 */
package com.yo.android.di;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * The Class InjectedBroadcastReceiver.
 */
public abstract class InjectedBroadcastReceiver extends BroadcastReceiver {

    /**
     * On receive.
     *
     * @param context the context
     * @param intent  the intent
     */
    @Override
    public void onReceive(final Context context, final Intent intent) {
        Injector.obtain(context.getApplicationContext()).inject(this);
    }
}
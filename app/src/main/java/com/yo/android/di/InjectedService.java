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

import android.app.Service;

/**
 * The Class InjectedService.
 */
public abstract class InjectedService extends Service {

    /**
     * On create.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Injector.obtain(getApplication()).inject(this);
    }
}

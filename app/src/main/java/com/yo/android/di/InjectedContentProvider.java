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

import android.content.ContentProvider;

/**
 * The Class InjectedContentProvider.
 */
public abstract class InjectedContentProvider extends ContentProvider {

    /**
     * Inject.
     */
    protected void inject() {
        Injector.obtain(getContext().getApplicationContext()).inject(this);
    }
}

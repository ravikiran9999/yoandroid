package com.yo.android.di;

import android.content.Context;

import dagger.ObjectGraph;

/**
 * Created by Ramesh on 8/3/16.
 */
public final class Injector {
    private static final String INJECTOR_SERVICE = "com.yo.android.di.injector";

    /**
     * Constructor
     */
    private Injector() {
        throw new AssertionError("No instances.");
    }

    // Explicitly doing a custom service.
    @SuppressWarnings({"ResourceType", "WrongConstant"})
    public static ObjectGraph obtain(Context context) {
        return (ObjectGraph) context.getSystemService(INJECTOR_SERVICE);
    }

    public static boolean matchesService(String name) {
        return INJECTOR_SERVICE.equals(name);
    }

}

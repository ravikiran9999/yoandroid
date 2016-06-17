package com.yo.android.di;

import android.content.Context;

import dagger.ObjectGraph;

/**
 * Created by Ramesh on 8/3/16.
 */
public final class Injector {
    private static final String INJECTOR_SERVICE = "com.i2space.etravos.injector";

    @SuppressWarnings({"ResourceType", "WrongConstant"}) // Explicitly doing a custom service.
    public static ObjectGraph obtain(Context context) {
        return (ObjectGraph) context.getSystemService(INJECTOR_SERVICE);
    }

    public static boolean matchesService(String name) {
        return INJECTOR_SERVICE.equals(name);
    }

    private Injector() {
        throw new AssertionError("No instances.");
    }
}

package com.yo.android.app;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.firebase.client.Firebase;
import com.yo.android.di.Injector;
import com.yo.android.di.RootModule;

import dagger.ObjectGraph;

/**
 * Created by Ramesh on 17/06/16.
 */
public class BaseApp extends MultiDexApplication {

    private ObjectGraph objectGraph;

    @Override
    public void onCreate() {
        super.onCreate();
        injectDependencies();

        /* Enable disk persistence  */
        Firebase.getDefaultConfig().setPersistenceEnabled(true);
        Firebase.setAndroidContext(getApplicationContext());
    }



    private void injectDependencies() {
        objectGraph = ObjectGraph.create(new RootModule(this));
        objectGraph.inject(this);
    }

    @Override
    public Object getSystemService(String name) {
        if (Injector.matchesService(name)) {
            return objectGraph;
        }
        return super.getSystemService(name);
    }
}

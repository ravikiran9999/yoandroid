package com.yo.android.app;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.firebase.client.Firebase;
import com.google.firebase.database.FirebaseDatabase;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.di.Injector;
import com.yo.android.di.RootModule;
import com.yo.android.util.Constants;
import com.yo.android.util.ReCreateService;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.ObjectGraph;

/**
 * Created by Ramesh on 17/06/16.
 */
public class BaseApp extends MultiDexApplication {

    private ObjectGraph objectGraph;

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;

    @Override
    public void onCreate() {
        super.onCreate();
        injectDependencies();

        /* Enable disk persistence  */
       // FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        Firebase.getDefaultConfig().setPersistenceEnabled(true);
        Firebase.setAndroidContext(getApplicationContext());
       // ReCreateService.getInstance(this).start(this);

        
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

    @Override
    public void onTerminate() {
        super.onTerminate();
        preferenceEndPoint.saveBooleanPreference(Constants.IS_IN_APP, false);
    }
}

package com.yo.android.app;

import android.content.Intent;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.firebase.client.Firebase;
import com.flurry.android.FlurryAgent;
import com.flurry.android.FlurryAgentListener;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.di.Injector;
import com.yo.android.di.RootModule;
import com.yo.android.util.Constants;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.ObjectGraph;

/**
 * Created by Ramesh on 17/06/16.
 */
public class BaseApp extends MultiDexApplication {

    private ObjectGraph objectGraph;
    private static BaseApp baseAppInstance;

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;
    private FlurryAgentListener flurryAgentListener;

    @Override
    public void onCreate() {
        super.onCreate();
        baseAppInstance = this;
        injectDependencies();

        /* Enable disk persistence  */
        // FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        Firebase.getDefaultConfig().setPersistenceEnabled(true);
        Firebase.setAndroidContext(getApplicationContext());
        // ReCreateService.getInstance(this).start(this);

        flurryAgentListener = new FlurryAgentListener() {
            @Override
            public void onSessionStarted() {
                Log.d("BaseApp", "In onSessionStarted of Flurry");
            }
        };
        initFlurry();
    }

    public static BaseApp get() {
        return baseAppInstance;
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

    private void initFlurry() {
        new FlurryAgent.Builder()
                .withLogEnabled(true)
                .withCaptureUncaughtExceptions(true)
                //.withContinueSessionMillis(10)
                //.withLogEnabled(true)
                .withLogLevel(Log.VERBOSE)
                .withListener(flurryAgentListener)
                .build(this, Constants.FLURRY_API_KEY);
    }
}

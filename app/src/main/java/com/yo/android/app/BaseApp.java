package com.yo.android.app;

import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.firebase.client.Firebase;
import com.flurry.android.FlurryAgent;
import com.flurry.android.FlurryAgentListener;
import com.orion.android.common.preferences.PreferenceEndPoint;
//import com.squareup.leakcanary.LeakCanary;
import com.yo.android.di.Injector;
import com.yo.android.di.RootModule;
import com.yo.android.util.Constants;
import com.yo.android.util.FireBaseHelper;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.ObjectGraph;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Ramesh on 17/06/16.
 */
public class BaseApp extends MultiDexApplication {

    private static final String TAG = BaseApp.class.getSimpleName();
    private ObjectGraph objectGraph;
    private static BaseApp baseAppInstance;
    protected String userAgent;

    public static boolean appRunning;

    // Production
    private static final String FLURRY_API_KEY = "GRYKGBSF2C3XWJRVCXGP";

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;
    @Inject
    FireBaseHelper fireBaseHelper;

    private static RealmConfiguration realmConfiguration;
    private FlurryAgentListener flurryAgentListener;

    @Override
    public void onCreate() {
        super.onCreate();
        baseAppInstance = this;

        // Memory leaks
        /*if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);*/


        /* Enable disk persistence  */
        // FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        Firebase.getDefaultConfig().setPersistenceEnabled(true);
        Firebase.setAndroidContext(getApplicationContext());

        // The default Realm file is "default.realm" in Context.getFilesDir();
        // we'll change it to "myrealm.realm"
        // initialize realm
        try {
            Realm.init(this);
            realmConfiguration = new RealmConfiguration.Builder().build();
            Realm.setDefaultConfiguration(realmConfiguration);
        } catch (Exception e) {
            e.printStackTrace();
        }

        injectDependencies();

        // ReCreateService.getInstance(this).start(this);

        flurryAgentListener = new FlurryAgentListener() {
            @Override
            public void onSessionStarted() {
                Log.d("BaseApp", "In onSessionStarted of Flurry");
            }
        };
        initFlurry();


        // Fix for camera in nougat
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            builder.detectFileUriExposure();
        }

        /*StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                //.penaltyDeath()
                .build());*/

    }

    public static RealmConfiguration getRealmConfiguration() {
        if(realmConfiguration == null) {
            realmConfiguration = new RealmConfiguration.Builder().build();
        }
        return realmConfiguration;
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
                .build(this, FLURRY_API_KEY);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        
    }
}

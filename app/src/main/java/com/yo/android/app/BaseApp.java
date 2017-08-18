package com.yo.android.app;

import android.content.Intent;
import android.os.StrictMode;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.firebase.client.Firebase;
import com.google.android.exoplayer2.BuildConfig;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
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
    protected String userAgent;

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;

    @Override
    public void onCreate() {
        super.onCreate();
        injectDependencies();
        userAgent = Util.getUserAgent(this, "ExoPlayerDemo");
        /* Enable disk persistence  */
        // FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        Firebase.getDefaultConfig().setPersistenceEnabled(true);
        Firebase.setAndroidContext(getApplicationContext());
        // ReCreateService.getInstance(this).start(this);

        // Fix for camera in nougat
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
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

    public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(this, bandwidthMeter,
                buildHttpDataSourceFactory(bandwidthMeter));
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter);
    }

    public boolean useExtensionRenderers() {
        return BuildConfig.FLAVOR.equals("withExtensions");
    }

}

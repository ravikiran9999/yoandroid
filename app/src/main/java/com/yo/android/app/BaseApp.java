package com.yo.android.app;

import android.app.Application;

import com.orion.android.common.logger.Log;
import com.yo.android.di.Injector;
import com.yo.android.di.RootModule;

import javax.inject.Inject;

import dagger.ObjectGraph;

/**
 * Created by Ramesh on 17/06/16.
 */
public class BaseApp extends Application {

    private ObjectGraph objectGraph;
    @Inject
    Log mLog;

    @Override
    public void onCreate() {
        super.onCreate();
        injectDependencies();
        //
        mLog.logToLogCat(true);
    }

    private void injectDependencies() {
        objectGraph = ObjectGraph.create(new RootModule(this));
        objectGraph.inject(this);
    }

//    @Override
//    public void inject(Object object) {
//        objectGraph.inject(object);
//    }

    @Override
    public Object getSystemService(String name) {
        if (Injector.matchesService(name)) {
            return objectGraph;
        }
        return super.getSystemService(name);
    }
}

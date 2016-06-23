package com.yo.android.di;

import android.content.Context;

import com.yo.android.app.BaseApp;
import com.yo.android.notification.MyInstanceIDListenerService;
import com.yo.android.ui.MainActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Ramesh on 17/06/16.
 */
@Module(
        injects = {
                BaseApp.class,
                MainActivity.class,
                MyInstanceIDListenerService.class

        },
        includes = {
                AppModule.class,
                SharedPreferencesModule.class
        }
)

public class RootModule {

    private BaseApp app;

    /**
     * Constructor
     *
     * @param app
     */
    public RootModule(BaseApp app) {
        this.app = app;
    }

    /**
     * The provide application context
     *
     * @return
     */
    @Provides
    @Singleton
    public Context provideApplicationContext() {
        return app;
    }

}

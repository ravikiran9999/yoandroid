package com.yo.android.di;

import android.content.Context;

import com.orion.android.common.logger.Log;
import com.orion.android.common.logger.LogImpl;
import com.orion.android.common.util.ResourcesHelper;
import com.orion.android.common.util.ToastFactory;
import com.orion.android.common.util.ToastFactoryImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Ramesh on 17/06/16.
 */

@Module(
        complete = false,
        library = true
)
public class AppModule {

    @Singleton
    @Provides
    Log provideLog() {
        Log log = new LogImpl();
        log.logToLogCat(true);
        return log;
    }

    @Singleton
    @Provides
    ToastFactory provideToastFactory(Context context) {
        return new ToastFactoryImpl(context);
    }


    @Singleton
    @Provides
    ResourcesHelper provideResourcesHelper(Context context) {
        return new ResourcesHelper(context);
    }

}

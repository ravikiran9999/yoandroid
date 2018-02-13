package com.yo.android.di;

import android.content.Context;

import com.orion.android.common.logger.Log;
import com.orion.android.common.logger.LogImpl;
import com.orion.android.common.util.ConnectivityHelper;
import com.orion.android.common.util.ResourcesHelper;
import com.orion.android.common.util.ToastFactory;
import com.orion.android.common.util.ToastFactoryImpl;
import com.yo.android.ui.uploadphoto.ImagePickHelper;

import javax.inject.Named;
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

    @Provides
    ImagePickHelper provideCameraIntent(Context context) {
        return new ImagePickHelper();
    }


    @Singleton
    @Provides
    ResourcesHelper provideResourcesHelper(Context context) {
        return new ResourcesHelper(context);
    }

    @Singleton
    @Provides
    ConnectivityHelper provideConnectivityHelper(Context context) {
        return new ConnectivityHelper(context);
    }

    @Singleton
    @Provides
    @Named("voip_support")
    boolean provideIsVoipSupported(Context context) {
        return true;
    }

}

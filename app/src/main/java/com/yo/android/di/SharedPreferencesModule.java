package com.yo.android.di;

import android.content.Context;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.orion.android.common.preferences.PreferenceEndPointImpl;

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
public class SharedPreferencesModule {

    @Singleton
    @Provides
    @Named("login")
    PreferenceEndPoint provideLoginShareEndPoint(Context context) {
        return new PreferenceEndPointImpl(context, "login");
    }

    @Singleton
    @Provides
    @Named("sip_user")
    PreferenceEndPoint provideSipUserShareEndPoint(Context context) {
        PreferenceEndPoint endPoint = new PreferenceEndPointImpl(context, "sip_user");
        endPoint.saveStringPreference("username", "9701449817");
        endPoint.saveStringPreference("password", "123456");
        return endPoint;
    }
}

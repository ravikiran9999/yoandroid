package com.yo.android.di;

import android.content.Context;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by rdoddapaneni on 12/20/2016.
 */

@Module(
        complete = false,
        library = true
)
public class JobsModule {
    @Provides
    @Singleton
    FirebaseJobDispatcher provideFirebaseJobDispatcher(Context context) {
        return new FirebaseJobDispatcher(new GooglePlayDriver(context));
    }

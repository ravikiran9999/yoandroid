package com.yo.android.di;

import android.content.Context;

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
public class AwsModule {

    @Singleton
    @Provides
    AwsLogsCallBack provideAwsLogsCallBack(Context context) {
        return new AwsCallBackImpl();
    }
}

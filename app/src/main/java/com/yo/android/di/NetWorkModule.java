package com.yo.android.di;

import com.yo.android.vox.VoxApi;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Ramesh on 1/7/16.
 */


@Module(
        complete = false,
        library = true
)
public class NetWorkModule {

    @Singleton
    @Provides
    VoxApi.VoxService provideVoxService() {
        VoxApi voxApi = new VoxApi();
        return voxApi.buildAdapter();
    }

}

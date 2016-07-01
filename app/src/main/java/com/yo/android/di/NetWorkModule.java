package com.yo.android.di;

import com.yo.android.api.YoApi;
import com.yo.android.vox.VoxApi;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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
        return buildAdapter(VoxApi.BASE_URL, VoxApi.VoxService.class);
    }

    @Singleton
    @Provides
    YoApi.YoService provideYoService() {
        return buildAdapter(YoApi.BASE_URL, YoApi.YoService.class);
    }

    private <T> T buildAdapter(String baseUrl, Class<T> clazz) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient defaultHttpClient = new OkHttpClient
                .Builder()
                .addInterceptor(interceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(defaultHttpClient)
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(clazz);
    }
}

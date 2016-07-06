package com.yo.android.di;

import com.orion.android.common.logger.Log;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.api.YoApi;
import com.yo.android.vox.VoxApi;

import javax.inject.Named;
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
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        return buildAdapter(VoxApi.BASE_URL, VoxApi.VoxService.class, builder);
    }

    @Singleton
    @Provides
    YoApi.YoService provideYoService(YoApi.YoRefreshTokenService tokenService, @Named("login") PreferenceEndPoint endPoint, Log log) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.authenticator(new TokenAuthenticator(tokenService, endPoint, log));
        return buildAdapter(YoApi.BASE_URL, YoApi.YoService.class, builder);
    }

    @Singleton
    @Provides
    YoApi.YoRefreshTokenService provideYoRefreshTokenService() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        return buildAdapter(YoApi.BASE_URL, YoApi.YoRefreshTokenService.class, builder);
    }

    private <T> T buildAdapter(String baseUrl, Class<T> clazz, OkHttpClient.Builder builder) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient defaultHttpClient = builder
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

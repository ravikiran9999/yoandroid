package com.yo.android.di;

import android.content.Context;

import com.orion.android.common.logger.Log;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.api.YoApi;
import com.yo.android.vox.VoxApi;

import java.io.File;
import java.io.IOException;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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
    YoApi.YoService provideYoService(Context context, YoApi.YoRefreshTokenService tokenService, @Named("login") PreferenceEndPoint endPoint, Log log) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.authenticator(new TokenAuthenticator(tokenService, endPoint, log));
        long SIZE_OF_CACHE = 10 * 1024 * 1024; // 10 MB
        Cache cache = new Cache(new File(context.getCacheDir(), "http"), SIZE_OF_CACHE);
        builder.cache(cache);
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
        //

        OkHttpClient defaultHttpClient = builder
                .addInterceptor(interceptor)
                //work like charm! Enable later
//                .addNetworkInterceptor(mCacheControlInterceptor)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .client(defaultHttpClient)
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(clazz);
    }

    //https://gist.github.com/polbins/1c7f9303d2b7d169a3b1
    private static final Interceptor mCacheControlInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            // Add Cache Control only for GET methods
            if (request.method().equals("GET")) {
                if (false/*ConnectivityHelper.isNetworkAvailable(mContext)*/) {
                    // 1 day
                    request.newBuilder()
                            .header("Cache-Control", "only-if-cached")
                            .build();
                } else {
                    // 4 weeks stale
                    request.newBuilder()
                            .header("Cache-Control", "public, max-stale=2419200")
                            .build();
                }
            }

            Response response = chain.proceed(request);

            // Re-write response CC header to force use of cache
            return response;
//            .newBuilder()
//                    .header("Cache-Control", "public, max-age=86400") // 1 day
//                    .build();
        }
    };
}

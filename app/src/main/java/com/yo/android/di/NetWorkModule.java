package com.yo.android.di;

import android.content.Context;

import com.orion.android.common.logger.Log;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.orion.android.common.util.ConnectivityHelper;
import com.yo.android.BuildConfig;
import com.yo.android.api.YoApi;
import com.yo.android.vox.VoxApi;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

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
    VoxApi.VoxService provideVoxService(Context context, ConnectivityHelper connectivityHelper) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        enableCache(context, builder);
        return buildAdapter(VoxApi.BASE_URL, VoxApi.VoxService.class, builder, connectivityHelper);
    }

    @Singleton
    @Provides
    YoApi.YoService provideYoService(Context context, YoApi.YoRefreshTokenService tokenService, @Named("login") PreferenceEndPoint endPoint, Log log, ConnectivityHelper connectivityHelper) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.authenticator(new TokenAuthenticator(context, tokenService, endPoint, log));
        enableCache(context, builder);
        return buildAdapter(BuildConfig.BASE_URL, YoApi.YoService.class, builder, connectivityHelper);
    }

    private void enableCache(Context context, OkHttpClient.Builder builder) {
        long SIZE_OF_CACHE = 10 * 1024 * 1024; // 10 MB
        Cache cache = new Cache(new File(context.getCacheDir(), "http"), SIZE_OF_CACHE);
        builder.cache(cache);

    }

    @Singleton
    @Provides
    YoApi.YoRefreshTokenService provideYoRefreshTokenService(Context context, ConnectivityHelper connectivityHelper) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        enableCache(context, builder);
        return buildAdapter(BuildConfig.BASE_URL, YoApi.YoRefreshTokenService.class, builder, connectivityHelper);
    }

    private <T> T buildAdapter(String baseUrl, Class<T> clazz, OkHttpClient.Builder builder, ConnectivityHelper connectivityHelper) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        //
        OkHttpClient defaultHttpClient = builder
                .addInterceptor(interceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                //Commented below code as Getting OOM : Created an issue in
                // https://github.com/square/okhttp/issues/2781
//                .addInterceptor(new OfflineResponseInterceptor(connectivityHelper))
//                .addNetworkInterceptor(new RewriteResponseInterceptor())
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .client(defaultHttpClient)
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(clazz);
    }

    public class CacheInterceptor implements Interceptor {
        final ConnectivityHelper mConnectivityHelper;

        public CacheInterceptor(ConnectivityHelper connectivityHelper) {
            mConnectivityHelper = connectivityHelper;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            boolean isCacheEnable = true;
            if (request.url().toString().contains("api/tags.json")) {
                isCacheEnable = false;
            }
            if (!isCacheEnable) {
                Response response = chain.proceed(request);
                return response;
            }
            // Add Cache Control only for GET methods
            if (isCacheEnable && request.method().equals("GET")) {
                if (mConnectivityHelper.isConnected()) {
                    // 1 day
                    request.newBuilder()
                            .header("Cache-Control", "only-if-cached,max-age=31536000")
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
            return response
                    .newBuilder()
                    .header("Cache-Control", "public, max-age=86400") // 1 day
                    .build();

        }
    }

    //Solution: http://stackoverflow.com/a/36795214/874752
    public class RewriteResponseInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {

            Response originalResponse = chain.proceed(chain.request());
            String cacheControl = originalResponse.header("Cache-Control");

            if (cacheControl == null || cacheControl.contains("no-store") || cacheControl.contains("no-cache") ||
                    cacheControl.contains("must-revalidate") || cacheControl.contains("max-age=0")) {
                return originalResponse.newBuilder()
                        .header("Cache-Control", "public, max-age=" + 10)
                        .build();
            } else {
                return originalResponse;
            }
        }

    }

    public class OfflineResponseInterceptor implements Interceptor {
        final ConnectivityHelper mConnectivityHelper;

        public OfflineResponseInterceptor(ConnectivityHelper connectivityHelper) {
            mConnectivityHelper = connectivityHelper;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            if (!mConnectivityHelper.isConnected()) {
                // tolerate 4-weeks stale
                int maxStale = 60 * 60 * 24 * 28;
                request = request.newBuilder()
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                        .build();
            }

            return chain.proceed(request);
        }
    }
}

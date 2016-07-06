package com.yo.android.di;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.api.YoApi;
import com.yo.android.model.OTPResponse;
import com.yo.android.vox.VoxApi;

import java.io.IOException;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Authenticator;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
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
    YoApi.YoService provideYoService(YoApi.YoRefreshTokenService tokenService, @Named("login") PreferenceEndPoint endPoint) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.authenticator(new TokenAuthenticator(tokenService, endPoint));
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

    public class TokenAuthenticator implements Authenticator {
        YoApi.YoRefreshTokenService tokenService;
        PreferenceEndPoint preferenceEndPoint;

        public TokenAuthenticator(YoApi.YoRefreshTokenService tokenService, PreferenceEndPoint preferenceEndPoint) {
            this.tokenService = tokenService;
            this.preferenceEndPoint = preferenceEndPoint;

        }

        @Override
        public Request authenticate(Route route, Response response) throws IOException {
            String refreshToken = preferenceEndPoint.getStringPreference("refresh_token");
            if (response.code() == 401) {
                try {
                    OTPResponse responseBody
                            = tokenService.refreshToken(YoApi.CLIENT_ID, YoApi.CLIENT_SECRET, "refresh_token", refreshToken)
                            .execute()
                            .body();
                    preferenceEndPoint.saveStringPreference(YoApi.ACCESS_TOKEN, responseBody.getAccessToken());
                    preferenceEndPoint.saveStringPreference(YoApi.REFRESH_TOKEN, responseBody.getRefreshToken());
                    HttpUrl httpUrl = response.request().url().newBuilder().addQueryParameter(YoApi.ACCESS_TOKEN, responseBody.getAccessToken()).build();
                    // Add new header to rejected request and retry it
                    return response.request()
                            .newBuilder()
                            .url(httpUrl)
                            .build();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Add new header to rejected request and retry it
            return response.request().newBuilder()
                    .build();

        }
    }
}

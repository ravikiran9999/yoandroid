package com.yo.android.di;

import com.orion.android.common.logger.Log;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.api.YoApi;
import com.yo.android.model.OTPResponse;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class TokenAuthenticator implements Authenticator {
    private YoApi.YoRefreshTokenService tokenService;
    private PreferenceEndPoint preferenceEndPoint;
    private Log mLog;

    public TokenAuthenticator(YoApi.YoRefreshTokenService tokenService, PreferenceEndPoint preferenceEndPoint, Log log) {
        this.tokenService = tokenService;
        this.preferenceEndPoint = preferenceEndPoint;
        this.mLog = log;
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
                // Add new httpurl to rejected request and retry it
                return response.request()
                        .newBuilder()
                        .url(httpUrl)
                        .build();

            } catch (Exception e) {
                mLog.w("TokenAuthenticator", e);
            }
        }
        return response.request();

    }
}

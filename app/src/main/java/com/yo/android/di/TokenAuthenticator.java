package com.yo.android.di;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.orion.android.common.logger.Log;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.BuildConfig;
import com.yo.android.api.YoApi;
import com.yo.android.chat.ui.LoginActivity;
import com.yo.android.model.OTPResponse;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class TokenAuthenticator implements Authenticator {
    private static final boolean TOKEN_EXPIRE = false;
    private YoApi.YoRefreshTokenService tokenService;
    private PreferenceEndPoint preferenceEndPoint;
    private Log mLog;
    private int tokenExpireCount = 0;
    private Context mContext;

    public TokenAuthenticator(Context context, YoApi.YoRefreshTokenService tokenService, PreferenceEndPoint preferenceEndPoint, Log log) {
        this.tokenService = tokenService;
        this.preferenceEndPoint = preferenceEndPoint;
        this.mLog = log;
        this.mContext = context;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        String refreshToken = preferenceEndPoint.getStringPreference(YoApi.REFRESH_TOKEN);
        boolean isRequestForTokens = response.request().url().toString().contains("oauth/token.json");
        boolean sessionExpire = preferenceEndPoint.getBooleanPreference(Constants.SESSION_EXPIRE, false);
        if (((!isRequestForTokens && tokenExpireCount > 5) || sessionExpire)) {
            //Session Expire
            if (!sessionExpire) {
                preferenceEndPoint.clearAll();
                preferenceEndPoint.saveBooleanPreference(Constants.SESSION_EXPIRE, true);
                //Reset
                tokenExpireCount = 0;
                Intent intent = new Intent(mContext, LoginActivity.class);
                if (mContext != null) {
                    Util.cancelAllNotification(mContext);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra(Constants.SESSION_EXPIRE, true);
                mContext.startActivity(intent);
            } else {
                mLog.e("TokenAuthenticator", "TokenExpireCount - %d", tokenExpireCount);
            }
        } else if (!isRequestForTokens && response.code() == 401 && !TextUtils.isEmpty(refreshToken)) {
            try {
                tokenExpireCount++;
                mLog.e("TokenAuthenticator", "TokenExpireCount - %d", tokenExpireCount);
                OTPResponse responseBody
                        = tokenService.refreshToken(BuildConfig.CLIENT_ID, BuildConfig.CLIENT_SECRET, "refresh_token", refreshToken)
                        .execute()
                        .body();
                refreshToken = responseBody.getRefreshToken();

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

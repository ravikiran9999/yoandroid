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
import com.yo.android.pjsip.YoSipService;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;
import com.yo.android.voip.VoipConstants;
import com.yo.dialer.CallExtras;
import com.yo.dialer.DialerConfig;

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
    private long tokenSuccessTime = 0;
    private Context mContext;

    private Object lock = new Object();

    String refreshToken = null;

    public TokenAuthenticator(Context context, YoApi.YoRefreshTokenService tokenService, PreferenceEndPoint preferenceEndPoint, Log log) {
        this.tokenService = tokenService;
        this.preferenceEndPoint = preferenceEndPoint;
        refreshToken = preferenceEndPoint.getStringPreference(YoApi.REFRESH_TOKEN);
        this.mLog = log;
        this.mContext = context;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        //mLog.e("TokenAuthenticator", "start authentication");
        synchronized (lock) {
            refreshToken = preferenceEndPoint.getStringPreference(YoApi.REFRESH_TOKEN);
            boolean isRequestForTokens = response.request().url().toString().contains("oauth/token.json");
            boolean sessionExpire = preferenceEndPoint.getBooleanPreference(Constants.SESSION_EXPIRE, false);
            if (System.currentTimeMillis() - tokenSuccessTime < 120000) {
                String accessToken = preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
                mLog.e("AccessToken Call ", accessToken);
                mLog.e("HttpUrl Calls", response.request().url().toString());
                if (response.request().url().toString().contains(accessToken)) {
                    return response.request();
                }
                HttpUrl httpUrl = response.request().url().newBuilder().addQueryParameter(YoApi.ACCESS_TOKEN, accessToken).build();
                mLog.e("HttpUrl Call", httpUrl.toString());
                // Add new httpurl to rejected request and retry it
                //mLog.e("TokenAuthenticator", "finish authentication");
                return response.request()
                        .newBuilder()
                        .url(httpUrl)
                        .build();
            }
            if (((!isRequestForTokens && tokenExpireCount > 5) || sessionExpire)) {
                //Session Expire
                if (!sessionExpire) {
                    preferenceEndPoint.saveBooleanPreference(Constants.SESSION_EXPIRE, true);
                    //Reset
                    tokenExpireCount = 0;
                    mLog.e("TokenAuthenticator", "Refreshtoken - " + refreshToken);
                    mLog.e("TokenAuthenticator", "access token - " + preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN));
                    mLog.e("TokenAuthenticator", "Final TokenExpireCount -" + tokenExpireCount);
                    preferenceEndPoint.clearAll();
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    //To Stop calls when logout
                    if (DialerConfig.IS_NEW_SIP) {
                        Intent service = new Intent(mContext, com.yo.dialer.YoSipService.class);
                        service.setAction(CallExtras.UN_REGISTER);
                        mContext.startService(service);
                    } else {
                        Intent intents = new Intent(VoipConstants.ACCOUNT_LOGOUT, null, mContext, YoSipService.class);
                        mContext.startService(intents);
                    }

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
                    if (responseBody != null) {
                        refreshToken = responseBody.getRefreshToken();

                        preferenceEndPoint.saveStringPreference(YoApi.REFRESH_TOKEN, refreshToken);
                        preferenceEndPoint.saveStringPreference(YoApi.ACCESS_TOKEN, responseBody.getAccessToken());
                        mLog.e("AccessToken", responseBody.getAccessToken());
                        HttpUrl httpUrl = response.request().url().newBuilder().addQueryParameter(YoApi.ACCESS_TOKEN, responseBody.getAccessToken()).build();
                        mLog.e("HttpUrl ", httpUrl.toString());
                        tokenSuccessTime = System.currentTimeMillis();
                        tokenExpireCount = 0;
                        // Add new httpurl to rejected request and retry it
                        //mLog.e("TokenAuthenticator", "finish authentication");
                        return response.request()
                                .newBuilder()
                                .url(httpUrl)
                                .build();
                    }
                } catch (Exception e) {
                    mLog.w("TokenAuthenticator", e);
                }
            }
        }
        //mLog.e("TokenAuthenticator", "finish authentication");
        return response.request();
    }
}

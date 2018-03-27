package com.orion.android.common.logging;


import com.yo.android.BuildConfig;

/**
 * @author :paradigmcreatives
 */
public class GlobalClass {

    public static final boolean DEBUG = true;
    public static final String BUCKET_NAME = "yo-android";
    public static final String NEW_BUCKET_NAME = "yo-app-logs";
    public static final String LOG_FB_USER_NAME = "AWS_LOGS";
    public static final String ACCESS_KEY = "AKIAJDD3DSXXKRPASWMA";
    public static final String SECRET_KEY = "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";

    private GlobalClass() {
        //default constructor needed
    }

    public static String getBucketName() {
        if (!BuildConfig.YO_AWS_ACCOUNT) {
            return BUCKET_NAME;
        } else {
            return NEW_BUCKET_NAME;
        }
    }
}

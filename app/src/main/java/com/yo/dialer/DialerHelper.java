package com.yo.dialer;

import com.yo.android.BuildConfig;

/**
 * Created by root on 11/7/17.
 */

public class DialerHelper {
    public static String parsePhoneNumber(String username) {
        if (username != null) {
            return username.substring(username.indexOf(BuildConfig.RELEASE_USER_TYPE) + 6, username.length() - 1);
        }
        return username;
    }

    public static String getURI(String displayname, String username, String domain) {
        return String.format("\"%s\"<sip:%s@%s>", displayname, username, domain);
    }

    public static String getRegister(String domain) {
        return String.format("sip:%s:%s", domain, 5060);
    }

    public static String getProxy(String domain) {
        return getRegister(domain);
    }
}

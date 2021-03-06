package com.yo.dialer.yopj;

import android.content.Context;

import com.yo.android.pjsip.MyAccount;
import com.yo.android.pjsip.MyApp;
import com.yo.android.pjsip.SipProfile;
import com.yo.dialer.DialerConfig;
import com.yo.dialer.DialerHelper;
import com.yo.dialer.DialerLogs;
import com.yo.dialer.model.SipProperties;

import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.AccountSipConfig;
import org.pjsip.pjsua2.AuthCredInfo;
import org.pjsip.pjsua2.AuthCredInfoVector;
import org.pjsip.pjsua2.StringVector;

/**
 * Created by Rajesh Babu. on 11/7/17.
 */

public class YoPJRegConfig {
    private static final String TAG = YoPJRegConfig.class.getSimpleName();
    private static Context mContext;

    public static YoAccount buildAccount(Context context, SipProperties properties, YoAccount myAccount, YoApp yoApp, String id, String msg) throws UnsatisfiedLinkError {
        DialerLogs.messageI(TAG, "YO========buildAccount===========" + myAccount);
        mContext = context;
        if (myAccount != null) {
            return myAccount;
        }
        AccountConfig accCfg = new AccountConfig();
        accCfg.setIdUri(id);
        accCfg.getRegConfig().setTimeoutSec(com.yo.android.pjsip.YoSipService.EXPIRE);
        accCfg.getNatConfig().setIceEnabled(properties.isICE());
        accCfg.getVideoConfig().setAutoTransmitOutgoing(true);
        accCfg.getVideoConfig().setAutoShowIncoming(true);
        accCfg.getNatConfig().setTurnEnabled(properties.isTurnEnable());
        //TODO: Add turn server and users details
        if (yoApp == null) {
            startSipService(context, yoApp, properties);
        }

        YoAccount yoAccount = yoApp.addAcc(accCfg);
        DialerLogs.messageI(TAG, "YO========buildAccount return yo account as===========" + myAccount);

        return yoAccount;
    }

    private static boolean startSipService(Context context, YoApp yoApp, SipProperties properties) {
        DialerLogs.messageI(TAG, "YO========INIT===========");
        if (!YoApp.isInitialized) {
            yoApp.init(properties, new YoCallObserver(), context.getFilesDir().getAbsolutePath());
        }
        return true;
    }

    public static void updateSIPConfig(YoAccount yoAccount, SipProperties properties) {
        String id = DialerHelper.getInstance(mContext).getURI(properties.getDisplayName(), properties.getSipUsername(), properties.getSipServer());
        if (yoAccount != null) {
            DialerLogs.messageI(TAG, "YO======updateSIPConfig Sip URI " + id);
            AccountConfig cfg = yoAccount.cfg;
            if (properties != null && properties.getSipUsername() != null) {
                configAccount(cfg, id, properties);
                try {
                    if (cfg != null && cfg.getRegConfig() != null) {
                        cfg.getRegConfig().setTimeoutSec(com.yo.android.pjsip.YoSipService.EXPIRE);
                        yoAccount.modify(cfg);
                    }
                } catch (Exception e) {
                    DialerLogs.messageE(TAG, e.getMessage());
                }
            } else {
                DialerLogs.messageE(TAG, "USer name is null so wrong call.");
            }
        } else {
            DialerLogs.messageE(TAG, "Created account object is null");
        }
    }

    private static void configAccount(AccountConfig accCfg, String acc_id, SipProperties properties) {
        DialerLogs.messageI(TAG, "YO======configAccount :" + properties.getSipUsername() + ", Password = " + properties.getSipPassword());

        String registrar = DialerHelper.getInstance(mContext).getRegister(properties.getSipServer());
        String username = properties.getSipUsername();
        String password = properties.getSipPassword();
        accCfg.setIdUri(acc_id);
        DialerLogs.messageI(TAG, "YO======Register:" + registrar);

        accCfg.getRegConfig().setRegistrarUri(registrar);
        AccountSipConfig sipConfig = accCfg.getSipConfig();
        AuthCredInfoVector creds = sipConfig.getAuthCreds();
        creds.clear();
        if (username != null && !username.isEmpty() && username.length() != 0) {
            creds.add(new AuthCredInfo("Digest", "*", username, 0, password));
        }
        StringVector proxies = new StringVector();

        String proxyServer = DialerConfig.SIP + properties.getProxyServer();
        DialerLogs.messageI(TAG, "YO======Proxy Server:" + proxyServer);
        if (proxies != null) {
            proxies.add(proxyServer);
            if (sipConfig != null) {
                sipConfig.setProxies(proxies);
            }
        }

        /* Enable ICE */
        DialerLogs.messageI(TAG, "YO======Settings ICE:" + properties.isICE());
        accCfg.getNatConfig().setIceEnabled(properties.isICE());
    }

}

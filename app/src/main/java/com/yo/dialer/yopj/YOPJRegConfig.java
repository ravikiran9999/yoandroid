package com.yo.dialer.yopj;

import android.content.Context;

import com.yo.android.pjsip.MyAccount;
import com.yo.android.pjsip.MyApp;
import com.yo.android.pjsip.SipProfile;
import com.yo.dialer.DialerHelper;
import com.yo.dialer.DialerLogs;

import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.AuthCredInfo;
import org.pjsip.pjsua2.AuthCredInfoVector;
import org.pjsip.pjsua2.StringVector;

/**
 * Created by Rajesh Babu. on 11/7/17.
 */

public class YOPJRegConfig {
    private static final String TAG = YOPJRegConfig.class.getSimpleName();

    public static MyAccount buildAccount(Context context, MyAccount myAccount, MyApp myApp, String id, String msg, boolean isSipServiceCreated) throws UnsatisfiedLinkError {
        if (myAccount != null) {
            return myAccount;
        }
        AccountConfig accCfg = new AccountConfig();
        accCfg.setIdUri(id);
        accCfg.getRegConfig().setTimeoutSec(com.yo.android.pjsip.YoSipService.EXPIRE);
        accCfg.getNatConfig().setIceEnabled(true);
        accCfg.getVideoConfig().setAutoTransmitOutgoing(true);
        accCfg.getVideoConfig().setAutoShowIncoming(true);
        accCfg.getNatConfig().setTurnEnabled(true);
        //TODO: Add turn server and users details
        if (myApp == null) {
            isSipServiceCreated = startSipService(context,myApp);
        }
        DialerLogs.messageW(TAG, msg + " Setting TURN server");
        return myApp.addAcc(accCfg);
    }

    private static boolean startSipService(Context context, MyApp myApp) {
        myApp = new MyApp();
        myApp.init(new YOCallNotifiy(), context.getFilesDir().getAbsolutePath());
        return true;
    }

    public static void updateSIPConfig(MyAccount myAccount, SipProfile sipProfile, String username, String displayname) {
        String id = DialerHelper.getURI(displayname, username, sipProfile.getDomain());
        String registrar = DialerHelper.getRegister(sipProfile.getDomain());
        String proxy = DialerHelper.getProxy(sipProfile.getDomain());
        String password = sipProfile.getPassword();
        if (myAccount != null) {
            DialerLogs.messageI(TAG, "Sip URI " + id);
            configAccount(myAccount.cfg, id, registrar, proxy, username, password);
            try {
                myAccount.cfg.getRegConfig().setTimeoutSec(com.yo.android.pjsip.YoSipService.EXPIRE);
                myAccount.modify(myAccount.cfg);
            } catch (Exception e) {
                DialerLogs.messageE(TAG, e.getMessage());
            }
        } else {
            DialerLogs.messageE(TAG, "Created account object is null");
        }
    }

    private static void configAccount(AccountConfig accCfg, String acc_id, String registrar, String proxy,
                                      String username, String password) {

        accCfg.setIdUri(acc_id);
        accCfg.getRegConfig().setRegistrarUri(registrar);
        AuthCredInfoVector creds = accCfg.getSipConfig().getAuthCreds();
        creds.clear();
        if (username != null && !username.isEmpty() && username.length() != 0) {
            creds.add(new AuthCredInfo("Digest", "*", username, 0, password));
        }
        StringVector proxies = accCfg.getSipConfig().getProxies();
        //TODO: Adding proxy support;
        /*
        StringVector proxies = new StringVector();
        proxies.add("sip:173.82.147.172:5060;transport=tcp");
        */
        accCfg.getSipConfig().setProxies(proxies);
        proxies.clear();
        if (proxy.length() != 0) {
            proxies.add(proxy);
        }
        /* Enable ICE */
        accCfg.getNatConfig().setIceEnabled(true);
    }

}

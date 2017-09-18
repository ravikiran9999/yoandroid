package com.yo.dialer.yopj;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.util.Constants;
import com.yo.dialer.CallExtras;
import com.yo.dialer.DialerConfig;
import com.yo.dialer.DialerHelper;
import com.yo.dialer.DialerLogs;
import com.yo.dialer.YoSipService;
import com.yo.dialer.model.SipProperties;

/**
 * Created by Rajesh Babu on 17/7/17.
 */

public class YoSipServiceHandler implements SipServicesListener {

    protected PreferenceEndPoint preferenceEndPoint;

    private static final String TAG = YoSipServiceHandler.class.getSimpleName();
    public static final String START = "Start";

    private static YoSipServiceHandler instance;
    private YoAccount yoAccount;

    public static YoApp getYoApp() {
        return yoApp;
    }

    private static YoApp yoApp;
    private String displayname;
    private static YoCallObserver yoCallObserver;
    private static Context mContext;

    public static YoSipServiceHandler getInstance(Context context, PreferenceEndPoint preferenceEndPoints) {
        mContext = context;
        if (instance == null) {
            instance = new YoSipServiceHandler();
            instance.preferenceEndPoint = preferenceEndPoints;
            yoApp = new YoApp();
            yoCallObserver = YoCallObserver.getInstance(context);
            DialerLogs.messageI(TAG, "YO========INIT===========");
            yoApp.init(instance.getSIPProperties(), yoCallObserver, context.getFilesDir().getAbsolutePath());
        }
        return instance;
    }

    @Override
    public YoAccount addAccount(Context context) {
        DialerLogs.messageI(TAG, "YO========addAccount===========");
        SipProperties properties = getSIPProperties();
        if (properties.isDisplayName()) {
            displayname = DialerHelper.getInstance(mContext).parsePhoneNumber(properties.getSipUsername());
        } else {
            displayname = properties.getSipUsername();
        }
        String id = DialerHelper.getInstance(mContext).getURI(displayname, properties.getSipUsername(), properties.getSipServer());
        DialerLogs.messageI(TAG, "YO========While adding account, new URI format " + id);
        try {
            yoAccount = YoPJRegConfig.buildAccount(context, properties, yoAccount, yoApp, id, START);
            YoPJRegConfig.updateSIPConfig(yoAccount, properties);
        } catch (Exception | UnsatisfiedLinkError e) {
            DialerLogs.messageE(TAG, e.getMessage());
        }
        return yoAccount;
    }

    @Override
    public void acceptCall() {
        if (mContext != null) {
            if (mContext instanceof YoSipService) {
                ((YoSipService) mContext).acceptCall();
            }
        }
    }

    @Override
    public void rejectCall() {
        if (mContext != null) {
            if (mContext instanceof YoSipService) {
                ((YoSipService) mContext).rejectCall();
            }
        }
    }

    @Override
    public int getCallDurationInSec() {
        if (mContext != null) {
            if (mContext instanceof YoSipService) {
                return ((YoSipService) mContext).getCallDurationInSec();
            }
        }
        return 0;
    }


    @Override
    public void setMic(boolean flag) {
        if (mContext != null) {
            if (mContext instanceof YoSipService) {
                ((YoSipService) mContext).setMic(flag);
            }
        }
    }

    @Override
    public void setHold(boolean flag) {
        DialerLogs.messageE(TAG, mContext + " Setting call hold " + flag);
        if (mContext != null) {
            if (mContext instanceof YoSipService) {
                ((YoSipService) mContext).setHold(flag);
            }
        }
    }

    @Override
    public void callDisconnected(String reason) {
        Intent action = new Intent(CallExtras.Actions.COM_YO_ACTION_CLOSE);
        action.putExtra("Reason", reason);
        sendAction(action);
    }

    public void sendAction(Intent action) {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(mContext);
        localBroadcastManager.sendBroadcast(action);
    }

    @Override
    public void updateWithCallStatus(int callState) {
        Intent intent = new Intent(CallExtras.Actions.COM_YO_ACTION_CALL_UPDATE_STATUS);
        intent.putExtra(CallExtras.CALL_STATE, callState);
        sendAction(intent);
    }

    public int getRegistersCount() {
        return yoApp.accList.size();
    }


    private SipProperties getSIPProperties() {
        return withNexgeSettings();
    }

    private SipProperties withNexgeSettings() {
        String username = preferenceEndPoint.getStringPreference(Constants.VOX_USER_NAME, null);
        String password = preferenceEndPoint.getStringPreference(Constants.PASSWORD, null);
        return new SipProperties.Builder().withICEEnable(false)
                .withTurnEnable(true)
                .withStunServer(DialerConfig.STUN_SERVER)
                .withTurnServer(DialerConfig.TURN_SERVER)
                .withTurnServerUsername(DialerConfig.USERNAME)
                .withTurnServerPassword(DialerConfig.PASSWORD)
                .withSipServer(DialerConfig.NEXGE_SERVER_IP + ":" + DialerConfig.NEXGE_SERVER_TCP_PORT)
                .withProxyServer(DialerConfig.NEXGE_SERVER_IP + ":" + DialerConfig.NEXGE_SERVER_TCP_PORT + DialerConfig.TCP)
                .withVadEnable(false)
                .withDisplayName(DialerHelper.getInstance(mContext).parsePhoneNumber(username))
                .showDisplayName(true)
                .withRegister(DialerConfig.NEXGE_SERVER_IP + ":" + DialerConfig.NEXGE_SERVER_TCP_PORT)
                .withUserName(username)
                .withPassword(password)
                .build();
    }

    private SipProperties withPJSIPSettings() {
        String username = "866";  //Second user 867
        String password = "pw866"; //Second user pw867
        return new SipProperties.Builder().withICEEnable(true)
                .withTurnEnable(true)
                .withStunServer("stun.pjsip.org:5080")
                .withTurnServer("turn.pjsip.org:33478")
                .withTurnServerUsername("abzlute01")
                .withTurnServerPassword("abzlute01")
                .withSipServer("pjsip.org:5080")
                .withProxyServer("sip.pjsip.org:5080;transport=tcp;lr")
                .withVadEnable(false)
                .withDisplayName("YO")
                .showDisplayName(true)
                .withRegister("pjsip.org:5080")
                .withUserName(username)
                .withPassword(password)
                .build();
    }

    public void deleteAccount(YoAccount account) {
        DialerLogs.messageI(TAG, "UN-REGISTER ACCOUNT===========");
        yoApp.delAcc(account);
        yoApp.deinit();
    }


    public void cancelCallNotification() {
        if (mContext != null) {
            ((YoSipService) mContext).cancelCallNotification();
        }

    }
}

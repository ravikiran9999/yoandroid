package com.yo.dialer.yopj;

import android.content.Context;

import com.yo.dialer.CallExtras;
import com.yo.dialer.DialerLogs;
import com.yo.dialer.YoSipService;

import org.pjsip.pjsua2.Call;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_status_code;

/**
 * Created by Rajesh Babu on 12/7/17.
 */

public class YoCallObserver implements YoAppObserver {
    private static final String TAG = YoCallObserver.class.getSimpleName();
    private static YoCallObserver yoCallObserver;
    private static Context mContext;

    public static YoCallObserver getInstance(Context context) {
        mContext = context;
        if (yoCallObserver == null) {
            yoCallObserver = new YoCallObserver();
        }
        return yoCallObserver;
    }

    @Override
    public void notifyRegState(pjsip_status_code code, String reason, int expiration) {
        DialerLogs.messageI(TAG, "YO========notifyRegState===========" + reason + ",CODE " + code);

        String registrationStatus;
        if (expiration == 0) {
            registrationStatus = "Unregistration";
        } else {
            registrationStatus = "Registration";
        }

        if (code.swigValue() / 100 == 2) {
            registrationStatus += " successful";
        } else {
            registrationStatus += " failed: " + reason;
        }
        if (reason != null && reason.equalsIgnoreCase(CallExtras.NETWORK_NOT_REACHABLE)) {
            if (mContext instanceof YoSipService) {
                ((YoSipService) mContext).setCallStatus(CallExtras.NETWORK_NOT_REACHABLE);
            }
        } else {
            ((YoSipService) mContext).setCallStatus(registrationStatus);
        }
        DialerLogs.messageI(TAG, "YO========notifyRegState>>>> " + registrationStatus);
    }

    @Override
    public void notifyIncomingCall(YoCall call) {
        try {
            DialerLogs.messageI(TAG, "YO========notifyIncomingCall===========");
            if (mContext != null) {
                if (mContext instanceof YoSipService) {
                    ((YoSipService) mContext).OnIncomingCall(call);
                }
            }
        } catch (Exception e) {
            DialerLogs.messageI(TAG, "YO========notifyIncomingCall===========" + e.getMessage());
        }
    }

    @Override
    public void notifyCallState(YoCall call) {
        try {
            DialerLogs.messageI(TAG, "YO========notifyCallState===========State =" + call.getInfo().getState() + ",Reason" + call.getInfo().getLastReason());
            if (call.getInfo().getState() == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
                if (mContext instanceof YoSipService) {
                    ((YoSipService) mContext).setCurrentCallToNull();
                }
            }
        } catch (Exception e) {
            DialerLogs.messageI(TAG, "YO========notifyCallState===========" + e.getMessage());
        }
    }

    @Override
    public void notifyCallMediaState(YoCall call) {
        try {
            DialerLogs.messageI(TAG, "YO========notifyCallMediaState===========" + call.getInfo().getLastReason());
        } catch (Exception e) {
            DialerLogs.messageI(TAG, "YO========notifyCallMediaState===========" + e.getMessage());
        }
    }

    @Override
    public void notifyBuddyState(YoBuddy buddy) {
        DialerLogs.messageI(TAG, "YO========notifyBuddyState===========");
    }
}

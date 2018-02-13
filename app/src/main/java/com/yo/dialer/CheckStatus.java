package com.yo.dialer;


import android.os.Handler;
import android.os.Looper;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.util.Constants;
import com.yo.dialer.yopj.YoCall;
import com.yo.feedback.AppFailureReport;

import java.util.Date;

/**
 * Created by root on 30/8/17.
 */

public class CheckStatus {
    private static Handler handler = new Handler();
    private static final int DELAY = 1000;
    private static final int CALL_STATE_CHECK_DELAY = 5 * 1000;

    private static final String TAG = CheckStatus.class.getSimpleName();

    public static void registration(final YoSipService yoSipService, final PreferenceEndPoint preferenceEndPoint) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String username = preferenceEndPoint.getStringPreference(Constants.VOX_USER_NAME, null);

                if (yoSipService != null && username != null) {
                    String registrationStatus = yoSipService.getPreferenceEndPoint().getStringPreference(CallExtras.REGISTRATION_STATUS_MESSAGE);
                    if (!registrationStatus.equalsIgnoreCase(CallExtras.REGISTRATION_SUCCESS)) {
                        yoSipService.register(null);
                    }
                    long time = System.currentTimeMillis();
                    int seconds = new Date(time).getSeconds();
                    int sleepSecs = 60 - seconds;
                    handler.postDelayed(this, sleepSecs * 1000);
                }
            }
        };
        handler.postDelayed(runnable, DELAY);
    }

    public static void callStateBasedOnRTP(final YoSipService yoSipService) {
        if (yoSipService != null) {
            Runnable statsCheckRunnable = new Runnable() {
                @Override
                public void run() {
                    final YoCall yoCurrentCall = yoSipService.getYoCurrentCall();
                    DialerLogs.messageE(TAG, "Call State Based on RTP  runnable triggered." + yoCurrentCall);
                    if (yoCurrentCall != null && yoCurrentCall.isActive()) {
                        try {
                            long rxStatBytes = yoCurrentCall.getStreamStat(0).getRtcp().getRxStat().getBytes();
                            long txStatBytes = yoCurrentCall.getStreamStat(0).getRtcp().getTxStat().getBytes();
                            DialerLogs.messageW(TAG, "Incoming packets = " + rxStatBytes);
                            DialerLogs.messageW(TAG, "Outgoing packets = " + txStatBytes);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        handler.removeCallbacks(this);
                        AppFailureReport.sendDetails("While checking Call state alive or not based on RTP papckets, yo current call object is null, nothing doing further. May be call disconnected. Removing triggering stats check again after 5sec.");
                    }

                }
            };
            handler.postDelayed(statsCheckRunnable, CALL_STATE_CHECK_DELAY);
        } else {
            AppFailureReport.sendDetails("While checking Call state alive or not based on RTP papckets, SipService call object is null, nothing doing further.");
        }
    }

}

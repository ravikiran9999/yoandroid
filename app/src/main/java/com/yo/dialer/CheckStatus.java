package com.yo.dialer;


import android.os.Handler;

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

    public static void registration(final YoSipService yoSipService) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (yoSipService != null) {
                    String registrationStatus = yoSipService.getPreferenceEndPoint().getStringPreference(CallExtras.REGISTRATION_STATUS_MESSAGE);
                    if (!registrationStatus.equalsIgnoreCase(CallExtras.REGISTRATION_SUCCESS)) {
                        yoSipService.register();
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

    public static void callStateBasedOnRTP(YoSipService yoSipService) {
        if (yoSipService != null) {
            final YoCall yoCurrentCall = yoSipService.getYoCurrentCall();
            Runnable statsCheckRunnable = new Runnable() {
                @Override
                public void run() {
                    DialerLogs.messageE(TAG, "Call State Based on RTP  runnable triggered.");
                    try {
                        if (yoCurrentCall != null) {
                            long rxStatBytes = yoCurrentCall.getStreamStat(0).getRtcp().getRxStat().getBytes();
                            long txStatBytes = yoCurrentCall.getStreamStat(0).getRtcp().getTxStat().getBytes();
                            DialerLogs.messageW(TAG, "Incoming packets = " + rxStatBytes);
                            DialerLogs.messageW(TAG, "Outgoing packets = " + rxStatBytes);
                        } else {
                            handler.removeCallbacks(this);
                            AppFailureReport.sendDetails("While checking Call state alive or not based on RTP papckets, yo current call object is null, nothing doing further. May be call disconnected. Removing triggering stats check again after 5sec.");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            };
            handler.postDelayed(statsCheckRunnable, CALL_STATE_CHECK_DELAY);
        } else {
            AppFailureReport.sendDetails("While checking Call state alive or not based on RTP papckets, SipService call object is null, nothing doing further.");
        }
    }

}

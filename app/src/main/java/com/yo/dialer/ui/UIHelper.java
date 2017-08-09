package com.yo.dialer.ui;

import android.content.Context;
import android.text.format.DateUtils;

import com.yo.android.R;
import com.yo.dialer.CallExtras;
import com.yo.dialer.DialerLogs;

/**
 * Created by root on 31/7/17.
 */

class UIHelper {
    private static final String TAG = UIHelper.class.getSimpleName();
    private static CallBaseActivity activity = null;

    public static Runnable getTimer(Context context) {
        if (context instanceof CallBaseActivity) {
            activity = (CallBaseActivity) context;
        }
        return new Runnable() {
            @Override
            public void run() {

             /*   DialerLogs.messageE(TAG, "YO====isCallStopped" + !activity.isCallStopped);

                if (!activity.isCallStopped) {
                    activity.mHandler.postDelayed(this, 1000L);
                }
                if (activity.sipBinder != null) {
                    int seconds = activity.sipBinder.getYOHandler().getCallDurationInSec();
                    StringBuilder mRecycle = new StringBuilder(8);
                    String text = DateUtils.formatElapsedTime(mRecycle, seconds);
                    activity.durationTxtview.setText(text);
                    String callState = activity.sipBinder.getYOHandler().getCallState();
                    if (callState == null) {
                        //seems call got disconnected.
                        DialerLogs.messageE(TAG, "YO====Seems call got disconnected");
                        activity.connectionStatusTxtView.setText(activity.getResources().getString(R.string.disconnect_status));
                        activity.finish();
                    } else {
                        DialerLogs.messageE(TAG, "YO====Call State " + callState);

                        if (callState.equalsIgnoreCase(CallExtras.CONFIRMED)) {
                            activity.connectionStatusTxtView.setText(activity.getResources().getString(R.string.connected_status));
                        } else if (callState.equalsIgnoreCase(CallExtras.CONNECTING)) {
                            activity.connectionStatusTxtView.setText(activity.getResources().getString(R.string.connecting_status));
                        } else if (callState.equalsIgnoreCase(CallExtras.DISCONNECTED)) {
                            activity.isCallStopped = true;
                            DialerLogs.messageE(TAG, "YO====Seems call got disconnected");
                            activity.connectionStatusTxtView.setText(activity.getResources().getString(R.string.disconnect_status));
                            activity.finish();
                        } else if (!callState.equalsIgnoreCase(CallExtras.REGISTRATION_SUCCESS)) {
                            activity.connectionStatusTxtView.setText(activity.getResources().getString(R.string.reconnecting_status));
                        }
                    }
                }*/
            }
        };
    }
}

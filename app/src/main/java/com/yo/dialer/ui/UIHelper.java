package com.yo.dialer.ui;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.yo.android.R;
import com.yo.dialer.CallExtras;
import com.yo.dialer.DialerLogs;

/**
 * Created by root on 31/7/17.
 */

class UIHelper {
    private static final String TAG = UIHelper.class.getSimpleName();
    private static CallBaseActivity activity = null;

    public static Runnable getDurationRunnable(Context context) {
        if (context instanceof CallBaseActivity) {
            activity = (CallBaseActivity) context;
        }
        return durationRunnable;
    }


    private static Runnable durationRunnable = new Runnable() {
        @Override
        public void run() {
            if (!activity.isCallStopped) {
                activity.mHandler.postDelayed(this, 1000L);
            }
            if (activity.sipBinder != null) {
                int seconds = activity.sipBinder.getYOHandler().getCallDurationInSec();
                StringBuilder mRecycle = new StringBuilder(8);
                String text = DateUtils.formatElapsedTime(mRecycle, seconds);
                activity.durationTxtview.setText(text);
            }
        }
    };


    public static void handleCallStatus(Context context, boolean isIncoming, final int callStatus, TextView tvCallStatus, final TextView connectionStatusTxtView) {
        connectionStatusTxtView.setVisibility(View.VISIBLE);
        tvCallStatus.setVisibility(View.VISIBLE);
        if (callStatus == CallExtras.StatusCode.YO_INV_STATE_SC_CALLING) {
            connectionStatusTxtView.setText(context.getResources().getString(R.string.calling));
            tvCallStatus.setText(context.getResources().getString(R.string.calling));
            DialerLogs.messageI(TAG, "YO====handleCallStatus====CALLING....");
        } else if (callStatus == CallExtras.StatusCode.YO_INV_STATE_SC_RINGING) {
            DialerLogs.messageI(TAG, "YO====handleCallStatus====RINGING....");
            if (isIncoming) {
                connectionStatusTxtView.setText(context.getResources().getString(R.string.incoming));
                tvCallStatus.setText(context.getResources().getString(R.string.incoming));
            } else {
                connectionStatusTxtView.setText(context.getResources().getString(R.string.ringing));
                tvCallStatus.setText(context.getResources().getString(R.string.ringing));
            }
        } else if (callStatus == CallExtras.StatusCode.YO_INV_STATE_CONNECTED) {
            connectionStatusTxtView.setText(context.getResources().getString(R.string.connected_status));
            tvCallStatus.setText(context.getResources().getString(R.string.connected_status));
        } else if (callStatus == CallExtras.StatusCode.YO_INV_STATE_SC_RE_CONNECTING) {
            connectionStatusTxtView.setText(context.getResources().getString(R.string.reconnecting_status));
            tvCallStatus.setText(context.getResources().getString(R.string.reconnecting_status));
        } else if (callStatus == CallExtras.StatusCode.YO_CALL_MEDIA_REMOTE_HOLD) {
            connectionStatusTxtView.setText(context.getResources().getString(R.string.call_on_hold_status));
            tvCallStatus.setText(context.getResources().getString(R.string.call_on_hold_status));
        } else if (callStatus == CallExtras.StatusCode.YO_CALL_MEDIA_LOCAL_HOLD) {
            connectionStatusTxtView.setText(context.getResources().getString(R.string.call_on_hold_status));
            tvCallStatus.setText(context.getResources().getString(R.string.call_on_hold_status));
        } else if (callStatus == CallExtras.StatusCode.YO_CALL_NETWORK_NOT_REACHABLE) {
            connectionStatusTxtView.setText(context.getResources().getString(R.string.reconnecting_status));
            tvCallStatus.setText(context.getResources().getString(R.string.reconnecting_status));
        } else if (callStatus == CallExtras.StatusCode.YO_INV_STATE_SC_CONNECTING) {
            connectionStatusTxtView.setText(context.getResources().getString(R.string.connecting_status));
            tvCallStatus.setText(context.getResources().getString(R.string.connecting_status));
        } else if (callStatus == CallExtras.StatusCode.YO_BUSY_HERE) {

            Toast.makeText(context, context.getResources().getString(R.string.busy), Toast.LENGTH_LONG).show();
        } else if (callStatus == CallExtras.StatusCode.YO_INV_STATE_SC_UNKNOWN) {
            if (isIncoming) {
                connectionStatusTxtView.setText(context.getResources().getString(R.string.incoming));
                tvCallStatus.setText(context.getResources().getString(R.string.incoming));
            } else {
                connectionStatusTxtView.setText(context.getResources().getString(R.string.calling));
                tvCallStatus.setText(context.getResources().getString(R.string.calling));
            }
        }
    }
}

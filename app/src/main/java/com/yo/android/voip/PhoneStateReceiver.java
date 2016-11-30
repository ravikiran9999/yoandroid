package com.yo.android.voip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import de.greenrobot.event.EventBus;

/**
 * Created by rajesh on 28/11/16.
 */
public class PhoneStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        //Toast.makeText(context, state + incomingNumber, Toast.LENGTH_SHORT).show();

        if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            EventBus.getDefault().post(100);
            // Getting call
            //Toast.makeText(context, "Ringing State Number is -" + incomingNumber, Toast.LENGTH_SHORT).show();
        }
        if ((state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))) {
            //call accepted
            //Toast.makeText(context, "Received State", Toast.LENGTH_SHORT).show();
        }
        if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
            EventBus.getDefault().post(101);

            // Toast.makeText(context, "Idle State", Toast.LENGTH_SHORT).show();
            //Call disconnected
        }
    }
}

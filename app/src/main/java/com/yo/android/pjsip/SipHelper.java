package com.yo.android.pjsip;

import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.yo.android.ui.NewDailerActivity;
import com.yo.android.voip.OutGoingCallActivity;
import com.yo.android.voip.VoipConstants;
import com.yo.dialer.CallExtras;
import com.yo.dialer.DialerConfig;
import com.yo.dialer.DialerLogs;

/**
 * Created by Ramesh on 14/8/16.
 */
public class SipHelper {
    //For pstn calls if user enter + or 0091 the dailer should show the same number, but call will use the number after removing + and 00
    private static String actualNumber;
    private static final String TAG = SipHelper.class.getSimpleName();

    public static void init(String number) {
        actualNumber = number;
    }

    public static void makeCall(Context mContext, String number, boolean isPSTN) {

        Intent intent;
        DialerLogs.messageI(TAG, "Phone Number while making call " + number);

        if (DialerConfig.IS_NEW_SIP) {
            intent = new Intent(CallExtras.MAKE_CALL, null, mContext, com.yo.dialer.YoSipService.class);
            intent.putExtra(CallExtras.CALLER_NO, number);
            intent.putExtra(CallExtras.IS_PSTN, isPSTN);
        } else {
            intent = new Intent(VoipConstants.CALL_ACTION_OUT_GOING, null, mContext, YoSipService.class);
            intent.putExtra(OutGoingCallActivity.CALLER_NO, number);
            intent.putExtra(VoipConstants.PSTN, isPSTN);
        }

        if (actualNumber == null) {
            actualNumber = number;
        }
        intent.putExtra(OutGoingCallActivity.DISPLAY_NUMBER, actualNumber);
        //actualNumber = null;
        mContext.startService(intent);

    }
}

package com.yo.android.pjsip;

import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.yo.android.ui.NewDailerActivity;
import com.yo.android.voip.OutGoingCallActivity;
import com.yo.android.voip.VoipConstants;

/**
 * Created by Ramesh on 14/8/16.
 */
public class SipHelper {
    //For pstn calls if user enter + or 0091 the dailer should show the same number, but call will use the number after removing + and 00
    private static String actualNumber;

    public static void init(String number) {
        actualNumber = number;
    }

    public static void makeCall(Context mContext, String number) {

        Intent intent = new Intent(VoipConstants.CALL_ACTION_OUT_GOING, null, mContext, YoSipService.class);
        if (mContext instanceof NewDailerActivity) {
            intent.putExtra(VoipConstants.PSTN, true);
        }
        intent.putExtra(OutGoingCallActivity.CALLER_NO, number);
        if (actualNumber == null) {
            actualNumber = number;
        }
        intent.putExtra(OutGoingCallActivity.DISPLAY_NUMBER, actualNumber);
        //actualNumber = null;
        mContext.startService(intent);

    }
}

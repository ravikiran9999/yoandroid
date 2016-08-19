package com.yo.android.pjsip;

import android.content.Context;
import android.content.Intent;

import com.yo.android.voip.OutGoingCallActivity;
import com.yo.android.voip.VoipConstants;

/**
 * Created by Ramesh on 14/8/16.
 */
public class SipHelper {

    public static void makeCall(Context mContext, String number) {
//        Intent intent = new Intent(mContext, OutGoingCallActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.putExtra(OutGoingCallActivity.CALLER_NO, number);
//        mContext.startActivity(intent);

        Intent intent = new Intent(VoipConstants.CALL_ACTION_OUT_GOING, null, mContext, YoSipService.class);
        intent.putExtra(OutGoingCallActivity.CALLER_NO, number);
        mContext.startService(intent);
    }

}

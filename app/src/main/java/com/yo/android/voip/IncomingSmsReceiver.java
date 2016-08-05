package com.yo.android.voip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import de.greenrobot.event.EventBus;

public class IncomingSmsReceiver extends BroadcastReceiver {

    private static final String TAG = "IncomingSms";

    public void onReceive(Context context, Intent intent) {
        // Retrieves a map of extended data from the intent.
        String intentAction = intent.getAction();
        Log.d("Action is ", intentAction);
        final Bundle bundle = intent.getExtras();
        //extractOTP(bundle);
        EventBus.getDefault().post(bundle);
    }

    public static String extractOTP(Bundle bundle) {
        try {
            if (bundle != null) {
                Object[] pdusObj = (Object[]) bundle.get("pdus");
                for (Object aPdusObj : pdusObj) {
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) aPdusObj);
                    String senderAddress = currentMessage.getDisplayOriginatingAddress();
                    String message = currentMessage.getDisplayMessageBody();

                    Log.e(TAG, "Received SMS: " + message + ", Sender: " + senderAddress);

                    // if the SMS is not from our gateway, ignore the message
                    if (senderAddress.toLowerCase().contains("020001")
                            || senderAddress.equalsIgnoreCase("MD-Beepse")
                            ) {
                        // verification code from sms
                        String verificationCode = getVerificationCode(message);
                        Log.e(TAG, "OTP received: " + verificationCode);
                        return verificationCode;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
        return null;
    }

    /**
     * Getting the OTP from sms message body
     * ':' is the separator of OTP from the message
     *
     * @param message
     * @return
     */
    private static String getVerificationCode(String message) {
        String code = null;
        int index = message.indexOf(":");

        if (index != -1) {
            int start = index + 2;
            int length = 6;
            code = message.substring(start, start + length);
            return code;
        }

        return code;
    }
}
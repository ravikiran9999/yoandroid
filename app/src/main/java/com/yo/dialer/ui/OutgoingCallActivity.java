package com.yo.dialer.ui;

import android.os.Bundle;

import com.yo.android.R;
import com.yo.dialer.DialerHelper;
import com.yo.dialer.DialerLogs;

/**
 * Created by Rajesh Babu on 29/7/17.
 */

public class OutgoingCallActivity extends CallBaseActivity {
    private static final String TAG = OutgoingCallActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialer_outgoing_call);
        DialerLogs.messageI(TAG, "YO========Incoming call screen=====");
        setContentView(R.layout.dialer_received_call);
        DialerLogs.messageI(TAG, "YO==== Callee Number====" + callePhoneNumber);
        initViews();
        loadCalleImage(calleImageView, calleImageUrl);
        loadCalleeName(calleNameTxt, calleName);
        loadCallePhoneNumber(callePhoneNumberTxt, DialerHelper.getInstance(this).parsePhoneNumber(callePhoneNumber));
        //to show callee yo chat
        //callMessageBtn.setTag(callePhoneNumber);
    }

    private void initViews() {

    }

}

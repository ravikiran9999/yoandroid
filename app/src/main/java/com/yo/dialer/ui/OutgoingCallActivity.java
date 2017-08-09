package com.yo.dialer.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.dialer.DialerHelper;
import com.yo.dialer.DialerLogs;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Rajesh Babu on 29/7/17.
 */

public class OutgoingCallActivity extends CallBaseActivity {
    private static final String TAG = OutgoingCallActivity.class.getSimpleName();
    private View mOutgoingCallHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialer_outgoing_call);
        DialerLogs.messageI(TAG, "YO========Outgongcall call screen=====");
        DialerLogs.messageI(TAG, "YO==== Callee Number====" + callePhoneNumber);
        initViews();
        loadCalleImage(calleImageView, calleImageUrl);
        loadCalleeName(calleNameTxt, calleName);
        loadCallePhoneNumber(callePhoneNumberTxt, DialerHelper.getInstance(this).parsePhoneNumber(callePhoneNumber));
        //to show callee yo chat
        //callMessageBtn.setTag(callePhoneNumber);
    }

    private void initViews() {
        mOutgoingCallHeader = findViewById(R.id.header_outgoing_call);
        calleImageView = (CircleImageView) mOutgoingCallHeader.findViewById(R.id.imv_caller_pic);
        calleNameTxt = (TextView) mOutgoingCallHeader.findViewById(R.id.tv_caller_name);
        callePhoneNumberTxt = (TextView) mOutgoingCallHeader.findViewById(R.id.tv_caller_number);
        fullImageLayout = (RelativeLayout) findViewById(R.id.full_image_layout);
    }

}

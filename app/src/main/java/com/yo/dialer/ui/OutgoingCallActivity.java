package com.yo.dialer.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.dialer.DialerHelper;
import com.yo.dialer.DialerLogs;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Rajesh Babu on 29/7/17.
 */

public class OutgoingCallActivity extends CallBaseActivity implements View.OnClickListener{
    private static final String TAG = OutgoingCallActivity.class.getSimpleName();
    private View mOutgoingCallHeader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialer_received_call);
        DialerLogs.messageI(TAG, "YO========Outgongcall call screen=====");
        DialerLogs.messageI(TAG, "YO==== Callee Number====" + callePhoneNumber);
        initViews();
        loadUserDetails();
        tvCallStatus.setText(getResources().getString(R.string.calling));

        //TODO: Need to check edge cases for Incoming and Outgoing calls
        loadCallePhoneNumber(callePhoneNumberTxt, callePhoneNumber);
        //to show callee yo chat
        //callMessageBtn.setTag(callePhoneNumber);
    }


    private void initViews() {
        mOutgoingCallHeader = findViewById(R.id.incoming_call_header);
        mAcceptedCallHeader = findViewById(R.id.received_call_header);
        mAcceptedCallHeader.setVisibility(View.GONE);

        calleImageView = (CircleImageView) mOutgoingCallHeader.findViewById(R.id.imv_caller_pic);
        calleNameTxt = (TextView) mOutgoingCallHeader.findViewById(R.id.tv_caller_name);
        callePhoneNumberTxt = (TextView) mOutgoingCallHeader.findViewById(R.id.tv_caller_number);
        fullImageLayout = (RelativeLayout) findViewById(R.id.full_image_layout);
        tvCallStatus = (TextView) mOutgoingCallHeader.findViewById(R.id.tv_incoming);


        //Accepted call
        acceptedCalleImageView = (CircleImageView) mAcceptedCallHeader.findViewById(R.id.imv_caller_pic);
        acceptedcalleNameTxt = (TextView) mAcceptedCallHeader.findViewById(R.id.tv_caller_name);
        acceptedcallePhoneNumberTxt = (TextView) mAcceptedCallHeader.findViewById(R.id.tv_caller_number);
        connectionStatusTxtView = (TextView) mAcceptedCallHeader.findViewById(R.id.connection_status);
        durationTxtview = (TextView) mAcceptedCallHeader.findViewById(R.id.tv_call_duration);

        callAcceptBtn = (ImageView) findViewById(R.id.btnAcceptCall);
        callRejectBtn = (ImageView) findViewById(R.id.btnRejectCall);
        callMessageBtn = (ImageView) findViewById(R.id.btnMessageIncoming);
        callSpeakerBtn = (ImageView) findViewById(R.id.btnSpeaker);
        callMicBtn = (ImageView) findViewById(R.id.btnMICOff);
        callEndBtn = (ImageView) findViewById(R.id.btnEndCall);


        hideAcceptAndMessage();

        callSpeakerView = (ImageView) findViewById(R.id.imv_speaker);
        callSpeakerView.setTag(true);
        callMuteView = (ImageView) findViewById(R.id.imv_mic_off);
        callMuteView.setTag(true);
        callHoldView = (ImageView) findViewById(R.id.btnHold);
        callHoldView.setTag(true);

        registerListerners();

    }
    private void registerListerners() {
        callAcceptBtn.setOnClickListener(this);
        callRejectBtn.setOnClickListener(this);
        callMessageBtn.setOnClickListener(this);
        callHoldView.setOnClickListener(this);
        callMuteView.setOnClickListener(this);
        callSpeakerView.setOnClickListener(this);
        callEndBtn.setOnClickListener(this);
    }
    private void hideAcceptAndMessage() {
        callAcceptBtn.setVisibility(View.GONE);
        callMessageBtn.setVisibility(View.GONE);
        callSpeakerBtn.setVisibility(View.VISIBLE);
        callMicBtn.setVisibility(View.VISIBLE);
        callEndBtn.setVisibility(View.VISIBLE);
        callRejectBtn.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRejectCall:
            case R.id.btnEndCall:
                rejectCall();
                break;
            case R.id.btnMessageIncoming:
                //if call is not accepted it should display busy messages otherwise it should display yo chat.
                //showYoChat((String) v.getTag());
                break;
            case R.id.imv_speaker:
                toggerSpeaker(v);
                break;
            case R.id.imv_mic_off:
                toggleMic(v);
                break;
            case R.id.btnHold:
                toggleHold(v);
                break;
        }

    }
}

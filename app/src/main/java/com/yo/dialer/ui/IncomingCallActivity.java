package com.yo.dialer.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.voip.InComingCallActivity;
import com.yo.dialer.DialerHelper;
import com.yo.dialer.DialerLogs;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Rajesh Babu Polamarasetti on 22/7/17.
 */

public class IncomingCallActivity extends CallBaseActivity implements View.OnClickListener {
    private static final String TAG = InComingCallActivity.class.getSimpleName();

   //After accepting call
    private CircleImageView acceptedCalleImageView;
    private TextView acceptedcalleNameTxt;
    private TextView acceptedcallePhoneNumberTxt;


    private View mInComingHeader;
    private View mReceivedCallHeader;


    //YO Call buttons;
    private ImageView callAcceptBtn;
    private ImageView callRejectBtn;
    private ImageView callMessageBtn;

    private ImageView callSpeakerView;
    private ImageView callMuteView;
    private ImageView callHoldView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DialerLogs.messageI(TAG, "YO========Incoming call screen=====");
        setContentView(R.layout.dialer_received_call);
        DialerLogs.messageI(TAG, "YO==== Callee Number====" + callePhoneNumber);
        initViews();
        loadCalleImage(calleImageView, calleImageUrl);
        loadCalleeName(calleNameTxt, calleName);
        loadCallePhoneNumber(callePhoneNumberTxt, DialerHelper.getInstance(this).parsePhoneNumber(callePhoneNumber));
        //to show callee yo chat
        callMessageBtn.setTag(callePhoneNumber);

    }

    private void initViews() {
        mReceivedCallHeader = findViewById(R.id.received_call_header);
        mInComingHeader = findViewById(R.id.incoming_call_header);

        //Incoming
        calleImageView = (CircleImageView) mInComingHeader.findViewById(R.id.imv_caller_pic);
        calleNameTxt = (TextView) mInComingHeader.findViewById(R.id.tv_caller_name);
        callePhoneNumberTxt = (TextView) mInComingHeader.findViewById(R.id.tv_caller_number);
        mReceivedCallHeader.setVisibility(View.GONE);

        //Accepted call
        acceptedCalleImageView = (CircleImageView) mReceivedCallHeader.findViewById(R.id.imv_caller_pic);
        acceptedcalleNameTxt = (TextView) mReceivedCallHeader.findViewById(R.id.tv_caller_name);
        acceptedcallePhoneNumberTxt = (TextView) mReceivedCallHeader.findViewById(R.id.tv_caller_number);
        connectionStatusTxtView = (TextView) mReceivedCallHeader.findViewById(R.id.connection_status);
        durationTxtview = (TextView) mReceivedCallHeader.findViewById(R.id.tv_call_duration);

        fullImageLayout = (RelativeLayout) findViewById(R.id.full_image_layout);

        callAcceptBtn = (ImageView) findViewById(R.id.btnAcceptCall);
        callRejectBtn = (ImageView) findViewById(R.id.btnRejectCall);
        callMessageBtn = (ImageView) findViewById(R.id.btnMessageIncoming);

        callSpeakerView = (ImageView) findViewById(R.id.imv_speaker);
        callSpeakerView.setTag(false);
        callHoldView = (ImageView) findViewById(R.id.imv_mic_off);
        callHoldView.setTag(false);
        callMuteView = (ImageView) findViewById(R.id.btnHold);
        callMuteView.setTag(false);

        registerListerners();
    }

    private void registerListerners() {
        callAcceptBtn.setOnClickListener(this);
        callRejectBtn.setOnClickListener(this);
        callMessageBtn.setOnClickListener(this);
        callHoldView.setOnClickListener(this);
        callMuteView.setOnClickListener(this);
        callSpeakerView.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAcceptCall:
                acceptCall();
                changeToAcceptedCallUI();
                break;
            case R.id.btnRejectCall:
                rejectCall();
                break;
            case R.id.btnMessageIncoming:
                //if call is not accepted it should display busy messages otherwise it should display yo chat.
                showYoChat((String) v.getTag());
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

    private void changeToAcceptedCallUI() {
        mReceivedCallHeader.setVisibility(View.VISIBLE);
        mInComingHeader.setVisibility(View.GONE);
        loadCalleeName(acceptedcalleNameTxt, calleName);
        loadCalleImage(acceptedCalleImageView, calleImageUrl);
        loadCallePhoneNumber(acceptedcallePhoneNumberTxt, DialerHelper.getInstance(this).parsePhoneNumber(callePhoneNumber));
        isCallStopped = false;
        mHandler.post(UIHelper.getTimer(IncomingCallActivity.this));
    }

    private void showYoChat(String calleeYOUsername) {

    }
}

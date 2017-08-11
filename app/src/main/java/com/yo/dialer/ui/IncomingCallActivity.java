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

    private View mInComingHeader;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DialerLogs.messageI(TAG, "YO========Incoming call screen=====");
        setContentView(R.layout.dialer_received_call);
        setIncoming(true);
        DialerLogs.messageI(TAG, "YO==== Callee Number====" + callePhoneNumber);
        initViews();
        loadUserDetails();
        loadCallePhoneNumber(callePhoneNumberTxt, DialerHelper.getInstance(this).parsePhoneNumber(callePhoneNumber));
        tvCallStatus.setText(getResources().getString(R.string.incoming_call));
        //to show callee yo chat
        callMessageBtn.setTag(callePhoneNumber);

    }

    private void readIntentValues() {

    }

    private void initViews() {
        mAcceptedCallHeader = findViewById(R.id.received_call_header);
        mInComingHeader = findViewById(R.id.incoming_call_header);

        //Incoming
        calleImageView = (CircleImageView) mInComingHeader.findViewById(R.id.imv_caller_pic);
        calleNameTxt = (TextView) mInComingHeader.findViewById(R.id.tv_caller_name);
        callePhoneNumberTxt = (TextView) mInComingHeader.findViewById(R.id.tv_caller_number);
        fullImageLayout = (RelativeLayout) findViewById(R.id.full_image_layout);
        tvCallStatus = (TextView) mInComingHeader.findViewById(R.id.tv_incoming);
        mAcceptedCallHeader.setVisibility(View.GONE);

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

        hideMicAndSpeaker();


        callSpeakerView = (ImageView) findViewById(R.id.imv_speaker);
        callSpeakerView.setTag(true);
        callMuteView = (ImageView) findViewById(R.id.imv_mic_off);
        callMuteView.setTag(true);
        callHoldView = (ImageView) findViewById(R.id.btnHold);
        callHoldView.setTag(true);

        registerListerners();
    }

    private void hideMicAndSpeaker() {
        callMicBtn.setVisibility(View.GONE);
        callSpeakerBtn.setVisibility(View.GONE);
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


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAcceptCall:
                acceptCall();
                changeToAcceptCallUI();
                break;
            case R.id.btnRejectCall:
            case R.id.btnEndCall:
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

    private void changeToAcceptCallUI() {
        mInComingHeader.setVisibility(View.GONE);
        changeToAcceptedCallUI();
    }

    protected void acceptCall() {
        if (sipBinder != null && sipBinder.getYOHandler() != null) {
            sipBinder.getYOHandler().acceptCall();
        } else {
            DialerLogs.messageE(TAG, "YO====sipBinder == null && sipBinder.getYOHandler() ==NULL");
        }
    }

    private void showYoChat(String calleeYOUsername) {

    }
}

package com.yo.dialer.ui;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.androidadvance.topsnackbar.TSnackbar;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.chat.ui.fragments.AppContactsActivity;
import com.yo.android.util.Util;
import com.yo.dialer.DialerHelper;
import com.yo.dialer.DialerLogs;

import java.util.List;

import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Rajesh Babu on 29/7/17.
 */

/**
 * The Outgoing call screen
 */
public class OutgoingCallActivity extends CallBaseActivity implements View.OnClickListener {
    private static final String TAG = OutgoingCallActivity.class.getSimpleName();

    private RelativeLayout relative_layout_main;
    private SeekBar seekBar;
    private TSnackbar mSnackbar;
    private FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialer_received_call);

        setIncoming(false);
        DialerLogs.messageI(TAG, "YO========Outgongcall call screen=====");
        DialerLogs.messageI(TAG, "YO==== Callee Number====" + callePhoneNumber);
        initViews();
        initVolume();
        if (!TextUtils.isEmpty(callePhoneNumber) && callePhoneNumber.contains(BuildConfig.RELEASE_USER_TYPE)) {
            callePhoneNumber = DialerHelper.getInstance(this).parsePhoneNumber(callePhoneNumber);
        }
        loadUserDetails();
        tvCallStatus.setText(getResources().getString(R.string.calling));

        //If outgoing call phone number contaitns yo username it should be parse and display only phone number.

        //TODO: Need to check edge cases for Incoming and Outgoing calls
        //
        loadCallePhoneNumber(callePhoneNumberTxt, callePhoneNumber);
        //to show callee yo chat
        //callMessageBtn.setTag(callePhoneNumber);
        // if user opens outgoing call screen from notification, if the call is already is in progress just change UI to accepted call UI.
        CallControlsModel callControlsModel = CallControls.getCallControlsModel();
        if (callControlsModel != null && callControlsModel.isCallAccepted()) {
            changeToAcceptedCallUI();
            loadPreviousSettings();
        }

        if (callControlsModel != null && callControlsModel.isSpeakerOn()) {
            loadPreviousSettings();
        }
        updateCallType();
    }

    protected void onDestroy() {
        super.onDestroy();
        mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
    }


    /**
     * Initializes the views
     */
    private void initViews() {
        mOutgoingCallHeader = findViewById(R.id.incoming_call_header);
        mAcceptedCallHeader = findViewById(R.id.received_call_header);
        mAcceptedCallHeader.setVisibility(View.GONE);

        calleImageView = (CircleImageView) mOutgoingCallHeader.findViewById(R.id.imv_caller_pic);
        calleNameTxt = (TextView) mOutgoingCallHeader.findViewById(R.id.tv_caller_name);
        callePhoneNumberTxt = (TextView) mOutgoingCallHeader.findViewById(R.id.tv_caller_number);
        fullImageLayout = (RelativeLayout) findViewById(R.id.full_image_layout);
        tvCallStatus = (TextView) mOutgoingCallHeader.findViewById(R.id.tv_incoming);
        tvCallType = (TextView) mOutgoingCallHeader.findViewById(R.id.tv_incoming_call);


        //Accepted call
        acceptedCalleImageView = (CircleImageView) mAcceptedCallHeader.findViewById(R.id.imv_caller_pic);
        acceptedcalleNameTxt = (TextView) mAcceptedCallHeader.findViewById(R.id.tv_caller_name);
        acceptedcallePhoneNumberTxt = (TextView) mAcceptedCallHeader.findViewById(R.id.tv_caller_number);
        connectionStatusTxtView = (TextView) mAcceptedCallHeader.findViewById(R.id.connection_status);
        durationTxtview = (TextView) mAcceptedCallHeader.findViewById(R.id.tv_call_duration);
        tvAccepetedCallType = (TextView) mAcceptedCallHeader.findViewById(R.id.tv_call_type);


        callAcceptBtn = (ImageView) findViewById(R.id.btnAcceptCall);
        callRejectBtn = (ImageView) findViewById(R.id.btnRejectCall);
        callMessageBtn = (ImageView) findViewById(R.id.btnMessageIncoming);
        callSpeakerBtn = (ImageView) findViewById(R.id.btnSpeaker);
        callSpeakerBtn.setTag(true);
        callMicBtn = (ImageView) findViewById(R.id.btnMICOff);
        callMicBtn.setTag(true);
        callEndBtn = (ImageView) findViewById(R.id.btnEndCall);


        hideAcceptAndMessage();

        callSpeakerView = (ImageView) mAcceptedCallHeader.findViewById(R.id.imv_speaker);

        CallControlsModel callControlsModel = CallControls.getCallControlsModel();
        callSpeakerView.setTag(callControlsModel != null ? callControlsModel.isSpeakerOn() : false);
        callMuteView = (ImageView) findViewById(R.id.imv_mic_off);
        callMuteView.setTag(callControlsModel != null ? callControlsModel.isMicOn() : false);
        callHoldView = (ImageView) findViewById(R.id.btnHold);
        callHoldView.setTag(callControlsModel != null ? callControlsModel.isHoldOn() : false);


        //seekBar = (SeekBar) findViewById(R.id.seek_bar);
        relative_layout_main = (RelativeLayout) findViewById(R.id.relative_layout_main);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);

        registerListerners();
        initCircularView();
    }

    /**
     * Sets border for the callee profile pic
     */
    private void initCircularView() {
        calleImageView.setBorderColor(getResources().getColor(R.color.white));
        calleImageView.setBorderWidth(5);

        acceptedCalleImageView.setBorderColor(getResources().getColor(R.color.white));
        acceptedCalleImageView.setBorderWidth(5);
    }

    /**
     * Registers the click listeners
     */
    private void registerListerners() {
        callAcceptBtn.setOnClickListener(this);
        callRejectBtn.setOnClickListener(this);
        callMessageBtn.setOnClickListener(this);
        callHoldView.setOnClickListener(this);
        callMuteView.setOnClickListener(this);
        callSpeakerView.setOnClickListener(this);
        callEndBtn.setOnClickListener(this);
        callSpeakerBtn.setOnClickListener(this);
        callMicBtn.setOnClickListener(this);
        floatingActionButton.setOnClickListener(this);
    }

    /**
     * Hides the mic and speaker
     */
    private void hideAcceptAndMessage() {
        callAcceptBtn.setVisibility(View.GONE);
        callMessageBtn.setVisibility(View.GONE);
        callSpeakerBtn.setVisibility(View.VISIBLE);
        callMicBtn.setVisibility(View.GONE);
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
                startActivity(new Intent(this, AppContactsActivity.class));
                break;
            case R.id.imv_speaker:
                toggleSpeaker(v);
                break;
            case R.id.imv_mic_off:
                toggleMic(v);
                break;
            case R.id.btnHold:
                toggleHold(v);
                /*boolean isHold = toggleHold(v);
                if (!isHold) {
                    sipBinder.getYOHandler().setMic(false);
                    CallControls.changeSelection(v, false);
                } else {
                    sipBinder.getYOHandler().setMic(CallControls.getCallControlsModel().isMicOn());
                    CallControls.changeSelection(v, CallControls.getCallControlsModel().isMicOn());
                }*/
                break;
            case R.id.btnSpeaker:
                toggleSpeaker(v);
                break;
            case R.id.btnMICOff:
                toggleMic(v);
                break;
            case R.id.fab:
                init();
                break;
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        init();
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            int index = seekBar.getProgress();
            seekBar.setProgress(index - 1);
            return true;
        } else if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
            int index = seekBar.getProgress();
            seekBar.setProgress(index + 1);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void init() {
        if (mSnackbar == null) {
            mSnackbar = TSnackbar.make(relative_layout_main, "", TSnackbar.LENGTH_LONG);
        }
        mSnackbar.setActionTextColor(Color.WHITE);
        mSnackbar.setMaxWidth(3000);

        final TSnackbar.SnackbarLayout layout = (TSnackbar.SnackbarLayout) mSnackbar.getView();
        layout.setBackgroundColor(Color.parseColor("#FFFFFF"));

        // Inflate our custom view
        View snackView = getLayoutInflater().inflate(R.layout.custom_seekbar, null);
        seekBar = (SeekBar) snackView.findViewById(R.id.seek_bar);
        layout.addView(snackView, 0);
        mSnackbar.show();
        mSnackbar.setCallback(new TSnackbar.Callback() {
            @Override
            public void onDismissed(TSnackbar snackbar, @DismissEvent int event) {
                super.onDismissed(snackbar, event);
                mSnackbar = null;
            }
        });

        // volume controller
        //AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (am != null) {
            Util.initBar(seekBar, am, AudioManager.STREAM_VOICE_CALL);
        }
    }

    private void initVolume() {
        // volume controller
        //AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (am != null) {
            Util.initVolumeToSixty(am);
        }
    }
}

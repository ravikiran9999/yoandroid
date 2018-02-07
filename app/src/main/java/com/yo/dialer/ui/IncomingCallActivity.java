package com.yo.dialer.ui;

import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.androidadvance.topsnackbar.TSnackbar;
import com.yo.android.R;
import com.yo.android.chat.ui.fragments.AppContactsActivity;
import com.yo.android.util.Util;
import com.yo.android.voip.InComingCallActivity;
import com.yo.dialer.DialerHelper;
import com.yo.dialer.DialerLogs;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Rajesh Babu Polamarasetti on 22/7/17.
 */

/**
 * The Incoming Call screen
 */

public class IncomingCallActivity extends CallBaseActivity implements View.OnClickListener {
    private static final String TAG = InComingCallActivity.class.getSimpleName();

    private RelativeLayout relative_layout_main;
    private SeekBar seekBar;
    private TSnackbar mSnackbar;
    private FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DialerLogs.messageI(TAG, "YO========Incoming call screen=====");
        setContentView(R.layout.dialer_received_call);
        setIncoming(true);
        DialerLogs.messageI(TAG, "YO==== Callee Number====" + callePhoneNumber);

        //relative_layout_main = (LinearLayout) findViewById(R.id.relative_layout_main);

        initViews();
        loadUserDetails();
        initVolume();
        loadCallePhoneNumber(callePhoneNumberTxt, DialerHelper.getInstance(this).parsePhoneNumber(callePhoneNumber));
        tvCallStatus.setText(getResources().getString(R.string.incoming_call));
        //to show callee yo chat
        callMessageBtn.setTag(callePhoneNumber);
        // if user opens incoming call screen from notification, if the call is already is in progress just change UI to accepted call UI.
        CallControlsModel callControlsModel = CallControls.getCallControlsModel();
        if (callControlsModel != null && callControlsModel.isCallAccepted()) {
            changeToAcceptedCallUI();
            loadPreviousSettings();
        }
        updateCallType();

    }

    /**
     * Initializes the views
     */
    private void initViews() {
        mAcceptedCallHeader = findViewById(R.id.received_call_header);
        mInComingHeader = findViewById(R.id.incoming_call_header);

        //Incoming
        calleImageView = (CircleImageView) mInComingHeader.findViewById(R.id.imv_caller_pic);
        calleNameTxt = (TextView) mInComingHeader.findViewById(R.id.tv_caller_name);
        callePhoneNumberTxt = (TextView) mInComingHeader.findViewById(R.id.tv_caller_number);
        fullImageLayout = (RelativeLayout) findViewById(R.id.full_image_layout);
        tvCallStatus = (TextView) mInComingHeader.findViewById(R.id.tv_incoming);
        tvCallType = (TextView) mInComingHeader.findViewById(R.id.tv_incoming_call);
        mAcceptedCallHeader.setVisibility(View.GONE);

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
        callMicBtn = (ImageView) findViewById(R.id.btnMICOff);
        callEndBtn = (ImageView) findViewById(R.id.btnEndCall);

        hideMicAndSpeaker();


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
     * Hides the mic and speaker
     */
    private void hideMicAndSpeaker() {
        callMicBtn.setVisibility(View.GONE);
        callSpeakerBtn.setVisibility(View.GONE);
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
        floatingActionButton.setOnClickListener(this);
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
                break;
            case R.id.fab:
                init();
                break;
        }
    }

    /**
     * Change the UI to accepted call UI
     */
    private void changeToAcceptCallUI() {
        mInComingHeader.setVisibility(View.GONE);
        changeToAcceptedCallUI();
    }

    /**
     * Accept the call
     */
    protected void acceptCall() {
        if (sipBinder != null && sipBinder.getYOHandler() != null) {
            sipBinder.getYOHandler().acceptCall();
        } else {
            DialerLogs.messageE(TAG, "YO====sipBinder == null && sipBinder.getYOHandler() ==NULL");
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

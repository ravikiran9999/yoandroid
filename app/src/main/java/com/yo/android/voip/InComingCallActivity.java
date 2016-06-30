package com.yo.android.voip;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.ui.BaseActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by Ramesh on 26/6/16.
 */
public class InComingCallActivity extends BaseActivity implements View.OnClickListener {
    //
    public static final int NOEVENT = 0;
    public static final int MUTE_ON = 1;
    public static final int MUTE_OFF = 2;
    public static final int SPEAKER_ON = 3;
    public static final int SPEAKER_OFF = 4;
    public static final int CALL_ACCEPTED_START_TIMER = 10;
    public static final String CALLER_NO = "callerNo";
    public static final String CALLER = "caller";
    public static final String CALLER_NAME = "callerName";
    private SipCallModel callModel;
    private CallLogsModel log;
    private boolean isMute;
    private boolean isSpeakerOn;
    private TextView callerName;
    private TextView callerName2;
    private TextView callerNumber;
    private TextView callerNumber2;
    private TextView callDuration;
    int sec = 0, min = 0, hr = 0;
    private Handler handler;
    private EventBus bus = EventBus.getDefault();
    private MediaPlayer player;
    private View mReceivedCallHeader;
    private View mInComingHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialer_received_call);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        initViews();
        callModel = new SipCallModel();
        bus.register(this);
        //
        if (getIntent().getStringExtra(CALLER_NAME) != null) {
            callerName.setText(getIntent().getStringExtra(CALLER_NAME));
        } else {
            callerName.setText(getIntent().getStringExtra(CALLER_NO));
        }
        callerName.setText(getIntent().getStringExtra(CALLER));
        callerName2.setText(getIntent().getStringExtra(CALLER));
        callDuration.setText("Connecting...");
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(VoipConstants.CALL_ACTION_IN_COMING);
        Bundle callBundle = new Bundle();
        callBundle.putString(CALLER_NO, getIntent().getStringExtra(CALLER_NO));
        broadcastIntent.putExtras(callBundle);
        sendBroadcast(broadcastIntent);
        callModel.setOnCall(true);
        //CallLogs Model
        log = new CallLogsModel();
        log.setCallerName(getIntent().getStringExtra(CALLER));
        log.setCallTime(System.currentTimeMillis() / 1000L);
        log.setCallerNo(getIntent().getStringExtra(CALLER));
        log.setCallType(VoipConstants.CALL_DIRECTION_IN_MISSED);
        log.setCallMode(VoipConstants.CALL_MODE_VOIP);
        player = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI);
        player.start();

    }

    private void initViews() {
        findViewById(R.id.imv_speaker).setOnClickListener(this);
        findViewById(R.id.imv_mic_off).setOnClickListener(this);
        findViewById(R.id.btnEndCall).setOnClickListener(this);
        findViewById(R.id.btnAcceptCall).setOnClickListener(this);
        mReceivedCallHeader = findViewById(R.id.received_call_header);
        mInComingHeader = findViewById(R.id.incoming_call_header);
        callerName = (TextView) mReceivedCallHeader.findViewById(R.id.tv_caller_name);
        callerName2 = (TextView) mInComingHeader.findViewById(R.id.tv_caller_name);
        callerNumber = (TextView) mInComingHeader.findViewById(R.id.tv_caller_number);
        callerNumber2 = (TextView) mReceivedCallHeader.findViewById(R.id.tv_caller_number);
        callDuration = (TextView) mReceivedCallHeader.findViewById(R.id.tv_call_duration);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imv_mic_off:
                isMute = !isMute;
                if (isMute) {
                    callModel.setEvent(MUTE_ON);
                } else {
                    callModel.setEvent(MUTE_OFF);
                }
                bus.post(callModel);
                mToastFactory.showToast("Mute " + (isMute ? "ON" : "OFF"));
                break;
            case R.id.imv_speaker:
                isSpeakerOn = !isSpeakerOn;
                if (isSpeakerOn) {
                    callModel.setEvent(SPEAKER_ON);
                } else {
                    callModel.setEvent(SPEAKER_OFF);
                }
                bus.post(callModel);
                mToastFactory.showToast("Speaker " + (isSpeakerOn ? "ON" : "OFF"));
                break;
            case R.id.btnEndCall:
                callModel.setOnCall(false);
                log.setCallType(VoipConstants.CALL_DIRECTION_IN);
                bus.post(callModel);
                finish();
            case R.id.btnAcceptCall:
                onCallAccepted();
                break;
            default:
                break;
        }
    }

    public void onCallAccepted() {
        log.setCallType(VoipConstants.CALL_DIRECTION_IN);
        mReceivedCallHeader.setVisibility(View.VISIBLE);
        mInComingHeader.setVisibility(View.GONE);
        try {
            player.stop();
            player.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mLog.d("BUS", "ONCALLACCEPTED");
        callModel.setOnCall(true);
        bus.post(callModel);
        startTimer();
    }


    @Subscribe
    public void onEvent(SipCallModel model) {
        if (model.isOnCall() && model.getEvent() == CALL_ACCEPTED_START_TIMER) {
            runOnUiThread(new Runnable() {
                public void run() {
                    startTimer();
                }
            });
        }
    }


    private void startTimer() {
        handler = new Handler(Looper.getMainLooper());

        final Runnable r = new Runnable() {
            public void run() {
                if (!hasDestroyed()) {
                    sec++;
                    min += sec / 60;
                    hr += min / 60;
                    sec = sec % 60;
                    callDuration.setText(String.format("%02d:%02d:%02d", hr, min, sec));
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.postDelayed(r, 1000);

    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        moveTaskToBack(true);
    }
}

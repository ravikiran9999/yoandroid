package com.yo.android.voip;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.pjsip.SipBinder;
import com.yo.android.pjsip.YoSipService;
import com.yo.android.ui.BaseActivity;
import com.yo.android.ui.fragments.DialerFragment;
import com.yo.android.util.Util;

import de.greenrobot.event.EventBus;


/**
 * Created by Ramesh on 26/6/16.
 */
public class InComingCallActivity extends BaseActivity implements View.OnClickListener, CallEvents {
    //
    public static final int CALL_ACCEPTED_START_TIMER = 10;
    public static final String CALLER_NO = "callerNo";
    public static final String CALLER = "caller";
    public static final String CALLER_NAME = "callerName";
    private SipCallModel callModel;
    private CallLogsModel log;
    private boolean isMute;
    private boolean isHoldOn;
    private boolean isSpeakerOn;
    private TextView callerName;
    private TextView callerName2;
    private TextView callerNumber;
    private TextView callerNumber2;
    private TextView callDuration;
    int sec = 0, min = 0, hr = 0;
    private EventBus bus = EventBus.getDefault();
    private View mReceivedCallHeader;
    private View mInComingHeader;

    private int notificationId;
    private SipBinder sipBinder;
    private Handler mHandler = new Handler();
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            sipBinder = (SipBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            sipBinder = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialer_received_call);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        initViews();
        bindService(new Intent(this, YoSipService.class), connection, BIND_AUTO_CREATE);
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
        callModel.setOnCall(true);
        //CallLogs Model
        log = new CallLogsModel();
        log.setCallerName(getIntent().getStringExtra(CALLER));
        log.setCallTime(System.currentTimeMillis() / 1000L);
        log.setCallerNo(getIntent().getStringExtra(CALLER));
        log.setCallType(VoipConstants.CALL_DIRECTION_IN_MISSED);
        log.setCallMode(VoipConstants.CALL_MODE_VOIP);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(UserAgent.ACTION_CALL_END));
        notificationId = Util.createNotification(this, getIntent().getStringExtra(CALLER_NO), "Incoming call", InComingCallActivity.class, getIntent());
    }

    private void initViews() {
        findViewById(R.id.imv_speaker).setOnClickListener(this);
        findViewById(R.id.imv_mic_off).setOnClickListener(this);
        findViewById(R.id.btnEndCall).setOnClickListener(this);
        findViewById(R.id.btnRejectCall).setOnClickListener(this);
        findViewById(R.id.btnAcceptCall).setOnClickListener(this);
        findViewById(R.id.btnHold).setOnClickListener(this);
        mReceivedCallHeader = findViewById(R.id.received_call_header);
        mInComingHeader = findViewById(R.id.incoming_call_header);
        mReceivedCallHeader.setVisibility(View.GONE);
        //
        callerName = (TextView) mReceivedCallHeader.findViewById(R.id.tv_caller_name);
        callerName2 = (TextView) mInComingHeader.findViewById(R.id.tv_caller_name);
        callerNumber = (TextView) mInComingHeader.findViewById(R.id.tv_caller_number);
        callerNumber2 = (TextView) mReceivedCallHeader.findViewById(R.id.tv_caller_number);
        callDuration = (TextView) mReceivedCallHeader.findViewById(R.id.tv_call_duration);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        running = false;
        bus.unregister(this);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnHold:
                isHoldOn = !isHoldOn;
                if (sipBinder != null) {
                    sipBinder.getHandler().setHoldCall(isHoldOn);
                }
                if (isHoldOn) {
                    callModel.setEvent(HOLD_ON);
                } else {
                    callModel.setEvent(HOLD_OFF);
                }
                bus.post(callModel);
                mToastFactory.showToast("Hold " + (isHoldOn ? "ON" : "OFF"));
                break;
            case R.id.imv_mic_off:
                isMute = !isMute;
                if (sipBinder != null) {
                    sipBinder.getHandler().getMediaManager().setMicrophoneMuteOn(isMute);
                }
                if (isMute) {
                    callModel.setEvent(MUTE_ON);
                    ((ImageView) v).setImageResource(R.drawable.ic_mute_on);
                } else {
                    callModel.setEvent(MUTE_OFF);
                    ((ImageView) v).setImageResource(R.drawable.ic_mute_off);
                }
                bus.post(callModel);
                mToastFactory.showToast("Mute " + (isMute ? "ON" : "OFF"));
                break;
            case R.id.imv_speaker:
                if (!callModel.isOnCall()) {
                    return;
                }
                isSpeakerOn = !isSpeakerOn;
                if (sipBinder != null) {
                    sipBinder.getHandler().getMediaManager().setSpeakerOn(isSpeakerOn);
                }

                if (isSpeakerOn) {
                    callModel.setEvent(SPEAKER_ON);
                    ((ImageView) v).setImageResource(R.drawable.ic_speaker_on);
                } else {
                    callModel.setEvent(SPEAKER_OFF);
                    ((ImageView) v).setImageResource(R.drawable.ic_speaker_off);
                }
                bus.post(callModel);
                mToastFactory.showToast("Speaker " + (isSpeakerOn ? "ON" : "OFF"));
                break;
            case R.id.btnEndCall:
            case R.id.btnRejectCall:
                if (sipBinder != null) {
                    sipBinder.getHandler().hangupCall();
                    running = false;
                    mHandler.removeCallbacks(startTimer);
                }
                callModel.setOnCall(false);
                log.setCallType(VoipConstants.CALL_DIRECTION_IN);
                bus.post(callModel);
                bus.post(DialerFragment.REFRESH_CALL_LOGS);
                finish();
            case R.id.btnAcceptCall:
                onCallAccepted();
                break;
            case R.id.btnMessage:
                mToastFactory.showToast("Message: Need to implement");
                break;
            default:
                break;
        }
    }

    public void onCallAccepted() {
        if (sipBinder != null) {
            sipBinder.getHandler().acceptCall();
            running = true;
            mHandler.post(startTimer);
        }
        log.setCallType(VoipConstants.CALL_DIRECTION_IN);
        mReceivedCallHeader.setVisibility(View.VISIBLE);
        findViewById(R.id.btnEndCall).setVisibility(View.VISIBLE);
        findViewById(R.id.btnRejectCall).setVisibility(View.GONE);
        findViewById(R.id.btnMessage).setVisibility(View.GONE);
        findViewById(R.id.btnHold).setAlpha(1);
        mInComingHeader.setVisibility(View.GONE);
        mLog.d("BUS", "ONCALLACCEPTED");
        callModel.setOnCall(true);
        bus.post(callModel);
    }

    //    @Subscribe
    public void onEvent(SipCallModel model) {
        if (model.isOnCall() && model.getEvent() == CALL_ACCEPTED_START_TIMER) {
            //
        } else if (!model.isOnCall()) {
            if (model.getEvent() == UserAgent.CALL_STATE_BUSY
                    || model.getEvent() == UserAgent.CALL_STATE_ERROR
                    || model.getEvent() == UserAgent.CALL_STATE_END
                    ) {
                Util.cancelNotification(this, notificationId);
                finish();
                bus.post(DialerFragment.REFRESH_CALL_LOGS);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mToastFactory.showToast("Call ended.");
                    }
                });

            }

        }
    }


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        moveTaskToBack(true);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            finish();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Util.cancelNotification(context, notificationId);
                    mToastFactory.showToast("Call ended.");
                    bus.post(DialerFragment.REFRESH_CALL_LOGS);
                }
            });

        }
    };
    boolean running;
    private Runnable startTimer = new Runnable() {
        @Override
        public void run() {
            if (running) {
                mHandler.postDelayed(this, 1000);
            }
            if (sipBinder != null) {
                long start = sipBinder.getHandler().getCallStartDuration();
                long now = System.currentTimeMillis();
                long seconds = now - start;
                seconds /= 1000;
                StringBuilder mRecycle = new StringBuilder(8);
                String text = DateUtils.formatElapsedTime(mRecycle, seconds);
                callDuration.setText(text);
            }

        }
    };
}

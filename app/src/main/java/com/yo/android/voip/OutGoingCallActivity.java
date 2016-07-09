package com.yo.android.voip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.ui.BaseActivity;
import com.yo.android.ui.fragments.DialerFragment;
import com.yo.android.util.Util;

import de.greenrobot.event.EventBus;


/**
 * Created by Ramesh on 26/6/16.
 */
public class OutGoingCallActivity extends BaseActivity implements View.OnClickListener, CallEvents {
    //
    public static final int CALL_ACCEPTED_START_TIMER = 10;
    public static final String CALLER_NO = "callerNo";
    public static final String CALLER_NAME = "callerName";
    private SipCallModel callModel;
    private CallLogsModel log;
    private boolean isMute;
    private boolean isSpeakerOn;
    private TextView callerName;
    private TextView callerNumber;
    private TextView callDuration;
    int sec = 0, min = 0, hr = 0;
    private Handler handler;
    private EventBus bus = EventBus.getDefault();
    int notificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialer_outgoing_call);
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
        callerName.setText(getIntent().getStringExtra(CALLER_NO));
        callDuration.setText("Calling...");
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(VoipConstants.CALL_ACTION_OUT_GOING);
        Bundle callBundle = new Bundle();
        callBundle.putString(CALLER_NO, getIntent().getStringExtra(CALLER_NO));
        broadcastIntent.putExtras(callBundle);
        sendBroadcast(broadcastIntent);
        callModel.setOnCall(true);
        //CallLogs Model
        log = new CallLogsModel();
        String mobile = getIntent().getStringExtra(CALLER_NO);
        log.setCallerName("Sandeep Dev");
        log.setCallTime(System.currentTimeMillis() / 1000L);
        log.setCallerNo(mobile);
        log.setCallType(VoipConstants.CALL_DIRECTION_OUT);
        log.setCallMode(VoipConstants.CALL_MODE_VOIP);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(UserAgent.ACTION_CALL_END));
        notificationId = Util.createNotification(this, mobile, "Outgoing call", OutGoingCallActivity.class, getIntent());
    }

    private void initViews() {
        findViewById(R.id.imv_speaker).setOnClickListener(this);
        findViewById(R.id.imv_mic_off).setOnClickListener(this);
        findViewById(R.id.btnEndCall).setOnClickListener(this);
        callerName = (TextView) findViewById(R.id.tv_caller_name);
        callerNumber = (TextView) findViewById(R.id.tv_caller_number);
        callDuration = (TextView) findViewById(R.id.tv_call_duration);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Util.createNotification(this, "Yo App Calling", "Outgoing call", OutGoingCallActivity.class, intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);

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
                bus.post(callModel);
                Util.cancelNotification(this, notificationId);
                finish();
                break;
            default:
                break;
        }
    }

    //    @Subscribe
    public void onEvent(SipCallModel model) {
        if (model.isOnCall() && model.getEvent() == CALL_ACCEPTED_START_TIMER) {
            runOnUiThread(new Runnable() {
                public void run() {
                    startTimer();
                }
            });
        } else if (!model.isOnCall()) {
            if (model.getEvent() == UserAgent.CALL_STATE_BUSY
                    || model.getEvent() == UserAgent.CALL_STATE_ERROR
                    || model.getEvent() == UserAgent.CALL_STATE_END
                    ) {
                Util.cancelNotification(this, notificationId);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mToastFactory.showToast("Call ended.");
                    }
                });
                bus.post(DialerFragment.REFRESH_CALL_LOGS);
                finish();
            }

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

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            finish();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mToastFactory.showToast("Call ended.");
                    Util.cancelNotification(context, notificationId);
                    bus.post(DialerFragment.REFRESH_CALL_LOGS);
                }
            });

        }
    };

}

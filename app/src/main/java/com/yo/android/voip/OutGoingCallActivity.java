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

import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.pjsip_inv_state;

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
    private Handler mHandler = new Handler();
    boolean running;

    private SipBinder sipBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            sipBinder = (SipBinder) service;
            updateState();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            sipBinder = null;
        }
    };

    private void updateState() {
        if (sipBinder != null) {
            CallInfo callInfo = sipBinder.getHandler().getInfo();
            boolean isConnected = callInfo.getState() == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED;
            if (isConnected) {
                running = true;
                mHandler.post(startTimer);
            }
        }
    }

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
        callModel.setOnCall(true);
        //CallLogs Model
        String mobile = getIntent().getStringExtra(CALLER_NO);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(UserAgent.ACTION_CALL_END));
        notificationId = Util.createNotification(this, mobile, "Outgoing call", OutGoingCallActivity.class, getIntent());
        bindService(new Intent(this, YoSipService.class), connection, BIND_AUTO_CREATE);
    }

    private void initViews() {
        findViewById(R.id.imv_speaker).setOnClickListener(this);
        findViewById(R.id.imv_mic_off).setOnClickListener(this);
        findViewById(R.id.btnEndCall).setOnClickListener(this);
        callerName = (TextView) findViewById(R.id.tv_caller_name);
        callerNumber = (TextView) findViewById(R.id.tv_caller_number);
        callDuration = (TextView) findViewById(R.id.tv_dialing);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Util.createNotification(this, "Yo App Calling", "Outgoing call", OutGoingCallActivity.class, intent);
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
            case R.id.imv_mic_off:
                isMute = !isMute;
                if (sipBinder != null) {
                    sipBinder.getHandler().getMediaManager().setMicrophoneMuteOn(isMute);
                }
                if (isMute) {
                    callModel.setEvent(MUTE_ON);
                    ((ImageView) v).setImageResource(R.drawable.ic_mute_active_dailing);
                } else {
                    callModel.setEvent(MUTE_OFF);
                    ((ImageView) v).setImageResource(R.drawable.ic_mute_dailing);
                }
                bus.post(callModel);
                mToastFactory.showToast("Mute " + (isMute ? "ON" : "OFF"));
                break;
            case R.id.imv_speaker:
                isSpeakerOn = !isSpeakerOn;
                if (sipBinder != null) {
                    sipBinder.getHandler().getMediaManager().setSpeakerOn(isSpeakerOn);
                }

                if (isSpeakerOn) {
                    callModel.setEvent(SPEAKER_ON);
                    v.setAlpha(1f);
                } else {
                    callModel.setEvent(SPEAKER_OFF);
                    v.setAlpha(0.5f);
                }
                bus.post(callModel);
                mToastFactory.showToast("Speaker " + (isSpeakerOn ? "ON" : "OFF"));
                break;
            case R.id.btnEndCall:
                if (sipBinder != null) {
                    sipBinder.getHandler().hangupCall();
                    running = false;
                    mHandler.removeCallbacks(startTimer);
                }
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
            running = true;
            mHandler.post(startTimer);
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

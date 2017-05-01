package com.yo.android.voip;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.format.DateUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.orion.android.common.util.ConnectivityHelper;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.calllogs.CallLog;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.helpers.Helper;
import com.yo.android.model.Contact;
import com.yo.android.model.dialer.OpponentDetails;
import com.yo.android.pjsip.SipBinder;
import com.yo.android.pjsip.YoSipService;
import com.yo.android.provider.YoAppContactContract;
import com.yo.android.ui.BaseActivity;
import com.yo.android.ui.fragments.DialerFragment;
import com.yo.android.util.Util;
import com.yo.android.vox.BalanceHelper;

import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.pjsip_inv_state;

import javax.inject.Inject;
import javax.inject.Named;

import de.greenrobot.event.EventBus;


/**
 * Created by Ramesh on 26/6/16.
 */
public class OutGoingCallActivity extends BaseActivity implements View.OnClickListener, CallEvents {
    //
    public static final int CALL_ACCEPTED_START_TIMER = 10;
    public static final String CALLER_NO = "callerNo";
    public static final String CALLER_NAME = "callerName";
    private static final String TAG = OutGoingCallActivity.class.getSimpleName();
    private static final int KEEP_ON_HOLD = 100;
    private static final int KEEP_ON_HOLD_RESUME = 101;

    public static final String DISPLAY_NUMBER = "displaynumber";

    private SipCallModel callModel;
    private CallLogsModel log;
    private boolean isMute;
    private boolean isSpeakerOn;
    private TextView callerName;
    private TextView callerNumber;
    private String diplayNumber;
    private TextView connectionStatusTextView;
    int sec = 0, min = 0, hr = 0;
    private Handler handler;
    private EventBus bus = EventBus.getDefault();
    private Handler mHandler = new Handler();
    boolean running;
    private String mobile;
    private ImageView callerImageView;

    private SipBinder sipBinder;

    private TextView callDurationTextView;


    @Inject
    protected BalanceHelper mBalanceHelper;

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;

    @Inject
    ConnectivityHelper mHelper;

    public static final int OPEN_ADD_BALANCE_RESULT = 1000;

    public static final String AUTHORITY = YoAppContactContract.CONTENT_AUTHORITY;

    /**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER = "date DESC";
    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/callLogs");

    @Inject
    ContactsSyncManager mContactsSyncManager;
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
            if (callInfo != null) {
                boolean isConnected = callInfo.getState() == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED;
                if (isConnected) {
                    running = true;
                    mHandler.post(startTimer);
                }
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
        Contact contact = mContactsSyncManager.getContactByVoxUserName(getIntent().getStringExtra(CALLER_NO));

        if (getIntent().hasExtra(VoipConstants.PSTN) && getIntent().getBooleanExtra(VoipConstants.PSTN, false)) {
            String phoneName = Helper.getContactName(this, getIntent().getStringExtra(DISPLAY_NUMBER));
            if(phoneName !=null) {
                callerName.setText(phoneName);
            } else {
                final ContentResolver resolver = getContentResolver();
                Cursor c = null;
                try {
                    String trimmedNumber = getIntent().getStringExtra(DISPLAY_NUMBER).replace(" ", "");
                    c = resolver.query(
                            CONTENT_URI,

                            null,
                            //CallLog.Calls.NUMBER +" = "+ getIntent().getStringExtra(DISPLAY_NUMBER),
                            CallLog.Calls.NUMBER +" = "+ trimmedNumber,
                            null,
                            DEFAULT_SORT_ORDER);
                    if (c == null || !c.moveToFirst()) {

                    } else {
                     callerName.setText(c.getString(c.getColumnIndex(CallLog.Calls.CACHED_NAME)));

                    }
                } finally {
                    if (c != null) c.close();
                }
            }
        } else if (getIntent().getStringExtra(CALLER_NAME) != null) {
            callerName.setText(getIntent().getStringExtra(CALLER_NAME));
        } else if (contact != null && contact.getName() != null) {
            callerName.setText(contact.getName());
        } else {
            String stringExtra = getIntent().getStringExtra(CALLER_NO);
            if (stringExtra != null && stringExtra.contains(BuildConfig.RELEASE_USER_TYPE)) {
                try {
                    stringExtra = stringExtra.substring(stringExtra.indexOf(BuildConfig.RELEASE_USER_TYPE) + 6, stringExtra.length() - 1);
                    callerName.setText(stringExtra);
                } catch (StringIndexOutOfBoundsException e) {
                    mLog.e(TAG, "" + e);
                }
            } else if (stringExtra != null) {
                String phoneName = Helper.getContactName(this, stringExtra);
                if(phoneName != null) {
                    callerName.setText(phoneName);
                } else {
                    final ContentResolver resolver = getContentResolver();
                    Cursor c = null;
                    try {
                        String trimmedNumber = getIntent().getStringExtra(DISPLAY_NUMBER).replace(" ", "");
                        c = resolver.query(
                                CONTENT_URI,

                                null,
                                //CallLog.Calls.NUMBER + " = " + getIntent().getStringExtra(DISPLAY_NUMBER),
                                CallLog.Calls.NUMBER +" = "+ trimmedNumber,
                                null,
                                DEFAULT_SORT_ORDER);
                        if (c == null || !c.moveToFirst()) {

                        } else {
                            callerName.setText(c.getString(c.getColumnIndex(CallLog.Calls.CACHED_NAME)));

                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (c != null) c.close();
                    }
                }
            }
        }

        //To display name of the user based on vox username
        if (contact != null) {
            Glide.with(this).load(CallLog.Calls.getImagePath(this, contact.getNexgieUserName()))
                    .placeholder(R.drawable.ic_contacts)
                    .dontAnimate()
                    .error(R.drawable.ic_contacts).
                    into(callerImageView);
        }


        connectionStatusTextView.setText(getResources().getString(R.string.connecting_status));
        callModel.setOnCall(true);
        //CallLogs Model
        mobile = getIntent().getStringExtra(CALLER_NO);
        diplayNumber = getIntent().getStringExtra(DISPLAY_NUMBER);
        bus.register(this);
        bindService(new Intent(this, YoSipService.class), connection, BIND_AUTO_CREATE);
    }


    @Override
    protected void onPause() {
        super.onPause();
        bus.unregister(this);
    }

    private void initViews() {
        findViewById(R.id.imv_speaker).setOnClickListener(this);
        findViewById(R.id.imv_mic_off).setOnClickListener(this);
        findViewById(R.id.btnEndCall).setOnClickListener(this);
        callDurationTextView = (TextView)findViewById(R.id.tv_call_duration) ;
        callerName = (TextView) findViewById(R.id.tv_caller_name);
        callerNumber = (TextView) findViewById(R.id.tv_caller_number);
        connectionStatusTextView = (TextView) findViewById(R.id.tv_dialing);
        callerImageView = (ImageView) findViewById(R.id.imv_caller_pic);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        running = false;

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
                    sipBinder.getHandler().hangupCall(CallLog.Calls.OUTGOING_TYPE);
                    running = false;
                    mHandler.removeCallbacks(startTimer);
                }
                callModel.setOnCall(false);
                bus.post(callModel);
                finish();
                break;
            default:
                break;
        }
    }


    //    @Subscribe
    public void onEvent(Object object) {
        if (object instanceof SipCallModel) {
            SipCallModel model = (SipCallModel) object;
            if (model.getEvent() == 3) {
                connectionStatusTextView.setText(getResources().getString(R.string.connecting_status));
            }else {
                if (model.isOnCall() && model.getEvent() == CALL_ACCEPTED_START_TIMER) {
                    connectionStatusTextView.setText(getResources().getString(R.string.connected_status));
                    running = true;
                    mHandler.post(startTimer);
                } else if (!model.isOnCall()) {
                    if (model.getEvent() == UserAgent.CALL_STATE_BUSY
                            || model.getEvent() == UserAgent.CALL_STATE_ERROR
                            || model.getEvent() == UserAgent.CALL_STATE_END) {
                        bus.post(DialerFragment.REFRESH_CALL_LOGS);
                        finish();
                    }
                }
            }
        } else if (object instanceof OpponentDetails) {
            Util.showErrorMessages(bus, (OpponentDetails) object, this, mToastFactory, mBalanceHelper, preferenceEndPoint, mHelper);
        }else if (object instanceof Integer) {
            // while outgoing call is going on if default incoming call comes should put on hold
            int hold = (int) object;
            if (hold == KEEP_ON_HOLD) {
                if (sipBinder != null) {
                    sipBinder.getHandler().setHoldCall(true);
                    connectionStatusTextView.setText(getResources().getString(R.string.connected_status));
                }
            } else if (hold == KEEP_ON_HOLD_RESUME) {
                if (sipBinder != null) {
                    sipBinder.getHandler().setHoldCall(false);
                    connectionStatusTextView.setText(getResources().getString(R.string.connected_status));
                }
            }
        }
    }



    private Runnable startTimer = new Runnable() {
        @Override
        public void run() {
            if (running) {
                mHandler.postDelayed(this, 1000);
            }
            if (sipBinder != null) {
                callDurationTextView.setVisibility(View.VISIBLE);
                long start = sipBinder.getHandler().getCallStartDuration();
                long now = System.currentTimeMillis();
                long seconds = now - start;
                seconds /= 1000;
                StringBuilder mRecycle = new StringBuilder(8);
                String text = DateUtils.formatElapsedTime(mRecycle, seconds);
                callDurationTextView.setText(text);
            }

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_ADD_BALANCE_RESULT && resultCode == Activity.RESULT_OK) {

        }
    }
}

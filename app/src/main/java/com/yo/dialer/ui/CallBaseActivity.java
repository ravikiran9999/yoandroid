package com.yo.dialer.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.model.dialer.OpponentDetails;
import com.yo.android.model.wallet.Balance;
import com.yo.android.pjsip.SipBinder;
import com.yo.android.pjsip.SipHelper;
import com.yo.android.ui.BaseActivity;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;
import com.yo.android.util.YODialogs;
import com.yo.android.vox.BalanceHelper;
import com.yo.dialer.CallExtras;
import com.yo.dialer.DialerHelper;
import com.yo.dialer.DialerLogs;
import com.yo.dialer.Dialogs;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by root on 31/7/17.
 */

class CallBaseActivity extends BaseActivity {

    private static final String TAG = CallBaseActivity.class.getSimpleName();
    //User details;
    protected String calleName;
    protected String callePhoneNumber;
    protected String calleImageUrl;
    protected TextView tvCallStatus;
    protected TextView tvCallType;
    protected TextView tvAccepetedCallType;

    protected boolean isPstn;


    //Handler for call duration
    protected Handler mHandler = new Handler();

    //to stop timer
    protected boolean isCallStopped = true;

    protected static AudioManager am;

    protected static SipBinder sipBinder;

    protected TextView durationTxtview;
    protected TextView connectionStatusTxtView;
    protected RelativeLayout fullImageLayout;
    protected CircleImageView calleImageView;
    protected TextView calleNameTxt;
    protected TextView callePhoneNumberTxt;

    //YO Call buttons;
    protected ImageView callAcceptBtn;
    protected ImageView callRejectBtn;
    protected ImageView callMessageBtn;
    protected ImageView callSpeakerBtn;
    protected ImageView callMicBtn;

    protected ImageView callSpeakerView;
    protected ImageView callMuteView;
    protected ImageView callHoldView;

    //After accepting call
    protected View mAcceptedCallHeader;
    protected CircleImageView acceptedCalleImageView;
    protected TextView acceptedcalleNameTxt;
    protected TextView acceptedcallePhoneNumberTxt;
    protected ImageView callEndBtn;

    private boolean isIncoming;
    protected View mInComingHeader;
    protected View mOutgoingCallHeader;

    @Inject
    protected BalanceHelper mBalanceHelper;

    private int callNotificationId;


    public boolean isIncoming() {
        return isIncoming;
    }

    public void setIncoming(boolean incoming) {
        isIncoming = incoming;
    }

    LocalBroadcastManager mLocalBroadcastManager;
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(CallExtras.Actions.COM_YO_ACTION_CLOSE)) {
                String reason = intent.getStringExtra("Reason");
                callDisconnected(reason);
                SipHelper.isAlreadyStarted = false; // not allowing to do more than one call at the same time.
            } else if (intent.getAction().equals(CallExtras.Actions.COM_YO_ACTION_CALL_UPDATE_STATUS)) {
                updateWithCallStatus(intent.getIntExtra(CallExtras.CALL_STATE, 0));
            } else if (intent.getAction().equals(CallExtras.Actions.COM_YO_ACTION_CALL_ACCEPTED)) {
                callAccepted();
            } else if (intent.getAction().equalsIgnoreCase(CallExtras.Actions.COM_YO_ACTION_CALL_NO_NETWORK)) {
                showToast(context, context.getResources().getString(R.string.calls_no_network));
                SipHelper.isAlreadyStarted = false; // not allowing to do more than one call at the same time.
            }
        }
    };

    private void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    protected ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            sipBinder = (SipBinder) service;
            DialerLogs.messageW(TAG, "YO====Service connected to CallBaseActivity activity====" + sipBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            DialerLogs.messageW(TAG, "YO====Service disconnected to CallBaseActivity activity====");
            sipBinder = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (sipBinder == null) {
            bindService(new Intent(this, com.yo.dialer.YoSipService.class), connection, BIND_AUTO_CREATE);
        }
        registerForcallActions();
        if (am == null) {
            am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        callePhoneNumber = getIntent().getStringExtra(CallExtras.CALLER_NO);
        calleImageUrl = getIntent().getStringExtra(CallExtras.IMAGE);
        calleName = getIntent().getStringExtra(CallExtras.NAME);
        isPstn = getIntent().getBooleanExtra(CallExtras.IS_PSTN, false);
        callNotificationId = getIntent().getIntExtra("notificationId", 0);
        EventBus.getDefault().register(this);
    }

    /**
     * Register for call actions
     */
    private void registerForcallActions() {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(CallExtras.Actions.COM_YO_ACTION_CLOSE);
        mIntentFilter.addAction(CallExtras.Actions.COM_YO_ACTION_CALL_ACCEPTED);
        mIntentFilter.addAction(CallExtras.Actions.COM_YO_ACTION_CALL_UPDATE_STATUS);
        mIntentFilter.addAction(CallExtras.Actions.COM_YO_ACTION_CALL_NO_NETWORK);
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    protected boolean toggleHold(View v) {
        return CallControls.toggleHold(sipBinder, v);
    }

    protected void toggleMic(View v) {
        CallControls.toggleMic(sipBinder, v);
    }

    protected void toggleSpeaker(View v) {
        CallControls.toggleSpeaker(am, v);
    }

    protected void loadPrevMicSettings(View v) {
        CallControls.loadPrevMicSettings(sipBinder, v);
    }

    protected void loadPrevSpeakerSettings(View v) {
        CallControls.loadPrevSpeakerSettings(am, v);
    }

    protected void toggleRecSpeaker(View v) {
        CallControls.toggleRecSpeaker(am, v);
    }

    /**
     * Rejects the call
     */
    protected void rejectCall() {
        Util.cancelNotification(this, callNotificationId);
        if (sipBinder != null && sipBinder.getYOHandler() != null) {
            isCallStopped = true;
            sipBinder.getYOHandler().rejectCall();
            finish();
        } else {
            DialerLogs.messageE(TAG, "YO====sipBinder == null && sipBinder.getYOHandler() ==NULL");
        }
    }

    public void onEventMainThread(Object type) {
        if (type instanceof Integer) {
            if (type == Integer.valueOf(CallExtras.StatusCode.YO_NORMAL_PHONE_INCOMING_CALL)) {
                sipBinder.getYOHandler().setHold(true);
                CallControls.getCallControlsModel().setHoldOn(true);
            } else if (type == Integer.valueOf(CallExtras.StatusCode.YO_NORMAL_PHONE_INCOMING_CALL_DISCONNECTED)) {
                sipBinder.getYOHandler().setHold(false);
                CallControls.getCallControlsModel().setHoldOn(false);
            }
        } else if (type instanceof OpponentDetails) {
            DialerLogs.messageI(TAG, "Service not available or user not found so PSTN dialog");
            OpponentDetails opponentDetails = (OpponentDetails) type;
            if (opponentDetails != null && opponentDetails.getContact().getNexgieUserName() != null && opponentDetails.getContact().getNexgieUserName().contains(BuildConfig.RELEASE_USER_TYPE)) {
                if (opponentDetails.getStatusCode() == CallExtras.StatusCode.YO_INV_STATE_CALLEE_NOT_ONLINE) {
                    YODialogs.redirectToPSTN(EventBus.getDefault(), this, opponentDetails, preferenceEndPoint, mBalanceHelper, mToastFactory);
                }
            } else if (opponentDetails != null && opponentDetails.getVoxUserName() != null) {
                //This case is phone number is not save in his device bu the callee is voxuser
                opponentDetails.getContact().setNexgieUserName(opponentDetails.getVoxUserName());


                if (opponentDetails.getStatusCode() == CallExtras.StatusCode.YO_INV_STATE_CALLEE_NOT_ONLINE) {
                    YODialogs.redirectToPSTN(EventBus.getDefault(), this, opponentDetails, preferenceEndPoint, mBalanceHelper, mToastFactory);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        if (sipBinder != null) {
            try {
                unbindService(connection);
            } catch (IllegalArgumentException e) {
                DialerLogs.messageI(TAG, "YO====onPause====UNBIND SERVICE NOT BINDED");
            }
        }
    }

    /**
     * Loads the callee phone number
     * @param callePhoneNumberTxt The callee phone number text view
     * @param phoneNumber The callee phone number
     */
    protected void loadCallePhoneNumber(TextView callePhoneNumberTxt, String phoneNumber) {
        callePhoneNumberTxt.setText("+" + phoneNumber);
        DialerLogs.messageI(TAG, "YO====loadCallePhoneNumber====" + phoneNumber);
    }

    /**
     * Loads the callee name
     * @param textView The callee name text view
     * @param name The callee name
     */
    protected void loadCalleeName(TextView textView, String name) {

        if (!TextUtils.isEmpty(name)) {
            textView.setText(name);
            DialerLogs.messageI(TAG, "YO====loadCalleeName====" + name);
        } else {
            if (isIncoming) {
                textView.setText(DialerHelper.getInstance(this).parsePhoneNumber(callePhoneNumber));
            } else {
                textView.setText(callePhoneNumber);
            }
        }
    }

    /**
     * Loads the callee image
     * @param imageView The callee image view
     * @param imagePath The callee image path
     */
    protected void loadCalleImage(ImageView imageView, String imagePath) {
        try {
            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.ic_contacts)
                    .dontAnimate()
                    .error(R.drawable.ic_contacts);
            Glide.with(CallBaseActivity.this).load(imagePath)
                    .apply(requestOptions)
                    .into(imageView);
            loadFullImage(imagePath);
            DialerLogs.messageI(TAG, "YO====loadCalleImage====" + imagePath);
        } catch (IllegalArgumentException exe) {
            DialerLogs.messageI(TAG, "YO====loadCalleImage====" + exe.getMessage());
        }
    }

    /**
     * Loads the full image
     * @param imagePath The image path
     */
    protected void loadFullImage(String imagePath) {
        CallControls.loadFullImage(this, imagePath, fullImageLayout);
    }

    /**
     * Loads the user name and image
     */
    protected void loadUserDetails() {
        loadCalleImage(calleImageView, calleImageUrl);
        loadCalleeName(calleNameTxt, calleName);
    }

    /**
     * Called when the call is disconnected
     * @param reason
     */
    public void callDisconnected(final String reason) {
        mHandler.removeCallbacks(UIHelper.getDurationRunnable(CallBaseActivity.this));
        SipHelper.isAlreadyStarted = false;
        CallControls.getCallControlsModel().setCallAccepted(false);
        CallControls.getCallControlsModel().setSpeakerOn(false);
        am.setSpeakerphoneOn(false);
        DialerLogs.messageI(TAG, "callDisconnected Before finishing..");
        if ("Forbidden".equals(reason)) {
            mBalanceHelper.loadBalance(new Callback<Balance>() {
                @Override
                public void onResponse(Call<Balance> call, Response<Balance> response) {
                    String switchBalance = response.body().getSwitchBalance();
                    String walletBalance = response.body().getWalletBalance();
                    if(Double.parseDouble(mBalanceHelper.removeCurrencyCodeString(switchBalance)) <= 1) {
                        Dialogs.recharge(CallBaseActivity.this);
                        appLogglyUsecase.sendAlertsToLoggly(switchBalance + " : "+walletBalance, reason + ": switch balance is < 1", Constants.CRITICAL, 523);
                    } else {
                        Dialogs.nexgeRegistrationIssue(CallBaseActivity.this);
                        appLogglyUsecase.sendAlertsToLoggly(switchBalance + " : "+walletBalance, reason + ": nexge issue", Constants.CRITICAL, 809);
                    }

                }

                @Override
                public void onFailure(Call<Balance> call, Throwable t) {
                    mLog.w(TAG, "loadBalance", t.getMessage());
                }
            });

        } else if("Service not available/User not online".equals(reason) && isPstn) {
                finish();
            } else {
            if(!"Service not available/User not online".equals(reason)) {
                finish();
            }
        }
    }

    /**
     * Call is accepted
     */
    public void callAccepted() {
        DialerLogs.messageI(TAG, "Call Accepted");
        changeToAcceptedCallUI();
    }

    /**
     * Updates the call status
     * @param callStatus The call status
     */
    public void updateWithCallStatus(final int callStatus) {
        //User- > CallExtras.StatusCode.PJSIP_INV_STATE_CONFIRMED
        DialerLogs.messageI(TAG, "CallExtras.StatusCode -> updateWithCallStatus " + callStatus);

        if (callStatus == CallExtras.StatusCode.YO_INV_STATE_SC_NO_ANSWER) {
            showCallAgain();
        } else if (callStatus == CallExtras.StatusCode.YO_INV_STATE_CALLEE_NOT_ONLINE) {
            finish();
        } else {
            UIHelper.handleCallStatus(CallBaseActivity.this, isIncoming, callStatus, tvCallStatus, connectionStatusTxtView);
        }
    }


    //Need to show call again screen to caller not callee.
    private void showCallAgain() {
        if (!isIncoming()) {
            Toast.makeText(this, "Call not answered - Need to show call again screen", Toast.LENGTH_LONG).show();
            if (sipBinder != null) {
                if (sipBinder.getYOHandler() != null) {
                    sipBinder.getYOHandler().cancelCallNotification();
                }
            }
        }
        finish();
    }

    /**
     * Changes the UI to accepted call UI
     */
    protected void changeToAcceptedCallUI() {
        DialerLogs.messageI(TAG, "YO====changeToAcceptedCallUI====");
        //to increase the volume
        //am.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
       /* am.setStreamVolume(
                AudioManager.STREAM_VOICE_CALL,
                am.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
                0);*/
        //try {
        if (isIncoming) {
            if (mInComingHeader != null) {
                mInComingHeader.setVisibility(View.GONE);
            }
        } else {
            if (mOutgoingCallHeader != null) {
                mOutgoingCallHeader.setVisibility(View.GONE);
            }
        }
        mAcceptedCallHeader.setVisibility(View.VISIBLE);

        loadCalleeName(acceptedcalleNameTxt, calleName);
        loadCalleImage(acceptedCalleImageView, calleImageUrl);
        if (isIncoming) {
            loadCallePhoneNumber(acceptedcallePhoneNumberTxt, DialerHelper.getInstance(this).parsePhoneNumber(callePhoneNumber));
        } else {
            loadCallePhoneNumber(acceptedcallePhoneNumberTxt, callePhoneNumber);
        }
        isCallStopped = false;
        mHandler.post(UIHelper.getDurationRunnable(CallBaseActivity.this));
        showEndAndMessage();
        CallControls.getCallControlsModel().setCallAccepted(true);

        if (CallControls.getCallControlsModel().isSpeakerOn() && CallControls.getCallControlsModel().isCallAccepted()) {
            toggleRecSpeaker(callSpeakerView);
        }

        // } catch (IllegalArgumentException exe) {
        // DialerLogs.messageI(TAG, "YO====changeToAcceptedCallUI====" + exe.getMessage());
        //}
    }

    /**
     * Shows End call and Message buttons
     */
    private void showEndAndMessage() {
        callAcceptBtn.setVisibility(View.GONE);
        callSpeakerBtn.setVisibility(View.GONE);
        callMicBtn.setVisibility(View.GONE);
        callRejectBtn.setVisibility(View.GONE);
        callEndBtn.setVisibility(View.VISIBLE);
        callMessageBtn.setVisibility(View.VISIBLE);
    }

    /**
     * Loads the current call settings
     */
    protected void loadPreviousSettings() {
        CallControlsModel callControlsModel = CallControls.getCallControlsModel();
        if (callControlsModel != null) {
            if (callControlsModel.isMicOn()) {
                loadPrevMicSettings(callMuteView);
            }
            if (callControlsModel.isSpeakerOn()) {
                loadPrevSpeakerSettings(callSpeakerView);
                loadPrevSpeakerSettings(callSpeakerBtn);
            }
            DialerLogs.messageI(TAG, "YO====changeToAcceptedCallUI====" + callControlsModel.isHoldOn());

            if (callControlsModel.isHoldOn()) {
                callHoldView.setTag(false);
                toggleHold(callHoldView);
                tvCallStatus.setText(getResources().getString(R.string.call_on_hold_status));
                connectionStatusTxtView.setText(getResources().getString(R.string.call_on_hold_status));
            }
            if (callControlsModel.isMicOn()) {
                callMuteView.setTag(true);
                //toggleHold(callMuteView);
            }
            if (callControlsModel.isSpeakerOn()) {
                callSpeakerView.setTag(true);
                callSpeakerBtn.setTag(true);
                //toggleHold(callSpeakerView);
            }
            if (callControlsModel.isChatOpened()) {
                //TODO: NEED TO OPEN CHAT
            }
        }
    }

    /**
     * Updates the type of call text
     */
    protected void updateCallType() {
        if (isPstn) {
            tvCallType.setText(getResources().getString(R.string.pstn_call));
            tvAccepetedCallType.setText(getResources().getString(R.string.pstn_call));
        } else {
            tvCallType.setText(getResources().getString(R.string.app_call));
            tvAccepetedCallType.setText(getResources().getString(R.string.app_call));
        }
    }
}

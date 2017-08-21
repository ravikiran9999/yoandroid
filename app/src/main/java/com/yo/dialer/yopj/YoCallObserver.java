package com.yo.dialer.yopj;

import android.content.Context;

import com.yo.android.calllogs.CallLog;
import com.yo.android.model.Contact;
import com.yo.android.model.dialer.OpponentDetails;
import com.yo.dialer.CallExtras;
import com.yo.dialer.DialerLogs;
import com.yo.dialer.YoSipService;

import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallMediaInfo;
import org.pjsip.pjsua2.CallMediaInfoVector;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_status_code;
import org.pjsip.pjsua2.pjsua_call_media_status;

import de.greenrobot.event.EventBus;

import static org.pjsip.pjsua2.pjsip_status_code.PJSIP_SC_NOT_FOUND;
import static org.pjsip.pjsua2.pjsip_status_code.PJSIP_SC_REQUEST_TIMEOUT;
import static org.pjsip.pjsua2.pjsip_status_code.PJSIP_SC_SERVICE_UNAVAILABLE;

/**
 * Created by Rajesh Babu on 12/7/17.
 */

public class YoCallObserver implements YoAppObserver {
    private static final String TAG = YoCallObserver.class.getSimpleName();
    private static YoCallObserver yoCallObserver;
    private static Context mContext;
    private boolean isRinging;

    public static YoCallObserver getInstance(Context context) {
        mContext = context;
        if (yoCallObserver == null) {
            yoCallObserver = new YoCallObserver();
        }
        return yoCallObserver;
    }

    @Override
    public void notifyRegState(pjsip_status_code code, String reason, int expiration) {
        DialerLogs.messageI(TAG, "notifyRegState===========" + reason + ",CODE " + code);

        String registrationStatus;
        if (expiration == 0) {
            registrationStatus = "Unregistration";
        } else {
            registrationStatus = "Registration";
        }

        if (code.swigValue() / 100 == 2) {
            registrationStatus += " successful";
        } else {
            registrationStatus += " failed: " + reason;
        }
        YoSipService yoSipService = (YoSipService) YoCallObserver.mContext;
        if (reason != null && reason.equalsIgnoreCase(CallExtras.NETWORK_NOT_REACHABLE)) {
            if (yoSipService instanceof YoSipService) {
                yoSipService.setCallStatus(CallExtras.StatusCode.YO_CALL_NETWORK_NOT_REACHABLE);
                yoSipService.getPreferenceEndPoint().saveIntPreference(CallExtras.REGISTRATION_STATUS, CallExtras.StatusCode.YO_CALL_NETWORK_NOT_REACHABLE);
            }
        } else if (code == PJSIP_SC_REQUEST_TIMEOUT) {
            yoSipService.getPreferenceEndPoint().saveIntPreference(CallExtras.REGISTRATION_STATUS, CallExtras.StatusCode.YO_REQUEST_TIME_OUT);
        } else {
            yoSipService.getPreferenceEndPoint().saveIntPreference(CallExtras.REGISTRATION_STATUS, 0);
        }
        DialerLogs.messageI(TAG, "Registration Status " + registrationStatus);
        DialerLogs.messageI(TAG, "notifyRegState>>>> " + registrationStatus);
    }

    @Override
    public void notifyIncomingCall(YoCall call) {
        try {
            DialerLogs.messageI(TAG, "notifyIncomingCall===========" + call.getInfo().getState());
            if (mContext != null) {
                if (mContext instanceof YoSipService) {
                    ((YoSipService) mContext).OnIncomingCall(call);
                }
            }
        } catch (Exception e) {
            DialerLogs.messageI(TAG, "notifyIncomingCall===========" + e.getMessage());
        }

        //TODO: Know Caller that its ringing. // we may need to call this when playing ring sound
        CallOpParam call_param = new CallOpParam();
        call_param.setStatusCode(pjsip_status_code.PJSIP_SC_RINGING);
        try {
            call.answer(call_param);
        } catch (Exception e) {
            System.out.println(e);
            return;
        }


    }

    @Override
    public void notifyCallState(YoCall call) {
        try {
            CallInfo info = call.getInfo();
            DialerLogs.messageI(TAG, "notifyCallState===========State =" + info.getState() + ",Reason" + info.getLastReason());
            YoSipService yoSipService = (YoSipService) YoCallObserver.mContext;

            if (info.getState() == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
                yoSipService.setCallAccepted(false);
                if (YoCallObserver.mContext instanceof YoSipService) {
                    if (info.getLastReason().equalsIgnoreCase("Not Acceptable Here")) {
                        checkMissedCall();
                        yoSipService.getSipServiceHandler().updateWithCallStatus(CallExtras.StatusCode.YO_INV_STATE_SC_NO_ANSWER);
                        yoSipService.callDisconnected();
                        isRinging = false;
                    } else if (info.getLastReason().equalsIgnoreCase("Network is unreachable")) {
                        yoSipService.sendNoNetwork();
                    } else if (info.getLastReason().equalsIgnoreCase(CallExtras.StatusReason.YO_SERVICE_UNAVAILABLE)
                            || info.getLastReason().equalsIgnoreCase(CallExtras.StatusReason.YO_NOT_FOUND)
                            || info.getLastReason().equalsIgnoreCase(CallExtras.StatusReason.YO_REQUEST_TIMEOUT)) {
                        Contact contact = new Contact();
                        contact.setCountryCode("91");
                        DialerLogs.messageI(TAG, info.getRemoteUri() + "Service not available or user not found so PSTN dialog");
                        OpponentDetails details = new OpponentDetails(info.getRemoteUri(), contact, CallExtras.StatusCode.YO_INV_STATE_CALLEE_NOT_ONLINE);
                        EventBus.getDefault().post(details);
                    } else {
                        yoSipService.getSipServiceHandler().updateWithCallStatus(CallExtras.StatusCode.YO_INV_STATE_DISCONNECTED);
                        yoSipService.callDisconnected();
                    }
                }
            } else if (info.getState() == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
                yoSipService.getSipServiceHandler().updateWithCallStatus(CallExtras.StatusCode.YO_INV_STATE_CONNECTED);
                yoSipService.setCallAccepted(true);
                if (YoCallObserver.mContext instanceof YoSipService) {
                    yoSipService.callAccepted();
                }
            } else if (info.getState() == pjsip_inv_state.PJSIP_INV_STATE_EARLY && info.getLastReason().equalsIgnoreCase(CallExtras.StatusReason.YO_RINGING)) {
                yoSipService.getSipServiceHandler().updateWithCallStatus(CallExtras.StatusCode.YO_INV_STATE_SC_RINGING);
                isRinging = true;
            } else if (info.getState() == pjsip_inv_state.PJSIP_INV_STATE_CALLING) {
                yoSipService.getSipServiceHandler().updateWithCallStatus(CallExtras.StatusCode.YO_INV_STATE_SC_CALLING);
            } else {
                DialerLogs.messageE(TAG, "notifyCallState Other case===========" + info.getState());
                yoSipService.getSipServiceHandler().updateWithCallStatus(CallExtras.StatusCode.YO_INV_STATE_SC_UNKNOWN);
            }
        } catch (Exception e) {
            DialerLogs.messageE(TAG, "notifyCallState===========" + e.getMessage());
        }
    }

    private void checkMissedCall() {
        YoSipService yoSipService = (YoSipService) YoCallObserver.mContext;

        if (isRinging && yoSipService.getCallType() == CallLog.Calls.INCOMING_TYPE) {
            isRinging = false;
            yoSipService.sendMissedCallNotification();

        }
    }

    @Override
    public void notifyCallMediaState(YoCall call) {
        try {
            YoSipService yoSipService = (YoSipService) YoCallObserver.mContext;
            CallInfo info = call.getInfo();
            if (info != null && info.getState() == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
                DialerLogs.messageI(TAG, "notifyCallMediaState===========State =" + info.getState() + "," + "," + info.getStateText());
                CallMediaInfoVector media = info.getMedia();
                for (int i = 0; i < media.size(); i++) {
                    CallMediaInfo mediaInfo = media.get(i);
                    if (mediaInfo.getStatus() == pjsua_call_media_status.PJSUA_CALL_MEDIA_REMOTE_HOLD) {
                        yoSipService.setRemoteHold(true);
                        if (mContext instanceof YoSipService) {
                            yoSipService.getSipServiceHandler().updateWithCallStatus(CallExtras.StatusCode.YO_CALL_MEDIA_REMOTE_HOLD);
                        }
                    } else if (mediaInfo.getStatus() == pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE) {
                        yoSipService.setRemoteHold(false);
                        yoSipService.setLocalHold(false);
                        if (mContext instanceof YoSipService) {
                            yoSipService.getSipServiceHandler().updateWithCallStatus(CallExtras.StatusCode.YO_INV_STATE_CONNECTED);
                        }
                    } else if (mediaInfo.getStatus() == pjsua_call_media_status.PJSUA_CALL_MEDIA_LOCAL_HOLD) {
                        yoSipService.getSipServiceHandler().updateWithCallStatus(CallExtras.StatusCode.YO_CALL_MEDIA_LOCAL_HOLD);
                        yoSipService.setLocalHold(true);
                        if (!yoSipService.isRemoteHold()) {
                            yoSipService.setRemoteHold(false);
                        }
                    }
                    DialerLogs.messageI(TAG, "Medis Status =" + mediaInfo.getStatus());
                }
            }
        } catch (Exception e) {
            DialerLogs.messageE(TAG, "notifyCallMediaState===========" + e.getMessage());
        }
    }

    @Override
    public void notifyBuddyState(YoBuddy buddy) {
        DialerLogs.messageI(TAG, "notifyBuddyState===========");
    }
}

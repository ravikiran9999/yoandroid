package com.yo.dialer.yopj;

import android.content.Context;

import com.yo.dialer.CallExtras;
import com.yo.dialer.CallStateHandler;
import com.yo.dialer.DialerLogs;
import com.yo.dialer.YoSipService;

import org.pjsip.pjsua2.BuddyInfo;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallMediaInfo;
import org.pjsip.pjsua2.CallMediaInfoVector;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.PresenceStatus;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_status_code;
import org.pjsip.pjsua2.pjsua_call_media_status;

import static org.pjsip.pjsua2.pjrpid_activity.PJRPID_ACTIVITY_BUSY;
import static org.pjsip.pjsua2.pjsip_status_code.PJSIP_SC_REQUEST_TIMEOUT;
import static org.pjsip.pjsua2.pjsua_buddy_status.PJSUA_BUDDY_STATUS_ONLINE;

/**
 * Created by Rajesh Babu on 12/7/17.
 */

public class YoCallObserver implements YoAppObserver {
    private static final String TAG = YoCallObserver.class.getSimpleName();
    private static YoCallObserver yoCallObserver;
    private static Context mContext;

    public static YoCallObserver getInstance(Context context) {
        mContext = context;
        if (yoCallObserver == null) {
            yoCallObserver = new YoCallObserver();
        }
        return yoCallObserver;
    }

    @Override
    public void notifyRegState(pjsip_status_code code, String reason, int expiration, String wholeMsg) {
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
        yoSipService.getYoAccount().setRegistrationPending(false);
        if (reason != null && reason.equalsIgnoreCase(CallExtras.NETWORK_NOT_REACHABLE)) {
            if (yoSipService instanceof YoSipService) {
                yoSipService.setCallStatus(CallExtras.StatusCode.YO_CALL_NETWORK_NOT_REACHABLE);
                yoSipService.getPreferenceEndPoint().saveIntPreference(CallExtras.REGISTRATION_STATUS, CallExtras.StatusCode.YO_CALL_NETWORK_NOT_REACHABLE);
            }
        } else if (code == PJSIP_SC_REQUEST_TIMEOUT) {
            yoSipService.getPreferenceEndPoint().saveIntPreference(CallExtras.REGISTRATION_STATUS, CallExtras.StatusCode.YO_REQUEST_TIME_OUT);
            yoSipService.callDisconnected(CallExtras.StatusCode.YO_NEXGE_SERVER_DOWN, "Registration request timeout", "This may be nexge issue, as registration requst timeout. Reason = " + reason + ", Code is " + code + ", Whole msg  =" + wholeMsg);
        } else {
            yoSipService.getPreferenceEndPoint().saveIntPreference(CallExtras.REGISTRATION_STATUS, 0);
        }
        yoSipService.getPreferenceEndPoint().saveStringPreference(CallExtras.REGISTRATION_STATUS_MESSAGE, registrationStatus);
        DialerLogs.messageI(TAG, "Registration Status " + registrationStatus);
        DialerLogs.messageI(TAG, "notifyRegState>>>> " + registrationStatus);
        updateBuddyState(yoSipService);
    }

    @Override
    public void notifyIncomingCall(YoCall call) {
        YoCall yoCall = YoSipService.getYoCurrentCall();
        DialerLogs.messageI(TAG, "notifyIncomingCall===========Getting another call but current call object is" + yoCall);


        try {
            DialerLogs.messageI(TAG, "notifyIncomingCall===========" + call.getInfo().getState());

            if (yoCall != null) {
                DialerLogs.messageI(TAG, "notifyIncomingCall=========Make it missed call " + call.getInfo().getRemoteUri());
                ((YoSipService) mContext).uploadGoogleSheet(CallExtras.StatusCode.BUSY, "Busy", "Already user is in call with" + yoCall.getInfo().getRemoteUri() + ", So sending Busy to " + call.getInfo().getRemoteUri() + ", Call-Id " + call.getInfo().getCallIdString(), 0,call.getInfo().getRemoteUri());
                ((YoSipService) mContext).handleBusy(call);
                return;
            } else {
                if (mContext != null) {
                    if (mContext instanceof YoSipService) {
                        ((YoSipService) mContext).OnIncomingCall(call);
                    }
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
        } catch (Exception e) {
            DialerLogs.messageI(TAG, "notifyIncomingCall===========" + e.getMessage());
        }
    }

    @Override
    public void notifyCallState(YoCall call) {
        try {
            CallInfo info = call.getInfo();
            DialerLogs.messageI(TAG, "notifyCallState===========State =" + info.getState() + ",Reason" + info.getLastReason() + info.getStateText());
            YoSipService yoSipService = (YoSipService) YoCallObserver.mContext;
            CallStateHandler.verify(yoSipService, info);
        } catch (Exception e) {
            DialerLogs.messageE(TAG, "notifyCallState===========" + e.getMessage());
        }
    }


    @Override
    public void notifyCallMediaState(YoCall call) {
        try {
            YoSipService yoSipService = (YoSipService) YoCallObserver.mContext;
            YoCall yoCurrentCall = yoSipService.getYoCurrentCall();
            if (yoCurrentCall != null) {
                yoCurrentCall.setPendingReInvite(false);
            }
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

    private void updateBuddyState(YoSipService yoSipService) {
        try {
            PresenceStatus ps = new PresenceStatus();
            ps.setStatus(PJSUA_BUDDY_STATUS_ONLINE);
            // Optional, set the activity and some note
            ps.setActivity(PJRPID_ACTIVITY_BUSY);
            ps.setNote("On the phone");
            if (yoSipService != null) {
                YoAccount yoAccount = yoSipService.getYoAccount();
                if (yoAccount != null) {
                    yoAccount.setOnlineStatus(ps);
                }
            }
        } catch (Exception e) {
            DialerLogs.messageE(TAG, "While setting update Buddy State got an exception " + e.getMessage());
        }
    }

    @Override
    public void notifyBuddyState(YoBuddy buddy) {
        if (buddy != null) {
            BuddyInfo info = null;
            try {
                info = buddy.getInfo();
            } catch (Exception e) {


            }
            DialerLogs.messageI(TAG, "notifyBuddyState===========" + info.getUri() + ", State = " + info.getPresStatus().getStatusText());
        }
    }
}

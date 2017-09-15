package com.yo.dialer;

import com.yo.android.BuildConfig;
import com.yo.android.calllogs.CallLog;
import com.yo.android.model.Contact;
import com.yo.android.model.dialer.OpponentDetails;
import com.yo.dialer.yopj.YoCallObserver;

import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.pjsip_inv_state;

import de.greenrobot.event.EventBus;

import static com.yo.dialer.CallExtras.StatusCode.BUSY;
import static com.yo.dialer.CallExtras.StatusCode.FORBIDDEN;
import static com.yo.dialer.CallExtras.StatusCode.OTHER;
import static com.yo.dialer.CallExtras.StatusCode.YO_NEXGE_SERVER_DOWN;

/**
 * Created by root on 23/8/17.
 */

public class CallStateHandler {
    private static final String TAG = CallStateHandler.class.getSimpleName();
    private static boolean isRinging;

    public static void verify(YoSipService yoSipService, CallInfo info) {
        if (info.getState() == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
            yoSipService.setCallAccepted(false);
            if (info.getLastReason().equalsIgnoreCase(CallExtras.StatusReason.YO_NOT_ACCEPTABLE_HERE)) {
                checkMissedCall(yoSipService);
                yoSipService.getSipServiceHandler().updateWithCallStatus(CallExtras.StatusCode.YO_INV_STATE_SC_NO_ANSWER);
                yoSipService.callDisconnected(CallExtras.StatusCode.NO_ANSWER_NOT_ACCEPTABLE, "Call Not acceptable", "While call state got notified, got a reason that call not acceptable, so sending no answer." + info.getCallIdString());
            } else if (info.getLastReason().equalsIgnoreCase(CallExtras.StatusReason.YO_NETWORK_IS_UNREACHABLE)) {
                yoSipService.sendNoNetwork();
            } else if (info.getLastReason().equalsIgnoreCase(CallExtras.StatusReason.YO_SERVICE_UNAVAILABLE)
                    || info.getLastReason().equalsIgnoreCase(CallExtras.StatusReason.YO_NOT_FOUND)
                    ) {
                Contact contact = yoSipService.getCalleeContact();
                yoSipService.callDisconnected(CallExtras.StatusCode.USER_NOT_FOUND_SERVICE_NOT_AVAILABLE, "Service not available/User not online", "Got service not available or user not found, if its PSTN call need to check with Gateway people for not supporting " + info.getCallIdString());
                DialerLogs.messageI(TAG, yoSipService.phoneNumber + "Service not available or user not found so PSTN dialog");
                //dont  show PSTN dialog if the call is PSTN
                if (yoSipService.phoneNumber.contains(BuildConfig.RELEASE_USER_TYPE)) {
                    isContactUser(yoSipService, contact);
                    OpponentDetails details = new OpponentDetails(contact.getPhoneNo(), contact, CallExtras.StatusCode.YO_INV_STATE_CALLEE_NOT_ONLINE);
                    EventBus.getDefault().post(details);
                } else {
                    DialerLogs.messageI(TAG, yoSipService.phoneNumber + "Call is already PSTN CALL so just dropping the call.");
                }
            } else if (info.getLastReason().equalsIgnoreCase(CallExtras.StatusReason.YO_NEXGE_SERVER_DOWN)
                    || info.getLastReason().equalsIgnoreCase(CallExtras.StatusReason.YO_REQUEST_TIMEOUT)) {
                yoSipService.getSipServiceHandler().updateWithCallStatus(CallExtras.StatusCode.YO_INV_STATE_DISCONNECTED);
                yoSipService.callDisconnected(YO_NEXGE_SERVER_DOWN, "Connect refused/Request Timeout", "This may be Nexge server down." + info.getCallIdString());
            } else if (info.getLastReason().equalsIgnoreCase(CallExtras.StatusReason.YO_BUSY_HERE)) {
                yoSipService.getSipServiceHandler().updateWithCallStatus(CallExtras.StatusCode.YO_BUSY_HERE);
                yoSipService.callDisconnected(BUSY, "Busy", "Getting Busy here, " + info.getCallIdString());
            } else if (info.getLastReason().equalsIgnoreCase(CallExtras.StatusReason.YO_FORBIDDEN)) {
                yoSipService.callDisconnected(FORBIDDEN, "Forbidden", "Forbidden " + info.getCallIdString());
            } else {
                yoSipService.getSipServiceHandler().updateWithCallStatus(CallExtras.StatusCode.YO_INV_STATE_DISCONNECTED);
                yoSipService.callDisconnected(OTHER, "Call got disconnected", "Call got disconnected because of " + info.getLastReason()+", "+info.getCallIdString());
            }
            isRinging = false;
        } else if (info.getState() == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
            isRinging = false;
            yoSipService.getSipServiceHandler().updateWithCallStatus(CallExtras.StatusCode.YO_INV_STATE_CONNECTED);
            yoSipService.setCallAccepted(true);
            yoSipService.callAccepted();
        } else if (info.getState() == pjsip_inv_state.PJSIP_INV_STATE_EARLY && (info.getLastReason().equalsIgnoreCase(CallExtras.StatusReason.YO_RINGING))) {
            yoSipService.getSipServiceHandler().updateWithCallStatus(CallExtras.StatusCode.YO_INV_STATE_SC_RINGING);
            isRinging = true;
        } else if (info.getState() == pjsip_inv_state.PJSIP_INV_STATE_CALLING) {
            isRinging = false;
            yoSipService.getSipServiceHandler().updateWithCallStatus(CallExtras.StatusCode.YO_INV_STATE_SC_CALLING);
        } else {
            DialerLogs.messageE(TAG, "notifyCallState Other case===========" + info.getState());
            yoSipService.getSipServiceHandler().updateWithCallStatus(CallExtras.StatusCode.YO_INV_STATE_SC_UNKNOWN);
        }
    }

    private static boolean isContactUser(YoSipService yoSipService, Contact contact) {
        if (contact.getPhoneNo() != null) {
            return true;
        } else {
            contact.setNexgieUserName(yoSipService.phoneNumber);
            contact.setCountryCode(DialerHelper.getCountryCodeFromNexgeUsername(yoSipService.phoneNumber));
            return false;
        }
    }

    private static void checkMissedCall(YoSipService yoSipService) {
        if (isRinging && yoSipService.getCallType() == CallLog.Calls.INCOMING_TYPE) {
            isRinging = false;
            yoSipService.sendMissedCallNotification();
        }
    }
}

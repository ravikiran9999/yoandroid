package com.yo.dialer.yopj;

import android.util.Log;

import com.yo.dialer.DialerLogs;

import org.pjsip.pjsua2.AudioMedia;
import org.pjsip.pjsua2.Call;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallMediaInfo;
import org.pjsip.pjsua2.CallMediaInfoVector;
import org.pjsip.pjsua2.Media;
import org.pjsip.pjsua2.OnCallMediaStateParam;
import org.pjsip.pjsua2.OnCallStateParam;
import org.pjsip.pjsua2.OnCallTsxStateParam;
import org.pjsip.pjsua2.VideoPreview;
import org.pjsip.pjsua2.VideoWindow;
import org.pjsip.pjsua2.pjmedia_type;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_tsx_state_e;
import org.pjsip.pjsua2.pjsua2;
import org.pjsip.pjsua2.pjsua_call_media_status;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by root on 17/7/17.
 */

public class YoCall extends Call {
    private static final String TAG = YoCall.class.getSimpleName();
    public VideoWindow vidWin;
    public VideoPreview vidPrev;

    public boolean isPendingReInvite() {
        return isPendingReInvite;
    }

    public void setPendingReInvite(boolean pendingReInvite) {
        isPendingReInvite = pendingReInvite;
    }

    private boolean isPendingReInvite;

    public YoCall(YoAccount acc, int call_id) {
        super(acc, call_id);
        vidWin = null;
    }

    @Override
    public void onCallState(OnCallStateParam prm) {
        DialerLogs.messageI(TAG, "YO========onCallState===========");
        YoApp.observer.notifyCallState(this);
        try {
            CallInfo ci = getInfo();
            if (ci.getState() ==
                    pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
                this.delete();
            }
        } catch (Exception e) {
            return;
        }
    }

    @Override
    public void onCallMediaState(OnCallMediaStateParam prm) {
        DialerLogs.messageI(TAG, "YO========onCallMediaState===========");
        CallInfo ci;
        try {
            ci = getInfo();
        } catch (Exception e) {
            return;
        }

        CallMediaInfoVector cmiv = ci.getMedia();

        for (int i = 0; i < cmiv.size(); i++) {
            CallMediaInfo cmi = cmiv.get(i);
            if (cmi.getType() == pjmedia_type.PJMEDIA_TYPE_AUDIO &&
                    (cmi.getStatus() ==
                            pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE ||
                            cmi.getStatus() ==
                                    pjsua_call_media_status.PJSUA_CALL_MEDIA_REMOTE_HOLD)) {
                // unfortunately, on Java too, the returned Media cannot be
                // downcasted to AudioMedia
                Media m = getMedia(i);
                AudioMedia am = AudioMedia.typecastFromMedia(m);

                try {
                    am.adjustRxLevel((float) 1.5);
                    am.adjustTxLevel((float) 1.5);
                } catch (Exception exc) {
                    Log.e(TAG, "Error while adjusting levels", exc);
                }

                // connect ports
                try {
                    YoApp.ep.audDevManager().getCaptureDevMedia().
                            startTransmit(am);
                    am.startTransmit(YoApp.ep.audDevManager().
                            getPlaybackDevMedia());
                } catch (Exception e) {
                    continue;
                }
            } else if (cmi.getType() == pjmedia_type.PJMEDIA_TYPE_VIDEO &&
                    cmi.getStatus() ==
                            pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE &&
                    cmi.getVideoIncomingWindowId() != pjsua2.INVALID_ID) {
                vidWin = new VideoWindow(cmi.getVideoIncomingWindowId());
                vidPrev = new VideoPreview(cmi.getVideoCapDev());
            }
        }

        YoApp.observer.notifyCallMediaState(this);
    }

    @Override
    public void onCallTsxState(OnCallTsxStateParam prm) {
        DialerLogs.messageI(TAG, "YO========onCallTsxState===========");

        // Check if previous INVITE/UPDATE transaction has been completed
        if (prm.getE().getBody().getTsxState().getTsx().getState() == pjsip_tsx_state_e.PJSIP_TSX_STATE_TERMINATED &&
                (prm.getE().getBody().getTsxState().getTsx().getMethod()=="INVITE" || prm.getE().getBody().getTsxState().getTsx().getMethod()=="UPDATE"))
        {
            YoApp.observer.notifyCallTsxState(this);
        }
    }
}

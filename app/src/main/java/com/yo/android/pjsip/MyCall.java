package com.yo.android.pjsip;

import android.util.Log;

import org.pjsip.pjsua2.AudDevManager;
import org.pjsip.pjsua2.AudioMedia;
import org.pjsip.pjsua2.Call;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallMediaInfo;
import org.pjsip.pjsua2.Media;
import org.pjsip.pjsua2.OnCallMediaStateParam;
import org.pjsip.pjsua2.OnCallStateParam;
import org.pjsip.pjsua2.VideoPreview;
import org.pjsip.pjsua2.VideoWindow;
import org.pjsip.pjsua2.pjmedia_type;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsua_call_media_status;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

public class MyCall extends Call {
    public VideoWindow vidWin;
    public VideoPreview vidPrev;

    MyCall(MyAccount acc, int call_id) {
        super(acc, call_id);
        vidWin = null;
    }

    @Override
    public void onCallState(OnCallStateParam prm) {
        MyApp.observer.notifyCallState(this);
        try {
            CallInfo ci = getInfo();
            if (ci.getState() == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
                this.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    @Override
    public void onCallMediaState(OnCallMediaStateParam prm) {
        CallInfo info;
        try {
            info = getInfo();
        } catch (Exception exc) {
            Log.e(TAG, "onCallMediaState: error while getting call info", exc);
            return;
        }

        for (int i = 0; i < info.getMedia().size(); i++) {
            Media media = getMedia(i);
            CallMediaInfo mediaInfo = info.getMedia().get(i);

            if (mediaInfo.getType() == pjmedia_type.PJMEDIA_TYPE_AUDIO
                    && media != null
                    && mediaInfo.getStatus() == pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE) {
                AudioMedia audioMedia = AudioMedia.typecastFromMedia(media);


                // connect the call audio media to sound device
                try {
                    AudDevManager mgr = MyApp.mEndpoint.audDevManager();

                    try {
                        audioMedia.adjustRxLevel((float) 1.5);
                        audioMedia.adjustTxLevel((float) 1.5);
                    } catch (Exception exc) {
                        Log.e(TAG, "Error while adjusting levels", exc);
                    }

                    audioMedia.startTransmit(mgr.getPlaybackDevMedia());
                    mgr.getCaptureDevMedia().startTransmit(audioMedia);
                } catch (Exception exc) {
                    Log.e(TAG, "Error while connecting audio media to sound device", exc);
                }
            }
        }

        MyApp.observer.notifyCallMediaState(this);
    }
}

package com.yo.dialer;

import android.content.Intent;
import android.util.Log;

import com.yo.dialer.yopj.YoAccount;
import com.yo.dialer.yopj.YoApp;
import com.yo.dialer.yopj.YoCall;

import org.pjsip.pjsua2.AudDevManager;
import org.pjsip.pjsua2.AudioMedia;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallMediaInfo;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.Media;
import org.pjsip.pjsua2.pjmedia_type;
import org.pjsip.pjsua2.pjsip_status_code;
import org.pjsip.pjsua2.pjsua_call_media_status;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Rajesh Babu on 18/7/17.
 */

public class CallHelper {
    private static final String TAG = CallHelper.class.getSimpleName();

    public static YoCall makeCall(YoAccount yoAccount, Intent intent) {
        if (intent != null) {
            if (intent.hasExtra(CallExtras.CALLER_NO)) {
                DialerLogs.messageI(TAG, "YO========Making makeCall===========");
                try {
                    DialerLogs.messageI(TAG, "YO=========Caller Number==========" + yoAccount.getInfo().getUri());
                    String calleeNumber = prepareDestinationDetails(intent);
                    final YoCall call = new YoCall(yoAccount, -1);
                    CallOpParam prm = new CallOpParam(true);
                    try {
                        String dst_uri = "sip:" + calleeNumber + "@" + DialerConfig.NEXGE_SERVER_IP + ":" + DialerConfig.NEXGE_SERVER_TCP_PORT + DialerConfig.TCP;
                        DialerLogs.messageI(TAG, "YO=========Callee URI==========" + dst_uri);
                        call.makeCall(dst_uri, prm);
                        return call;
                    } catch (Exception e) {
                        e.printStackTrace();
                        call.delete();
                        return null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {

            }
        } else {
            DialerLogs.messageE(TAG, "YO=========makeCall Intent object is null==========");
        }
        return null;
    }

    private static String prepareDestinationDetails(Intent intent) {
        DialerLogs.messageI(TAG, "YO===================" + intent.getStringExtra(CallExtras.CALLER_NO));
        return intent.getStringExtra(CallExtras.CALLER_NO);
    }

    public static void accetpCall(YoCall yoCurrentCall) {
        if (yoCurrentCall != null) {
            CallOpParam call_param = new CallOpParam();
            call_param.setStatusCode(pjsip_status_code.PJSIP_SC_OK);
            try {
                DialerLogs.messageI(TAG, "YO=======Accepting call============" + yoCurrentCall.getInfo().getCallIdString());
                yoCurrentCall.answer(call_param);
            } catch (Exception e) {
                DialerLogs.messageI(TAG, "YO====While accepting call====" + e.getMessage());
                return;
            }
        } else {
            DialerLogs.messageI(TAG, "YO====Current Call Object is null====");

        }
    }

    public static void rejectCall(YoCall yoCurrentCall) {
        endCall(yoCurrentCall);
    }

    public static void endCall(YoCall yoCurrentCall) {
        CallOpParam param = new CallOpParam();
        param.setStatusCode(pjsip_status_code.PJSIP_SC_DECLINE);
        try {
            if (yoCurrentCall != null) {
                storeDump(yoCurrentCall);
                yoCurrentCall.hangup(param);
            } else {
                DialerLogs.messageI(TAG, "YO====Current Call Object is null====");
            }
        } catch (Exception exc) {
            DialerLogs.messageE(TAG, "YO====While End call====" + exc.getMessage());
        }
    }

    public static void storeDump(YoCall yoCurrentCall) {
        try {
            String dumpString = yoCurrentCall.dump(true, "");
            DialerLogs.messageI(TAG, "YO====The call disconnected dump string is====" + dumpString);
            appendLog(dumpString);
        } catch (Exception e) {
            DialerLogs.messageE(TAG, "YO====While Store DUMP call====" + e.getMessage());
        }
    }

    private static void appendLog(String text) {
        File logFile = new File("sdcard/calldump.file");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                DialerLogs.messageE(TAG, "YO====While Store DUMP appendLog====" + e.getMessage());
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            DialerLogs.messageE(TAG, "YO====While Store DUMP appendLog====" + e.getMessage());
        }
    }

    public static void setMute(YoApp yoapp, YoCall currentCall, boolean mute) {
        if (yoapp != null) {
            if (currentCall != null) {
                CallInfo info;
                try {
                    info = currentCall.getInfo();
                } catch (Exception exc) {
                    return;
                }

                for (int i = 0; i < info.getMedia().size(); i++) {
                    Media media = currentCall.getMedia(i);
                    CallMediaInfo mediaInfo = info.getMedia().get(i);

                    if (mediaInfo.getType() == pjmedia_type.PJMEDIA_TYPE_AUDIO
                            && media != null
                            && mediaInfo.getStatus() == pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE) {
                        AudioMedia audioMedia = AudioMedia.typecastFromMedia(media);

                        // connect or disconnect the captured audio
                        try {
                            AudDevManager mgr = yoapp.ep.audDevManager();

                            if (mute) {
                                mgr.getCaptureDevMedia().stopTransmit(audioMedia);
                            } else {
                                mgr.getCaptureDevMedia().startTransmit(audioMedia);
                            }

                        } catch (Exception exc) {
                        }
                    }
                }
            } else {
                DialerLogs.messageE(TAG, "Current call is null so call to mute/unmute");
            }
        } else {
            DialerLogs.messageE(TAG, "Yoapp is null so call to mute/unmute");
        }
    }

    public static void holdCall(YoCall currentCall) {
        if (currentCall != null) {
            CallOpParam prm = new CallOpParam(true);
            try {
                currentCall.setHold(prm);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            DialerLogs.messageE(TAG, "Current call is null so call not hold");
        }
    }

    public static void unHoldCall(YoCall currentCall) {
        CallOpParam prm = new CallOpParam(true);
        prm.getOpt().setFlag(1);
        if (currentCall != null) {
            try {
                currentCall.reinvite(prm);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            DialerLogs.messageE(TAG, "Current call is null so call not unhold");
        }
    }
}

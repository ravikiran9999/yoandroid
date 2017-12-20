package com.yo.dialer;

import android.content.Intent;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.BuildConfig;
import com.yo.android.pjsip.SipHelper;
import com.yo.android.util.Constants;
import com.yo.dialer.googlesheet.UploadCallDetails;
import com.yo.dialer.googlesheet.UploadModel;
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
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Rajesh Babu on 18/7/17.
 */


public class CallHelper {
    private static final String TAG = CallHelper.class.getSimpleName();
    public static String dumpString;

    public static YoCall makeCall(YoSipService sipService, YoAccount yoAccount, Intent intent) {
        if (intent != null) {
            if (intent.hasExtra(CallExtras.CALLER_NO)) {
                DialerLogs.messageI(TAG, "Making makeCall===========" + intent.getStringExtra(CallExtras.CALLER_NO));
                try {
                    String calleeNumber = prepareDestinationDetails(intent);
                    DialerLogs.messageI(TAG, " makeCall Callee Number==========" + calleeNumber);
                    if(yoAccount != null) {
                        final YoCall call = new YoCall(yoAccount, -1);
                        CallOpParam prm = new CallOpParam(true);
                        String callId = null;
                        try {
                            // String dst_uri = "sip:" + calleeNumber + "@" + DialerConfig.NEXGE_SERVER_IP + ":" + DialerConfig.NEXGE_SERVER_TCP_PORT + DialerConfig.TCP;
                            String dst_uri = String.format("\"%s\" <sip:%s@%s>", calleeNumber, calleeNumber, DialerConfig.NEXGE_SERVER_IP + ":" + DialerConfig.NEXGE_SERVER_TCP_PORT);
                            DialerLogs.messageI(TAG, "Callee URI==========" + dst_uri);
                            call.makeCall(dst_uri, prm);
                            return call;
                        } catch (Exception e) {
                            e.printStackTrace();
                            callId = call.getInfo().getCallIdString();
                            call.delete();
                            DialerLogs.messageE(TAG, "makeCall==========" + e.getMessage());
                            sipService.callDisconnected(CallExtras.StatusCode.OTHER + "", e.getMessage(), "While making call got an exception and message is " + e.getMessage() + ", So that call is going to disconnecting." + callId);
                            sipService.setYoAccount(null);
                            sipService.register(null);
                            SipHelper.isAlreadyStarted = false;
                            return null;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                DialerLogs.messageE(TAG, "YO=========Intent does not contain CALLER NO VALUE==========");

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
                DialerLogs.messageI(TAG, "Accepting call============" + yoCurrentCall.getInfo().getCallIdString());
                yoCurrentCall.answer(call_param);
            } catch (Exception e) {
                DialerLogs.messageI(TAG, "While accepting call====" + e.getMessage());
                return;
            }
        } else {
            DialerLogs.messageI(TAG, "Current Call Object is null====");

        }
    }


    public static void rejectCall(YoCall yoCurrentCall) {
        endCall(yoCurrentCall);
    }

    public static void endCall(YoCall yoCurrentCall) {
        DialerLogs.messageI(TAG, "Sending Hangup  Call...");
        CallOpParam param = new CallOpParam();
        param.setStatusCode(pjsip_status_code.PJSIP_SC_DECLINE);
        try {
            if (yoCurrentCall != null) {
                dumpString = storeDump(yoCurrentCall);
                yoCurrentCall.hangup(param);
                YoSipService.setYoCurrentCall(null);
            } else {
                DialerLogs.messageI(TAG, "Current Call Object is null====");
            }
        } catch (Exception exc) {
            YoSipService.setYoCurrentCall(null);
            DialerLogs.messageE(TAG, "While End call====" + exc.getMessage());
        }
    }

    public static String storeDump(YoCall yoCurrentCall) {
        String dumpString = "";
        try {
            dumpString = yoCurrentCall.dump(true, "");
            DialerLogs.messageI(TAG, "The call disconnected dump string is====" + dumpString);
            appendLog(dumpString);
        } catch (Exception e) {
            DialerLogs.messageE(TAG, "While Store DUMP call====" + e.getMessage());
        }

        return dumpString;
    }

    public static void appendLog(String text) {
        File logFile = new File("sdcard/calldump.file");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                DialerLogs.messageE(TAG, "While Store DUMP appendLog====" + e.getMessage());
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            DialerLogs.messageE(TAG, "While Store DUMP appendLog====" + e.getMessage());
        }
    }

    public static void setMute(YoApp yoapp, YoCall currentCall, boolean mute) {
        DialerLogs.messageE(TAG, "setMute first line");
        if (yoapp != null) {
            if (currentCall != null) {
                CallInfo info;
                try {
                    DialerLogs.messageE(TAG, "setMute try block");
                    info = currentCall.getInfo();
                } catch (Exception exc) {
                    DialerLogs.messageE(TAG, "setMute catch block");
                    return;
                }

                for (int i = 0; i < info.getMedia().size(); i++) {
                    Media media = currentCall.getMedia(i);
                    CallMediaInfo mediaInfo = info.getMedia().get(i);

                    DialerLogs.messageE(TAG, "setMute mediaInfo " + mediaInfo.getType() + media + mediaInfo.getStatus());

                    if (mediaInfo.getType() == pjmedia_type.PJMEDIA_TYPE_AUDIO
                            && media != null && mediaInfo.getStatus() == pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE) {
                        AudioMedia audioMedia = AudioMedia.typecastFromMedia(media);

                        DialerLogs.messageE(TAG, "setMute mediaInfo if audioMedia " + audioMedia);

                        // connect or disconnect the captured audio
                        try {
                            AudDevManager mgr = yoapp.ep.audDevManager();

                            if (mute) {
                                mgr.getCaptureDevMedia().stopTransmit(audioMedia);
                                DialerLogs.messageE(TAG, "Setting mute on");
                            } else {
                                mgr.getCaptureDevMedia().startTransmit(audioMedia);
                                DialerLogs.messageE(TAG, "Setting mute off");
                            }

                        } catch (Exception exc) {
                            DialerLogs.messageE(TAG, "Exception while setting mute " + exc.getMessage());
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

    public static void holdCall(YoCall currentCall, PreferenceEndPoint preferenceEndPoint, String phoneNumber) {
        if (currentCall != null) {
            CallOpParam prm = new CallOpParam(true);
            try {
                if(!currentCall.isPendingReInvite()) {
                    currentCall.setHold(prm);
                    currentCall.setPendingReInvite(true);
                    uploadToGoogleSheet(preferenceEndPoint, phoneNumber, "Hold On");
                } else {
                    DialerLogs.messageE(TAG, "Reinvite pending so can't set onhold");
                    currentCall.setPendingReInvite(true);
                    YoSipService.changeHoldUI = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                currentCall.setPendingReInvite(true);
                YoSipService.changeHoldUI = false;
                uploadToGoogleSheet(preferenceEndPoint, phoneNumber, "Hold On failed because of " + e.getMessage());
            }
        } else {
            DialerLogs.messageE(TAG, "Current call is null so call not hold");
        }
    }

    public static void unHoldCall(YoCall currentCall) throws Exception {
        CallOpParam prm = new CallOpParam(true);
        prm.getOpt().setFlag(1);
        if (currentCall != null) {
            if(!currentCall.isPendingReInvite()) {
                currentCall.reinvite(prm);
                currentCall.setPendingReInvite(true);
            } else {
                DialerLogs.messageE(TAG, "Reinvite pending so can't set unhold");
                currentCall.setPendingReInvite(true);
                YoSipService.changeHoldUI = false;
            }
        } else {
            DialerLogs.messageE(TAG, "Current call is null so call not unhold");
        }
    }

    public static void uploadToGoogleSheet(PreferenceEndPoint preferenceEndPoint, String phoneNumber, String comments) {
        if (DialerConfig.UPLOAD_REPORTS_GOOGLE_SHEET) {
            try {
                UploadModel model = new UploadModel(preferenceEndPoint);
                model.setCaller(preferenceEndPoint.getStringPreference(Constants.VOX_USER_NAME));
                model.setCallee(phoneNumber);
                String callee = model.getCallee();
                if (callee != null && callee.contains(BuildConfig.RELEASE_USER_TYPE)) {
                    model.setCallMode("App to App");
                } else {
                    model.setCallMode("App to PSTN");
                }

                model.setComments(comments);

                Calendar c = Calendar.getInstance();
                String formattedDate = YoSipService.df.format(c.getTime());
                model.setDate(formattedDate);
                Date d = new Date();
                String currentDateTimeString = YoSipService.sdf.format(d);
                model.setTime(currentDateTimeString);

                UploadCallDetails.postDataFromApi(model, "Hold");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void uploadToGoogleSheetBalanceFail(PreferenceEndPoint preferenceEndPoint, String phoneNumber, String name, String comments) {
        if (DialerConfig.UPLOAD_REPORTS_GOOGLE_SHEET) {
            try {
                UploadModel model = new UploadModel(preferenceEndPoint);
                model.setCaller(preferenceEndPoint.getStringPreference(Constants.VOX_USER_NAME));
                model.setCallee(phoneNumber);
                model.setToName(name);

                model.setComments(comments);

                Calendar c = Calendar.getInstance();
                String formattedDate = YoSipService.df.format(c.getTime());
                model.setDate(formattedDate);
                Date d = new Date();
                String currentDateTimeString = YoSipService.sdf.format(d);
                model.setTime(currentDateTimeString);

                UploadCallDetails.postDataFromApi(model, "BalanceFailures");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void uploadToGoogleSheetMessageSentFail(PreferenceEndPoint preferenceEndPoint, String phoneNumber, String name, String comments) {
        if (DialerConfig.UPLOAD_REPORTS_GOOGLE_SHEET) {
            try {
                UploadModel model = new UploadModel(preferenceEndPoint);
                model.setCaller(preferenceEndPoint.getStringPreference(Constants.VOX_USER_NAME));
                model.setCallee(phoneNumber);
                model.setToName(name);

                model.setComments(comments);

                Calendar c = Calendar.getInstance();
                String formattedDate = YoSipService.df.format(c.getTime());
                model.setDate(formattedDate);
                Date d = new Date();
                String currentDateTimeString = YoSipService.sdf.format(d);
                model.setTime(currentDateTimeString);

                UploadCallDetails.postDataFromApi(model, "Chat");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

package com.yo.dialer.yopj;

import com.yo.android.pjsip.MyBuddy;
import com.yo.dialer.DialerLogs;
import com.yo.dialer.model.SipProperties;

import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.AudioMedia;
import org.pjsip.pjsua2.BuddyConfig;
import org.pjsip.pjsua2.CodecInfo;
import org.pjsip.pjsua2.CodecInfoVector;
import org.pjsip.pjsua2.Endpoint;
import org.pjsip.pjsua2.EpConfig;
import org.pjsip.pjsua2.LogConfig;
import org.pjsip.pjsua2.StringVector;
import org.pjsip.pjsua2.TransportConfig;
import org.pjsip.pjsua2.UaConfig;
import org.pjsip.pjsua2.pj_log_decoration;
import org.pjsip.pjsua2.pjmedia_aud_dev_route;
import org.pjsip.pjsua2.pjsip_transport_type_e;

import java.util.ArrayList;

import static java.lang.System.err;


/**
 * Created by Rajesh Babu on 17/7/17.
 */

public class YoApp {
    private static final String TAG = YoApp.class.getSimpleName();

    static {
        try {
            System.loadLibrary("pjsua2");
            System.out.println("Library loaded");
        } catch (UnsatisfiedLinkError e) {
            DialerLogs.messageE(TAG, " UnsatisfiedLinkError: " + e.getMessage());
        }
    }

    public static Endpoint ep = new Endpoint();
    public static YoAppObserver observer;
    public ArrayList<YoAccount> accList = new ArrayList<YoAccount>();

    private EpConfig epConfig = new EpConfig();
    private TransportConfig sipTpConfig = new TransportConfig();

    /* Maintain reference to log writer to avoid premature cleanup by GC */
    private YoLogWriter logWriter;

    private final int SIP_PORT = 6000;
    private final int LOG_LEVEL = 5;
    SipProperties sipProperties;
    public static boolean isInitialized = false;

    public void init(SipProperties sipProperties, YoAppObserver obs, String app_dir) {
        this.sipProperties = sipProperties;
        isInitialized = true;
        init(obs, app_dir, false, sipProperties);

    }

    public void init(YoAppObserver obs, String app_dir,
                     boolean own_worker_thread, SipProperties sipProperties) {
        this.sipProperties = sipProperties;
        observer = obs;

	/* Create endpoint */
        try {
            ep.libCreate();
        } catch (Exception e) {
            return;
        }


	/* Override log level setting */
        epConfig.getLogConfig().setLevel(LOG_LEVEL);
        epConfig.getLogConfig().setConsoleLevel(LOG_LEVEL);

	/* Set log config. */
        LogConfig log_cfg = epConfig.getLogConfig();
        logWriter = new YoLogWriter();
        log_cfg.setWriter(logWriter);
        log_cfg.setDecor(log_cfg.getDecor() &
                ~(pj_log_decoration.PJ_LOG_HAS_CR.swigValue() |
                        pj_log_decoration.PJ_LOG_HAS_NEWLINE.swigValue()));

        /* Media config */
        epConfig.getMedConfig().setQuality(4);
        epConfig.getMedConfig().setNoVad(true);

	/* Set ua config. */
        UaConfig ua_cfg = epConfig.getUaConfig();
        ua_cfg.setUserAgent("Pjsua2 Android " + ep.libVersion().getFull());
        if (sipProperties.isStunEnable()) {
            StringVector stun_servers = new StringVector();
            stun_servers.add(sipProperties.getStunServer());
            ua_cfg.setStunServer(stun_servers);
        } else {
            DialerLogs.messageE(TAG, "YO====Stun server not enabled======");
        }

        if (own_worker_thread) {
            ua_cfg.setThreadCnt(0);
            ua_cfg.setMainThreadOnly(true);
        }

	/* Init endpoint */
        try {
            ep.libInit(epConfig);
            ep.audDevManager().setEcOptions(0, 0);
        } catch (Exception e) {
            return;
        }


        try {
            ep.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_TCP,
                    sipTpConfig);
        } catch (Exception e) {
            System.out.println(e);
        }

        /* Set SIP port back to default for JSON saved config */
        sipTpConfig.setPort(SIP_PORT);



	/* Start. */
        try {
            ep.libStart();

            ep.codecSetPriority("*", (short) 0);
            CodecInfoVector vector = ep.codecEnum();
            for (int i = 0; i < vector.size(); i++) {
                CodecInfo codecInfo = vector.get(i);
                DialerLogs.messageI(TAG, "YO=====ID=" + codecInfo.getCodecId() + ",Desc=" + codecInfo.getDesc() + ",Priority=" + codecInfo.getPriority());
            }
            ep.codecSetPriority("opus", (short) 1);
            ep.codecSetPriority("PCMA/8000", (short) 1);
            ep.codecSetPriority("PCMU/8000", (short) 1);


            /*ep.codecSetPriority("PCMA/8000", (short) 2);
            ep.codecSetPriority("PCMU/8000", (short) 3);
            ep.codecSetPriority("G722/8000", (short) 4);
            ep.codecSetPriority("iLBC/8000", (short) 5);
            ep.codecSetPriority("GSM/8000", (short) 6);*/

            //ep.codecSetPriority("G711/8000", (short) 1);
            ep.audDevManager().setInputRoute(pjmedia_aud_dev_route.PJMEDIA_AUD_DEV_ROUTE_CUSTOM);
            // ep.audDevManager().setVad(sipProperties.isVad());

        } catch (Exception e) {
            DialerLogs.messageE(TAG, "YO== Initialization of codecs Failed==" + e.getMessage());
            return;
        }
    }

    public YoAccount addAcc(AccountConfig cfg) {
        YoAccount acc = new YoAccount(cfg);
        try {
            acc.create(cfg);
        } catch (Exception e) {
            acc = null;
            return null;
        }

        accList.add(acc);
        createBuddy(acc, cfg);
        return acc;
    }

    private void createBuddy(YoAccount acc, AccountConfig acfg) {
        try {
            BuddyConfig cfg = new BuddyConfig();
            //cfg.setUri("\"919154512365\"<sip:youser919490570720D@173.82.147.172:6000>");
            cfg.setUri("\"919154512365\"<sip:youser919490570720D@185.106.240.205:6000>");
            YoBuddy buddy = new YoBuddy();
            buddy.create(acc, cfg);
            buddy.subscribePresence(true);
        } catch (Exception e) {
            DialerLogs.messageE(TAG, "While creating Buddy getting exception.");
        }
    }

    public void delAcc(YoAccount acc) {
        accList.remove(acc);
    }


    public void deinit() {

	/* Try force GC to avoid late destroy of PJ objects as they should be
    * deleted before lib is destroyed.
	*/
        Runtime.getRuntime().gc();

	/* Shutdown pjsua. Note that Endpoint destructor will also invoke
    * libDestroy(), so this will be a test of double libDestroy().
	*/
        try {
            ep.libDestroy();
        } catch (Exception e) {
        }

	/* Force delete Endpoint here, to avoid deletion from a non-
    * registered thread (by GC?).
	*/
        if (ep != null) {
            ep.delete();
        }
        ep = null;
    }

    //To test audio device working fine or not use this to play, it will play your voice.
    public void playMyVoice() throws Exception {
        AudioMedia play_med = ep.audDevManager().getPlaybackDevMedia();
        AudioMedia cap_med = ep.audDevManager().getCaptureDevMedia();
        cap_med.startTransmit(play_med);
    }
}

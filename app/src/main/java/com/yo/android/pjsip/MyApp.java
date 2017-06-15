package com.yo.android.pjsip;

import android.util.Log;

import com.orion.android.common.logging.Logger;
import com.yo.android.BuildConfig;
import com.yo.android.vox.CodecPriority;

import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.BuddyConfig;
import org.pjsip.pjsua2.CodecInfoVector;
import org.pjsip.pjsua2.ContainerNode;
import org.pjsip.pjsua2.Endpoint;
import org.pjsip.pjsua2.EpConfig;
import org.pjsip.pjsua2.JsonDocument;
import org.pjsip.pjsua2.LogConfig;
import org.pjsip.pjsua2.StringVector;
import org.pjsip.pjsua2.TransportConfig;
import org.pjsip.pjsua2.UaConfig;
import org.pjsip.pjsua2.pj_log_decoration;
import org.pjsip.pjsua2.pj_qos_type;
import org.pjsip.pjsua2.pjsip_transport_type_e;

import java.io.File;
import java.util.ArrayList;

class MyApp {


    private static final String TAG = MyApp.class.getSimpleName();

    static {
        try {
            System.loadLibrary("openh264");
            // Ticket #1937: libyuv is now included as static lib
            // System.loadLibrary("yuv");
        } catch (UnsatisfiedLinkError e) {
            System.out.println("UnsatisfiedLinkError: " + e.getMessage());
            System.out.println("This could be safely ignored if you "
                    + "don't need video.");
        }
        System.loadLibrary("pjsua2");
        System.out.println("Library loaded");
    }

    public static Endpoint mEndpoint = new Endpoint();
    public static MyAppObserver observer;
    public ArrayList<MyAccount> accList = new ArrayList<MyAccount>();

    private ArrayList<MyAccountConfig> accCfgs = new ArrayList<MyAccountConfig>();
    private EpConfig epConfig;
    private TransportConfig sipTpConfig = new TransportConfig();
    private String appDir;

    /* Maintain reference to log writer to avoid premature cleanup by GC */
    private MyLogWriter logWriter;

    private final String configName = "pjsua2.json";
    private final int SIP_PORT = 6000;
    private final int LOG_LEVEL = 5;
    public static String AGENT_NAME = "Yo! rv" + BuildConfig.VERSION_NAME;


    public void init(MyAppObserver obs, String app_dir) {
        init(obs, app_dir, false);
    }

    public void init(MyAppObserver obs, String app_dir,
                     boolean own_worker_thread) {
        observer = obs;
        appDir = app_dir;
        if (mEndpoint == null) {
            mEndpoint = new Endpoint();
        }

		/* Create endpoint */
        try {
            mEndpoint.libCreate();


            epConfig = new EpConfig();
            epConfig.getUaConfig().setUserAgent(AGENT_NAME);
            epConfig.getMedConfig().setHasIoqueue(true);
            epConfig.getMedConfig().setClockRate(16000);
            epConfig.getMedConfig().setQuality(10);
            epConfig.getMedConfig().setEcOptions(1);
            epConfig.getMedConfig().setEcTailLen(200);
            epConfig.getMedConfig().setThreadCnt(2);
            mEndpoint.libInit(epConfig);

            TransportConfig udpTransport = new TransportConfig();
            udpTransport.setQosType(pj_qos_type.PJ_QOS_TYPE_VOICE);
            TransportConfig tcpTransport = new TransportConfig();
            tcpTransport.setQosType(pj_qos_type.PJ_QOS_TYPE_VOICE);

            mEndpoint.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_UDP, udpTransport);
            mEndpoint.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_TCP, tcpTransport);
            mEndpoint.libStart();
            mEndpoint.codecSetPriority("*", (short) 0);
            mEndpoint.codecSetPriority("PCMA/8000", (short) 1);
            mEndpoint.codecSetPriority("PCMU/8000", (short) 1);
            mEndpoint.codecSetPriority("G722/8000", (short) 1);
            mEndpoint.codecSetPriority("G711/8000", (short) 1);
            mEndpoint.audDevManager().setOutputVolume(60);
            //Disabling VAD to get around NAT
            mEndpoint.audDevManager().setVad(false);
            Log.e(TAG, "SIP STATCK STARTED");

        } catch (Exception e) {
            //e.printStackTrace();
            //return;
        }

		/* Load config */
        String configPath = appDir + "/" + configName;
        Log.i("", "Configuration path" + configPath);
        File f = new File(configPath);
        if (f.exists()) {
            loadConfig(configPath);
        } else {
            /* Set 'default' values */
            sipTpConfig.setPort(SIP_PORT);
        }

		/* Override log level setting */
        epConfig.getLogConfig().setLevel(LOG_LEVEL);
        epConfig.getLogConfig().setConsoleLevel(LOG_LEVEL);

		/* Set log config. */
        LogConfig log_cfg = epConfig.getLogConfig();
        logWriter = new MyLogWriter();
        log_cfg.setWriter(logWriter);
        log_cfg.setDecor(log_cfg.getDecor()
                & ~(pj_log_decoration.PJ_LOG_HAS_CR.swigValue() | pj_log_decoration.PJ_LOG_HAS_NEWLINE
                .swigValue()));

		/* Set ua config. */
        UaConfig ua_cfg = epConfig.getUaConfig();
        ua_cfg.setUserAgent("Pjsua2 Android " + mEndpoint.libVersion().getFull());
        StringVector stun_servers = new StringVector();
        stun_servers.add("stun.pjsip.org");
        ua_cfg.setStunServer(stun_servers);
        if (own_worker_thread) {
            ua_cfg.setThreadCnt(0);
            ua_cfg.setMainThreadOnly(true);
        }

		/* Init endpoint */
        try {
            mEndpoint.libInit(epConfig);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

		/* Create transports. */
       /* try {
            mEndpoint.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_UDP,
                    sipTpConfig);
        } catch (Exception e) {
            System.out.println(e);
        }

        try {
            mEndpoint.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_TCP,
                    sipTpConfig);
        } catch (Exception e) {
            System.out.println(e);
        }*/

		/* Create accounts. */
        for (int i = 0; i < accCfgs.size(); i++) {
            MyAccountConfig my_cfg = accCfgs.get(i);

			/* Customize account config */
            my_cfg.accCfg.getNatConfig().setIceEnabled(false);
            my_cfg.accCfg.getNatConfig().setTurnEnabled(true);
            my_cfg.accCfg.getNatConfig().setTurnEnabled(true);
            my_cfg.accCfg.getVideoConfig().setAutoTransmitOutgoing(true);
            my_cfg.accCfg.getVideoConfig().setAutoShowIncoming(true);

            MyAccount acc = addAcc(my_cfg.accCfg);

            if (acc == null)
                continue;

			/* Add Buddies */
            for (int j = 0; j < my_cfg.buddyCfgs.size(); j++) {
                BuddyConfig bud_cfg = my_cfg.buddyCfgs.get(j);
                acc.addBuddy(bud_cfg);
            }
        }

		/* Start. */
        try {
            mEndpoint.libStart();
        } catch (Exception e) {
            return;
        }
    }

    public MyAccount addAcc(AccountConfig cfg) {
        MyAccount acc = new MyAccount(cfg);
        try {
            acc.create(cfg);
        } catch (Exception e) {
            Logger.warn("Exception while creating an account " + e.getMessage());
            acc = null;
            return null;
        }

        accList.add(acc);
        return acc;
    }

    public void delAcc(MyAccount acc) {
        accList.remove(acc);
    }

    private void loadConfig(String filename) {
        JsonDocument json = new JsonDocument();

        try {
            /* Load file */
            json.loadFile(filename);
            ContainerNode root = json.getRootContainer();

			/* Read endpoint config */
            epConfig.readObject(root);

			/* Read transport config */
            ContainerNode tp_node = root.readContainer("SipTransport");
            sipTpConfig.readObject(tp_node);

			/* Read account configs */
            accCfgs.clear();
            ContainerNode accs_node = root.readArray("accounts");
            while (accs_node.hasUnread()) {
                MyAccountConfig acc_cfg = new MyAccountConfig();
                acc_cfg.readObject(accs_node);
                accCfgs.add(acc_cfg);
            }
        } catch (Exception e) {
            System.out.println(e);
        }

		/*
         * Force delete json now, as I found that Java somehow destroys it after
		 * lib has been destroyed and from non-registered thread.
		 */
        json.delete();
    }

    private void buildAccConfigs() {
        /* Sync accCfgs from accList */
        accCfgs.clear();
        for (int i = 0; i < accList.size(); i++) {
            MyAccount acc = accList.get(i);


            MyAccountConfig my_acc_cfg = new MyAccountConfig();
            my_acc_cfg.accCfg = acc.cfg;

            my_acc_cfg.buddyCfgs.clear();
            for (int j = 0; j < acc.buddyList.size(); j++) {
                MyBuddy bud = acc.buddyList.get(j);
                my_acc_cfg.buddyCfgs.add(bud.cfg);
            }

            accCfgs.add(my_acc_cfg);
        }
    }

    private void saveConfig(String filename) {
        JsonDocument json = new JsonDocument();

        try {
            /* Write endpoint config */
            json.writeObject(epConfig);

			/* Write transport config */
            ContainerNode tp_node = json.writeNewContainer("SipTransport");
            sipTpConfig.writeObject(tp_node);

			/* Write account configs */
            buildAccConfigs();
            ContainerNode accs_node = json.writeNewArray("accounts");
            for (int i = 0; i < accCfgs.size(); i++) {
                accCfgs.get(i).writeObject(accs_node);
            }

			/* Save file */
            json.saveFile(filename);
        } catch (Exception e) {
        }

		/*
         * Force delete json now, as I found that Java somehow destroys it after
		 * lib has been destroyed and from non-registered thread.
		 */
        json.delete();
    }

    public void deinit() {
        String configPath = appDir + "/" + configName;
        saveConfig(configPath);

		/*
         * Try force GC to avoid late destroy of PJ objects as they should be
		 * deleted before lib is destroyed.
		 */
        Runtime.getRuntime().gc();

		/*
         * Shutdown pjsua. Note that Endpoint destructor will also invoke
		 * libDestroy(), so this will be a test of double libDestroy().
		 */
        try {
            mEndpoint.libDestroy();
        } catch (Exception e) {
        }

		/*
         * Force delete Endpoint here, to avoid deletion from a non- registered
		 * thread (by GC?).
		 */
        mEndpoint.delete();
        mEndpoint = null;
    }

}

package com.yo.android.voip;

import android.net.sip.SipManager;

public class Sip {

    public interface SipInterface {
        SipManager getSipManager();
    }

    public static void setSipManager(SipManager mSipManager) {
        Sip.mSipManager = mSipManager;
    }

    private static SipManager mSipManager = null;

    public static final SipManager getSipManager() {
        return mSipManager;
    }

}

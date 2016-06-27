package com.yo.android.voip;

import android.net.sip.SipManager;
import android.net.sip.SipProfile;

public class Sip {

    public interface SipInterface {
        public SipManager getSipManager();
    }

    public static SipManager mSipManager = null;
    public SipProfile mSipProfile = null;

    public static final SipManager getSipManager() {
        return mSipManager;
    }

}

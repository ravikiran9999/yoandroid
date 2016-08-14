package com.yo.android.pjsip;

import android.os.Binder;

public class SipBinder extends Binder {
    private SipServiceHandler handler;

    public SipBinder(SipServiceHandler handler) {
        this.handler = handler;
    }

    public SipServiceHandler getHandler() {
        return handler;
    }

}

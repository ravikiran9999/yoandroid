package com.yo.android.pjsip;

import android.os.Binder;

import com.yo.dialer.yopj.YoSipServiceHandler;

public class SipBinder extends Binder {
    private SipServiceHandler handler;
    private YoSipServiceHandler yohandler;

    public SipBinder(SipServiceHandler handler) {
        this.handler = handler;
    }

    public SipBinder(YoSipServiceHandler handler) {
        this.yohandler = handler;
    }

    public SipServiceHandler getHandler() {
        return handler;
    }

    public YoSipServiceHandler getYOHandler() {
        return yohandler;
    }

}

package com.yo.dialer.ui;

/**
 * Created by root on 8/8/17.
 */

public interface CallStatusListener {
    void callDisconnected();

    void callStatus(String status);


}

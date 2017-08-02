package com.yo.dialer.yopj;

import android.content.Context;

/**
 * Created by root on 17/7/17.
 */

public interface SipServicesListener {
    YoAccount addAccount(Context context);

    void acceptCall();

    void rejectCall();

    int getCallDurationInSec();

    String getCallState();

    void setMic(boolean flag);

    void setHold(boolean flag);


}

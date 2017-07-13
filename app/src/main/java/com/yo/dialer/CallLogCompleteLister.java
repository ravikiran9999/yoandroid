package com.yo.dialer;

import com.yo.dialer.model.CallLog;

/**
 * Created by root on 11/7/17.
 */

public interface CallLogCompleteLister {
    void callLogsCompleted(CallLog callLog);
}

package com.yo.dialer.yopj;

import org.pjsip.pjsua2.LogEntry;
import org.pjsip.pjsua2.LogWriter;

/**
 * Created by Rajesh Babu on 17/7/17.
 */

public class YoLogWriter extends LogWriter {
    @Override
    public void write(LogEntry entry) {
        System.out.println(entry.getMsg());

    }
}

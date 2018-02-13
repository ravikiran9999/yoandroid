package com.yo.android.model.dialer;

import java.util.List;

public class CallLogsData {
    private List<CallLogsResult> RESULT;

    public List<CallLogsResult> getRESULT() {
        return RESULT;
    }

    public void setRESULT(List<CallLogsResult> RESULT) {
        this.RESULT = RESULT;
    }

    @Override
    public String toString() {
        return "ClassPojo [RESULT = " + RESULT + "]";
    }
}

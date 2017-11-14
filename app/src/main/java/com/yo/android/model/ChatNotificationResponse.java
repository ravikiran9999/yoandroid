package com.yo.android.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by rdoddapaneni on 11/14/2017.
 */

public class ChatNotificationResponse implements Serializable {

    @SerializedName("code")
    @Expose
    private Integer code;
    @SerializedName("response")
    @Expose
    private String response;
    @SerializedName("data")
    @Expose
    private String data;
    private final static long serialVersionUID = -135761664401171714L;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getData() {
        return data;
    }
}

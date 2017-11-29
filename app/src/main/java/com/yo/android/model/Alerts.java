
package com.yo.android.model;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Alerts implements Serializable
{

    @SerializedName("code")
    @Expose
    private Integer code;
    @SerializedName("response")
    @Expose
    private String response;
    @SerializedName("data")
    @Expose
    private String data;
    private final static long serialVersionUID = 5967304711963009262L;

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

    public void setData(String data) {
        this.data = data;
    }

}

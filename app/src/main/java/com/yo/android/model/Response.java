package com.yo.android.model;

/**
 * Created by creatives on 7/5/2016.
 */
public class Response {
    private String response;

    private String data;

    private String code;

    public String getResponse ()
    {
        return response;
    }

    public void setResponse (String response)
    {
        this.response = response;
    }

    public String getData ()
    {
        return data;
    }

    public void setData (String data)
    {
        this.data = data;
    }

    public String getCode ()
    {
        return code;
    }

    public void setCode (String code)
    {
        this.code = code;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [response = "+response+", data = "+data+", code = "+code+"]";
    }
}
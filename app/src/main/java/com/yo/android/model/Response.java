package com.yo.android.model;

/**
 * Created by creatives on 7/5/2016.
 */
public class Response {
    private String response;

    private Object data;

    private String code;

    private String balance;

    public String getResponse ()
    {
        return response;
    }

    public void setResponse (String response)
    {
        this.response = response;
    }

    public Object getData ()
    {
        return data;
    }

    public void setData (Object data)
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

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [response = "+response+", data = "+data+", code = "+code+", balance = "+balance+"]";
    }
}

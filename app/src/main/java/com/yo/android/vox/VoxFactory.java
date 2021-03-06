package com.yo.android.vox;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Ramesh on 2/7/16.
 */
@Singleton
public class VoxFactory {
    private String LOGINUSER = "";
    private String LOGINSECRET = "";

    @Inject
    public VoxFactory() {

    }

    public GetBalance newGetBalance(String mobile) {
        GetBalance getBalance = new GetBalance(LOGINUSER, LOGINSECRET, "SUBSCRIBER", "GETBALANCE");
        getBalance.addPhoneNumber(mobile);
        return getBalance;
    }

    public GetSubscriberId newGetRates(String mobile) {
        GetSubscriberId getSubscriberId = new GetSubscriberId(LOGINUSER, LOGINSECRET, "BALANCE", "RATES");
        getSubscriberId.addPhoneNumber(mobile);
        return getSubscriberId;
    }

    public GetSubscriberId newGetSubscriberId(String mobile) {
        GetSubscriberId getSubscriberId = new GetSubscriberId(LOGINUSER, LOGINSECRET, "SUBSCRIBER", "GETSUBSCRIBERID");
        getSubscriberId.addPhoneNumber(mobile);
        return getSubscriberId;
    }

    //{"STATUS":"SUCCESS","DATA":{"SUBSCRIBERID":"77","USERNAME":"9573535345","PASSWORD":"123456","CREDIT":"2","CALLINGCARDNUMBER":"4949349845","MESSAGE":"Subscriber data inserted successfully"}}
    public AddSubscriberBody newAddSubscriber(String username, String mobile) {
        AddSubscriberBody addSubscriberBody = new AddSubscriberBody(LOGINUSER, LOGINSECRET, "SUBSCRIBER", "ADD");
        addSubscriberBody.addSubscriberBody(username, mobile);
        return addSubscriberBody;
    }

    public String addSubscriber(String username, String mobile, String countryCode) {
        Map<String, Object> data = new HashMap<>();

        return prepareRequest("SUBSCRIBER", "ADD", data);
    }


    public String getBalanceBody(String subscriberId) {
        Map<String, Object> data = new HashMap<>();
        data.put("SUBSCRIBERID", subscriberId);
        return prepareRequest("SUBSCRIBER", "GETBALANCE", data);
    }

    public String getPaymentHistory(String subscriberId) {
        Map<String, Object> data = new HashMap<>();
        data.put("SUBSCRIBERID", subscriberId);
        return prepareRequest("SUBSCRIBER", "PAYMENTHISTORY", data);
    }

    public String addBalanceBody(String subscriberId, String credit) {
        Map<String, Object> data = new HashMap<>();
        data.put("SUBSCRIBERID", subscriberId);
        data.put("CREDIT", credit);
        return prepareRequest("SUBSCRIBER", "ADDBALANCE", data);
    }


    public String getCallLogsBody(String mobile) {
        Map<String, Object> data = new HashMap<>();
        data.put("USERNAME", mobile);
        return prepareRequest("BALANCE", "CDR", data);
    }

    public String verifyOTP(String pin) {
        Map<String, Object> data = new HashMap<>();
        data.put("PIN", "+919573535345");
        data.put("TYPE", "1");
        data.put("PACKAGEID", "1");
        return prepareRequest("OTP", "OTPREQUEST", data);
    }

    public String getCallRatesBody(String packageId) {
        Map<String, Object> data = new HashMap<>();
        data.put("PACKAGEID", packageId);
        data.put("LIMIT", "50");
        data.put("COUNT", "0");
        return prepareRequest("PACKAGE", "RATES", data);
    }

    public String getSubscriberIdBody(String mobile) {
        Map<String, Object> data = new HashMap<>();
        data.put("USERNAME", mobile);
        return prepareRequest("SUBSCRIBER", "GETSUBSCRIBERID", data);
    }

    //2014­10­01
    public String getSpentDetailsHistoryBody(String subscriberId, String fromDate, String toDate, String limit) {
        Map<String, Object> data = new HashMap<>();
        data.put("SUBSCRIBERID", subscriberId);
        data.put("FROMDATE", fromDate);
        data.put("TODATE", toDate);
        data.put("LIMIT", limit);
        data.put("COUNT", "0");
        return prepareRequest("SUBSCRIBER", "CDRDATERANGE", data);
    }

    private String prepareRequest(String section, String action, Map<String, Object> data) {
        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("LOGINUSER", LOGINUSER);
        jsonData.put("LOGINSECRET", LOGINSECRET);
        jsonData.put("SECTION", section);
        jsonData.put("ACTION", action);
        jsonData.put("DATA", data);
        return new Gson().toJson(jsonData);

    }


}

package com.yo.android.vox;

import android.text.TextUtils;

import com.orion.android.common.logger.Log;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.api.YoApi;
import com.yo.android.model.PaymentHistoryItem;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ramesh on 9/7/16.
 */
@Singleton
public class BalanceHelper {
    private static final String TAG = "BalanceHelper";
    private Set<String> sCountryCodes;
    VoxFactory voxFactory;
    YoApi.YoService yoService;
    PreferenceEndPoint prefs;
    Log mLog;

    @Inject
    public BalanceHelper(Log log, VoxFactory voxFactory, YoApi.YoService yoService, @Named("login") PreferenceEndPoint preferenceEndPoint) {
        this.mLog = log;
        this.voxFactory = voxFactory;
        this.yoService = yoService;
        this.prefs = preferenceEndPoint;
    }


    public void checkBalance() {
        loadBalance(null);
    }


    private void loadBalance(final Callback<ResponseBody> callback) {
        final String accessToken = prefs.getStringPreference("access_token");
        yoService.executeBalanceAction(accessToken).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String str = Util.toString(response.body().byteStream());
                        JSONObject jsonObject = new JSONObject(str);
                        String balance = jsonObject.getString("CREDIT");
                        prefs.saveStringPreference(Constants.CURRENT_BALANCE, balance);
                        String subscriberId = jsonObject.getString("SUBSCRIBERID");
                        prefs.saveStringPreference(Constants.SUBSCRIBER_ID, subscriberId);
                        mLog.i(TAG, "loadBalance: balance -  %s", balance);
                        if (callback != null) {
                            callback.onResponse(call, response);
                        }
                    } catch (IOException | JSONException e) {
                        mLog.w(TAG, "loadBalance", e);

                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                mLog.i(TAG, "loadBalance: onFailure");
                if (callback != null) {
                    callback.onFailure(call, t);
                }
            }
        });
    }

    public void addBalance(final String credit, final Callback<ResponseBody> callback) {
        final String accessToken = prefs.getStringPreference("access_token");
        if (TextUtils.isEmpty(prefs.getStringPreference(Constants.SUBSCRIBER_ID))) {
            loadBalance(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    reAddBalance(accessToken, credit, callback);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
        } else {
            reAddBalance(accessToken, credit, callback);
        }

    }

    private void reAddBalance(String accessToken, String credit, final Callback<ResponseBody> callback) {
        yoService.addBalance(accessToken, prefs.getStringPreference(Constants.SUBSCRIBER_ID), credit).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String str = Util.toString(response.body().byteStream());
                        JSONObject jsonObject = new JSONObject(str);
                        String balance = jsonObject.getJSONObject("DATA").getString("CURRENTCREDIT");
                        prefs.saveStringPreference(Constants.CURRENT_BALANCE, balance);
                        mLog.i(TAG, "loadBalance: balance -  %s", balance);
                    } catch (IOException e) {
                        mLog.w(TAG, "loadBalance", e);
                    } catch (JSONException e) {
                        mLog.w(TAG, "loadBalance", e);
                    }
                }
                if (callback != null) {
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (callback != null) {
                    callback.onFailure(call, t);
                }

            }
        });
    }


    /**
     * Checkes whether a country code is valid.
     */
    public boolean isValidCountryCode(String countryCode) {
        if (sCountryCodes == null) {
            sCountryCodes = initCountryCodes();
        }
        return sCountryCodes.contains(countryCode);
    }

    private static Set<String> initCountryCodes() {
        final HashSet<String> result = new HashSet<String>();
        result.add("1");
        result.add("7");
        result.add("20");
        result.add("27");
        result.add("30");
        result.add("31");
        result.add("32");
        result.add("33");
        result.add("34");
        result.add("36");
        result.add("39");
        result.add("40");
        result.add("41");
        result.add("43");
        result.add("44");
        result.add("45");
        result.add("46");
        result.add("47");
        result.add("48");
        result.add("49");
        result.add("51");
        result.add("52");
        result.add("53");
        result.add("54");
        result.add("55");
        result.add("56");
        result.add("57");
        result.add("58");
        result.add("60");
        result.add("61");
        result.add("62");
        result.add("63");
        result.add("64");
        result.add("65");
        result.add("66");
        result.add("81");
        result.add("82");
        result.add("84");
        result.add("86");
        result.add("90");
        result.add("91");
        result.add("92");
        result.add("93");
        result.add("94");
        result.add("95");
        result.add("98");
        result.add("211");
        result.add("212");
        result.add("213");
        result.add("216");
        result.add("218");
        result.add("220");
        result.add("221");
        result.add("222");
        result.add("223");
        result.add("224");
        result.add("225");
        result.add("226");
        result.add("227");
        result.add("228");
        result.add("229");
        result.add("230");
        result.add("231");
        result.add("232");
        result.add("233");
        result.add("234");
        result.add("235");
        result.add("236");
        result.add("237");
        result.add("238");
        result.add("239");
        result.add("240");
        result.add("241");
        result.add("242");
        result.add("243");
        result.add("244");
        result.add("245");
        result.add("246");
        result.add("247");
        result.add("248");
        result.add("249");
        result.add("250");
        result.add("251");
        result.add("252");
        result.add("253");
        result.add("254");
        result.add("255");
        result.add("256");
        result.add("257");
        result.add("258");
        result.add("260");
        result.add("261");
        result.add("262");
        result.add("263");
        result.add("264");
        result.add("265");
        result.add("266");
        result.add("267");
        result.add("268");
        result.add("269");
        result.add("290");
        result.add("291");
        result.add("297");
        result.add("298");
        result.add("299");
        result.add("350");
        result.add("351");
        result.add("352");
        result.add("353");
        result.add("354");
        result.add("355");
        result.add("356");
        result.add("357");
        result.add("358");
        result.add("359");
        result.add("370");
        result.add("371");
        result.add("372");
        result.add("373");
        result.add("374");
        result.add("375");
        result.add("376");
        result.add("377");
        result.add("378");
        result.add("379");
        result.add("380");
        result.add("381");
        result.add("382");
        result.add("385");
        result.add("386");
        result.add("387");
        result.add("389");
        result.add("420");
        result.add("421");
        result.add("423");
        result.add("500");
        result.add("501");
        result.add("502");
        result.add("503");
        result.add("504");
        result.add("505");
        result.add("506");
        result.add("507");
        result.add("508");
        result.add("509");
        result.add("590");
        result.add("591");
        result.add("592");
        result.add("593");
        result.add("594");
        result.add("595");
        result.add("596");
        result.add("597");
        result.add("598");
        result.add("599");
        result.add("670");
        result.add("672");
        result.add("673");
        result.add("674");
        result.add("675");
        result.add("676");
        result.add("677");
        result.add("678");
        result.add("679");
        result.add("680");
        result.add("681");
        result.add("682");
        result.add("683");
        result.add("685");
        result.add("686");
        result.add("687");
        result.add("688");
        result.add("689");
        result.add("690");
        result.add("691");
        result.add("692");
        result.add("800");
        result.add("808");
        result.add("850");
        result.add("852");
        result.add("853");
        result.add("855");
        result.add("856");
        result.add("870");
        result.add("878");
        result.add("880");
        result.add("881");
        result.add("882");
        result.add("883");
        result.add("886");
        result.add("888");
        result.add("960");
        result.add("961");
        result.add("962");
        result.add("963");
        result.add("964");
        result.add("965");
        result.add("966");
        result.add("967");
        result.add("968");
        result.add("970");
        result.add("971");
        result.add("972");
        result.add("973");
        result.add("974");
        result.add("975");
        result.add("976");
        result.add("977");
        result.add("979");
        result.add("992");
        result.add("993");
        result.add("994");
        result.add("995");
        result.add("996");
        result.add("998");
        return result;
    }

    public void loadPaymentHistory(final Callback<List<PaymentHistoryItem>> callback) {
        mLog.w(TAG, "loadPaymentHistory:called");
        yoService.getPaymentHistory(prefs.getStringPreference(Constants.SUBSCRIBER_ID)).enqueue(new Callback<List<PaymentHistoryItem>>() {
            @Override
            public void onResponse(Call<List<PaymentHistoryItem>> call, Response<List<PaymentHistoryItem>> response) {
                mLog.i(TAG, "loadPaymentHistory: onResponse - %b", response.isSuccessful());
                if (callback != null) {
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(Call<List<PaymentHistoryItem>> call, Throwable t) {
                mLog.i(TAG, "loadPaymentHistory: onFailure");
                if (callback != null) {
                    callback.onFailure(call, t);
                }
            }
        });

    }

    public String getCurrentBalance() {
        String balance = prefs.getStringPreference(Constants.CURRENT_BALANCE, "0");
        try {
            DecimalFormat df = new DecimalFormat("0.000");
            String format = df.format(Double.valueOf(balance));
            return format;
        } catch (IllegalArgumentException e) {
            mLog.w(TAG, "getCurrentBalance", e);
        }

        return balance;
    }

    public String getCurrencySymbol() {
        String symbol = prefs.getStringPreference(Constants.CURRENCY_SYMBOL, "$");
        return symbol;
    }
}

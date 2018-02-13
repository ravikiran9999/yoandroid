package com.yo.android.util;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.orion.android.common.logger.Log;
import com.yo.android.R;
import com.yo.android.model.CountryCode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Ramesh on 18/7/16.
 */
@Singleton
public class CountryCodeHelper {

    final Context mContext;
    final Log mLog;
    private List<CountryCode> countryCodes;
    private final static String TAG = "CountryCodeHelper";

    @Inject
    public CountryCodeHelper(Context context, Log log) {
        this.mContext = context;
        this.mLog = log;
    }

    public List<CountryCode> readCodesFromAssets() {
        if (countryCodes != null && !countryCodes.isEmpty()) {
            return countryCodes;
        }
        BufferedReader in = null;
        try {
            in = new BufferedReader(
                    new InputStreamReader(mContext.getAssets().open("country_code.txt"), "UTF-8"));
            countryCodes = new ArrayList<>();
            for (; ; ) {
                String line = in.readLine();
                if (line == null) {
                    break;
                } else {
                    String[] codesArray = line.split("\\s*,\\s*");
                    CountryCode countryCode = new CountryCode();
                    countryCode.setCountryName(codesArray[0]);
                    countryCode.setCountryID(codesArray[1]);
                    countryCode.setCountryCode(codesArray[2]);
                    countryCodes.add(countryCode);
                }
            }
        } catch (IOException e) {
            mLog.e(TAG, "Exception", e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                mLog.e(TAG, "Exception", e);
            }
        }
        return countryCodes;
    }

    public String getSimCountryCode(Context context) {
        try {
            TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String mPhoneNumber = tMgr.getLine1Number();
            String zipCode = getCountryZipCode(context);
            mLog.e("Splash", "Phone number %s zipcode: %s ", mPhoneNumber, zipCode);
            return zipCode;
        } catch (Exception e) {
            mLog.w("Splash", e);
        }
        return null;
    }

    public String getCountryZipCode(Context context) {

        String CountryID = "";
        String CountryZipCode = "";

        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        //getNetworkCountryIso
        CountryID = manager.getSimCountryIso().toUpperCase();
        String[] rl = context.getResources().getStringArray(R.array.CountryCodes);
        for (int i = 0; i < rl.length; i++) {
            String[] g = rl[i].split(",");
            if (g[1].trim().equals(CountryID.trim())) {
                CountryZipCode = g[0];
                break;
            }
        }
        return CountryID;
    }
}

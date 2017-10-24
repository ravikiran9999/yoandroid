package com.yo.services;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.api.YoApi;
import com.yo.android.di.InjectedService;
import com.yo.android.util.Constants;
import com.yo.dialer.DialerLogs;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by root on 11/7/17.
 */

public class BackgroundServices extends InjectedService {
    public final static String FETCH_CALL_RATES = "action_fetch_call_rates";
    public final static String SYNC_OFFLINE_CONTACTS = "action_sync_offline_contacts";

    public final static boolean ENABLE_LOGS = true;
    private static final String TAG = BackgroundServices.class.getSimpleName();

    @Inject
    protected YoApi.YoService yoService;


    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ENABLE_LOGS) {
            Log.w(TAG, "Background service  is called");
        }
        performAction(intent);
        return START_STICKY;
    }


    private void performAction(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equalsIgnoreCase(FETCH_CALL_RATES)) {
                    fetchCallRates();
                } else if (action.equalsIgnoreCase(SYNC_OFFLINE_CONTACTS)) {
                    uploadContacts();
                }
            }
        }
    }

    private void uploadContacts() {
        String contactsJson = preferenceEndPoint.getStringPreference(Constants.OFFLINE_ADDED_CONTACTS);
        String accessToken = preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
        if (!TextUtils.isEmpty(contactsJson)) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<JSONObject>>() {
            }.getType();
            List<JSONObject> nameAndNumber = gson.fromJson(contactsJson, type);
            DialerLogs.messageI(TAG, "Contacts ready to upload.");
            yoService.syncContactsWithNameAPI(accessToken, nameAndNumber).enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    SharedPreferences.Editor editor = preferenceEndPoint.getSharedPreferences().edit();
                    editor.putString(Constants.OFFLINE_ADDED_CONTACTS, null);
                    editor.commit();
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    //if its failed to add try later to add failed contacts.
                }
            });
        }
    }

    private void fetchCallRates() {
        CallRates.fetch(yoService, preferenceEndPoint);
    }
}

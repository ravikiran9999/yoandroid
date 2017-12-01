package com.yo.android.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.FirebaseException;
import com.firebase.client.ValueEventListener;
import com.flurry.android.FlurryAgent;
import com.flurry.android.FlurryAgentListener;
import com.google.gson.Gson;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.api.ApiCallback;
import com.yo.android.di.Injector;
import com.yo.android.di.RootModule;
import com.yo.android.util.Constants;
import com.yo.android.util.FireBaseHelper;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.ObjectGraph;

/**
 * Created by Ramesh on 17/06/16.
 */
public class BaseApp extends MultiDexApplication {

    private static final String TAG = BaseApp.class.getSimpleName();
    private ObjectGraph objectGraph;
    private static BaseApp baseAppInstance;
    protected String userAgent;

    // Production
    private static final String FLURRY_API_KEY = "GRYKGBSF2C3XWJRVCXGP";

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;
    @Inject
    FireBaseHelper fireBaseHelper;


    private FlurryAgentListener flurryAgentListener;

    @Override
    public void onCreate() {
        super.onCreate();
        baseAppInstance = this;

        /* Enable disk persistence  */
        // FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        Firebase.getDefaultConfig().setPersistenceEnabled(true);
        Firebase.setAndroidContext(getApplicationContext());

        injectDependencies();

        // ReCreateService.getInstance(this).start(this);

        flurryAgentListener = new FlurryAgentListener() {
            @Override
            public void onSessionStarted() {
                Log.d("BaseApp", "In onSessionStarted of Flurry");
            }
        };
        initFlurry();


        // Fix for camera in nougat
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        firebaseUserStatus();
    }

    public static BaseApp get() {
        return baseAppInstance;
    }

    private void injectDependencies() {
        objectGraph = ObjectGraph.create(new RootModule(this));
        objectGraph.inject(this);
    }


    @Override
    public Object getSystemService(String name) {
        if (Injector.matchesService(name)) {
            return objectGraph;
        }
        return super.getSystemService(name);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        preferenceEndPoint.saveBooleanPreference(Constants.IS_IN_APP, false);
    }

    private void initFlurry() {
        new FlurryAgent.Builder()
                .withLogEnabled(true)
                .withCaptureUncaughtExceptions(true)
                //.withContinueSessionMillis(10)
                //.withLogEnabled(true)
                .withLogLevel(Log.VERBOSE)
                .withListener(flurryAgentListener)
                .build(this, FLURRY_API_KEY);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private void firebaseUserStatus() {
        fireBaseHelper.authWithCustomToken(getApplicationContext(), preferenceEndPoint.getStringPreference(Constants.FIREBASE_TOKEN), new ApiCallback<Firebase>() {
            @Override
            public void onResult(Firebase result) {
                String firebaseUserId = preferenceEndPoint.getStringPreference(Constants.FIREBASE_USER_ID);

                initialiseOnlinePresence(result, firebaseUserId);
            }

            @Override
            public void onFailure(String message) {
                android.util.Log.d(TAG, message);
            }
        });

    }

    private void initialiseOnlinePresence(Firebase databaseReference, String userId) {
        try {
            final Firebase onlineRef = databaseReference.child(".info/connected");
            final Firebase currentUserRef = databaseReference.child(Constants.USERS + "/" + userId + "/" + Constants.PROFILE).child("presence");
            onlineRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    android.util.Log.d(TAG, "DataSnapshot:" + dataSnapshot);
                    if (dataSnapshot.getValue(Boolean.class)) {
                        currentUserRef.onDisconnect().removeValue();
                        currentUserRef.setValue(dataSnapshot.getValue(), 1, new Firebase.CompletionListener() {
                            @Override
                            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                if(firebaseError != null) {
                                    android.util.Log.d(TAG, firebaseError.getDetails());
                                }
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(final FirebaseError databaseError) {
                    android.util.Log.d(TAG, "DatabaseError:" + databaseError);
                }
            });
        } catch (FirebaseException e) {
            Log.e(TAG, "Firebase error :" + e.getMessage());
        }
    }
}

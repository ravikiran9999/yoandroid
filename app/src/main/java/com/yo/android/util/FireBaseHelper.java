package com.yo.android.util;


import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.BuildConfig;
import com.yo.android.api.ApiCallback;
import com.yo.android.chat.firebase.FireBaseAuthToken;
import com.yo.android.chat.firebase.MyServiceConnection;
import com.yo.android.model.ChatMessage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import butterknife.ButterKnife;

@Singleton
public class FireBaseHelper {

    private static final String TAG = FireBaseHelper.class.getSimpleName();
    private Map<String, ChatMessage> map = new HashMap<>();
    private Context mContext;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private int firebaseLoginAttempt;
    private HashMap token;
    private int authTime;
    private int expiry;
    AuthData authData;

    @Inject
    MyServiceConnection myServiceConnection;

    final Firebase ref = new Firebase(BuildConfig.FIREBASE_URL);

    @Inject
    @Named("login")
    PreferenceEndPoint loginPrefs;

    @Inject
    public FireBaseHelper() {
        firebaseAuth = FirebaseAuth.getInstance();
        //authStateListener();

    }

    public ChatMessage getLastMessage(String roomId) {
        if (!map.containsKey(roomId)) {
            return null;
            //startListeningRoom(roomId);
        }
        return map.get(roomId);
    }

    // unauth from firebase
    public void unauth() {
        if (ref != null) {
            ref.unauth();
            ref.removeValue();
        }
    }

    // Generate firebase custom token
    public Firebase authWithCustomToken(final Context context, final String authToken, ApiCallback<Firebase> firebaseApiCallback) {
        mContext = context;
        //Url from Firebase dashboard
        if (getExpiry()!= 0 && getExpiry() >= 3600) {
            return ref;
        } else {
            authData = ref.getAuth();

            if (firebaseApiCallback != null && authData != null) {
                token = (HashMap) authData.getAuth().get("token");
                getTokenExpiry();
                firebaseApiCallback.onResult(ref);
            } else if (authData == null && !TextUtils.isEmpty(authToken)) {
                ref.authWithCustomToken(authToken, new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData mAuthData) {
                        if (mAuthData != null) {
                            Log.i(TAG, "Login Succeeded!");
                            String newAuthToken = loginPrefs.getStringPreference(Constants.FIREBASE_TOKEN);
                            authWithCustomToken(context, newAuthToken, null);
                        } else {

                            FireBaseAuthToken.getInstance(context).getFirebaseAuth(new FireBaseAuthToken.FireBaseAuthListener() {
                                @Override
                                public void onSuccess() {
                                    String newAuthToken = loginPrefs.getStringPreference(Constants.FIREBASE_TOKEN);
                                    authWithCustomToken(context, newAuthToken, null);
                                }

                                @Override
                                public void onFailed() {
                                    Log.i(TAG, "Login Failed!");
                                }
                            });
                            Log.i(TAG, "Login un Succeeded!");
                        }
                    }

                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {
                        Log.e(TAG, "Login Failed! Auth token expired : " + firebaseError.getMessage());
                        unauth();
                        loginPrefs.removePreference(Constants.FIREBASE_TOKEN);

                        FireBaseAuthToken.getInstance(context).getFirebaseAuth(new FireBaseAuthToken.FireBaseAuthListener() {
                            @Override
                            public void onSuccess() {
                                Log.e(TAG, "Login Failed! Auth token expired  and generated new token");
                                Log.d(TAG, String.valueOf(++firebaseLoginAttempt));
                                String newAuthToken = loginPrefs.getStringPreference(Constants.FIREBASE_TOKEN);
                                Log.d(TAG, "newAuthToken :" + newAuthToken);
                                if (firebaseLoginAttempt <= 3) {
                                    authWithCustomToken(context, newAuthToken, null);
                                }

                            }

                            @Override
                            public void onFailed() {
                                Log.e(TAG, "Login Failed! Auth token expired  and generated new token also failed.");

                            }
                        });
                    }
                });
            } else {
                return ref;
            }
        }
        return ref;
    }

    public void unbind() {
        ButterKnife.bind((Activity) mContext);
    }

    // This logic need to be used on updating firebase

    /*public Firebase authWithNewCustomToken(final Context context, final String authToken) {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithCustomToken(authToken)
                .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }*/

    private void getTokenExpiry() {
        try {
            for (Object o : token.keySet()) {
                String key = (String) o;
                if(token.get(key) instanceof Integer) {
                    int value = (int) token.get(key);

                    if (key.equals("exp")) {
                        expiry = value;
                    }

                    if (key.equals("auth_time")) {
                        authTime = value;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long getExpiry() {
        try {
            if (authTime != 0 && expiry != 0) {

                int diffTime = expiry - authTime;
                long expiryTime = System.currentTimeMillis() - diffTime;
                return expiryTime;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}

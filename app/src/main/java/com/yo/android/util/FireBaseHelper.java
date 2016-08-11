package com.yo.android.util;


import android.util.Log;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yo.android.BuildConfig;
import com.yo.android.chat.firebase.FirebaseService;
import com.yo.android.chat.firebase.MyServiceConnection;
import com.yo.android.model.ChatMessage;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Ramesh on 13/7/16.
 */
@Singleton
public class FireBaseHelper {

    private static final String TAG = "FireBaseHelper";

    private DatabaseReference roomReference;
    private Map<String, ChatMessage> map = new HashMap<>();

    @Inject
    MyServiceConnection myServiceConnection;

    /*@Inject
    FirebaseService firebaseService;*/

    @Inject
    public FireBaseHelper() {

    }

    public ChatMessage getLastMessage(String roomId) {
        if (!map.containsKey(roomId)) {
            return null;
            //startListeningRoom(roomId);
        }
        return map.get(roomId);
    }

    public Firebase authWithCustomToken(final String authToken) {
        //Url from Firebase dashboard
        Firebase ref = new Firebase(BuildConfig.FIREBASE_URL);
        AuthData authData = ref.getAuth();
        if (authData == null) {

            ref.authWithCustomToken(authToken, new Firebase.AuthResultHandler() {
                @Override
                public void onAuthenticated(AuthData authData) {
                    if (authData != null) {
                        Log.i(TAG, "Login Succeeded!");
                    } else {
                        Log.i(TAG, "Login un Succeeded!");
                    }
                }

                @Override
                public void onAuthenticationError(FirebaseError firebaseError) {
                    Log.e(TAG, "Login Failed! Auth token expired" + firebaseError.getMessage());
                    /*if (myServiceConnection.isServiceConnection()) {
                        firebaseService.getFirebaseAuth();
                    }*/
                }
            });
        } else {
            return ref;
        }
        return ref;
    }
}

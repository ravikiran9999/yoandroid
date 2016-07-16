package com.yo.android.util;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
    private DatabaseReference roomReference;
    private Map<String, ChatMessage> map = new HashMap<>();
    //This token will be generated at ROR server
    private String AUTH_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ2IjowLCJpYXQiOjE0Njg1OTE1NzksImQiOnsicHJvdmlkZXIiOiJhbm9ueW1vdXMiLCJ1aWQiOiItS01fQWNuQUE1ZzBVSURndjFBRSJ9fQ.Kp9EYyfaaj-lnBy57pqBbqTfGjKgveLmOIH8_zFIMcU";

    @Inject
    public FireBaseHelper() {
        roomReference = FirebaseDatabase.getInstance().getReference(Constants.ROOM_ID);
    }

    public ChatMessage getLastMessage(String roomId) {
        if (!map.containsKey(roomId)) {
            return null;
            //startListeningRoom(roomId);
        }
        return map.get(roomId);
    }

    //
    public void authWithCustomToken() {
        //Url from Firebase dashboard
        final Firebase ref = new Firebase("https://yoandroid-a0b48.firebaseio.com/");
        ref.authWithCustomToken(AUTH_TOKEN, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticationError(FirebaseError error) {
                System.err.println("Login Failed! " + error.getMessage());
            }

            @Override
            public void onAuthenticated(AuthData authData) {
                System.out.println("Login Succeeded!");
                //Once user is authenticated!
                ref.child("Rooms").child("-KM_AcnAA5g0UIDgv1AE")
                        .addListenerForSingleValueEvent(new com.firebase.client.ValueEventListener() {
                            @Override
                            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {

                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });
            }
        });

    }
}

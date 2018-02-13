package com.yo.android.ui;

import android.app.Activity;
import android.os.AsyncTask;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.yo.dialer.DialerLogs;
import com.yo.dialer.googlesheet.UploadCallDetails;

import static com.yo.android.ui.BottomTabsActivity.mCredential;

/**
 * Created by root on 5/9/17.
 */

class RetrieveJwtAsyncTask extends AsyncTask<Void, Boolean, String> {
    private Activity activity;
    final private String CLIENT_ID = "618560320180-0jsmqgpqnh8mdpeqi15svur4bmn5djbt.apps.googleusercontent.com";
    private static final String TAG = RetrieveJwtAsyncTask.class.getSimpleName();
    public RetrieveJwtAsyncTask(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected String doInBackground(Void... params) {
        String scope = "audience:server:client_id:" + CLIENT_ID;
        try {
            DialerLogs.messageI(TAG,"Selected account name is "+mCredential.getSelectedAccountName());

            return GoogleAuthUtil.getToken(
                    activity, mCredential.getSelectedAccount(),mCredential.getScope());
        } catch (UserRecoverableAuthIOException e) {
            activity.startActivityForResult(e.getIntent(), UploadCallDetails.REQUEST_AUTHORIZATION);
        } catch (Exception e) {
            e.printStackTrace(); // TODO: handle the exception
        }
        return null;
    }

    @Override
    protected void onPostExecute(String idToken) {
        // exchange encrypted idToken with server-side to identify the user
        DialerLogs.messageI(TAG,idToken);
        //mIdTokenEditText.setText(idToken);
    }

}

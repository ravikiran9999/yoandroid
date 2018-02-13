package com.yo.android.ui;

import android.app.Activity;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.yo.dialer.DialerLogs;
import com.yo.dialer.googlesheet.UploadCallDetails;

import java.util.Arrays;
import java.util.List;

import static com.yo.android.ui.BottomTabsActivity.mCredential;

/**
 * Created by root on 5/9/17.
 */

class RetrieveExchangeCodeAsyncTask extends AsyncTask<Void, Boolean, String> {

    final private String CLIENT_ID = "618560320180-0jsmqgpqnh8mdpeqi15svur4bmn5djbt.apps.googleusercontent.com\t";
    final private List<String> SCOPES = Arrays.asList(new String[]{
            "https://www.googleapis.com/auth/spreadsheets"
    });
    private Activity activity;
    private static final String TAG = RetrieveExchangeCodeAsyncTask.class.getSimpleName();

    public RetrieveExchangeCodeAsyncTask(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected String doInBackground(Void... params) {
        String scope = String.format("oauth2:server:client_id:%s:api_scope:%s",
                CLIENT_ID, TextUtils.join(" ", SCOPES));
        try {
            DialerLogs.messageI(TAG,"Selected account name is "+mCredential.getSelectedAccountName());
            return GoogleAuthUtil.getToken(
                    activity, mCredential.getSelectedAccount(),mCredential.getScope());
        } catch (UserRecoverableAuthException e) {
            activity.startActivityForResult(e.getIntent(), UploadCallDetails.REQUEST_AUTHORIZATION);
        } catch (Exception e) {
            e.printStackTrace(); // TODO: handle the exception
        }
        return null;
    }

    @Override
    protected void onPostExecute(String code) {
        // exchange code with server-side to retrieve an additional
        // access token on the server-side.
        DialerLogs.messageI(TAG, code);
        // mExchangeCodeEditText.setText(code);
    }
}

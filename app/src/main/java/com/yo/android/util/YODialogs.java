package com.yo.android.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;

import com.yo.android.R;
import com.yo.android.calllogs.CallLog;
import com.yo.android.chat.ui.LoginActivity;
import com.yo.android.pjsip.YoSipService;
import com.yo.android.provider.YoAppContactContract;
import com.yo.android.ui.BaseActivity;
import com.yo.android.ui.FollowingsActivity;
import com.yo.android.ui.fragments.DialerFragment;
import com.yo.android.voip.VoipConstants;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by rajesh on 8/9/16.
 */
public class YODialogs {
    public static void clearHistory(final Activity activity, final DialerFragment.CallLogClearListener callLogClearListener) {


        if (activity != null) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

            LayoutInflater layoutInflater = LayoutInflater.from(activity);
            final View view = layoutInflater.inflate(R.layout.clear_call_history, null);
            builder.setView(view);

            Button yesBtn = (Button) view.findViewById(R.id.yes_btn);
            Button noBtn = (Button) view.findViewById(R.id.no_btn);


            final AlertDialog alertDialog = builder.create();
            alertDialog.setCancelable(false);
            alertDialog.show();

            yesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    CallLog.Calls.clearCallHistory(activity);
                    callLogClearListener.clear();
                }
            });

            noBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });
        }
    }
}

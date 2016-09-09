package com.yo.android.util;

import android.app.Activity;
import android.app.Dialog;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;

import com.yo.android.R;
import com.yo.android.calllogs.CallLog;
import com.yo.android.ui.BaseActivity;
import com.yo.android.ui.FollowingsActivity;
import com.yo.android.ui.fragments.DialerFragment;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by rajesh on 8/9/16.
 */
public class YODialogs {
    public static void clearHistory(final Activity activity, final DialerFragment.CallLogClearListener callLogClearListener) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.clear_call_history);

        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        Button btnCancel = (Button) dialog.findViewById(R.id.btn_cancel);

        Button btnOk = (Button) dialog.findViewById(R.id.btn_ok);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                CallLog.Calls.clearCallHistory(activity);
                callLogClearListener.clear();
            }
        });

        dialog.show();
    }
}

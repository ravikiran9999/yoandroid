package com.yo.dialer;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.orion.android.common.util.ConnectivityHelper;
import com.yo.android.R;
import com.yo.android.chat.DeleteConfirmationListener;
import com.yo.android.chat.ui.LoginActivity;
import com.yo.android.flip.MagazineFlipArticlesFragment;
import com.yo.android.provider.YoAppContactContract;
import com.yo.android.ui.BottomTabsActivity;
import com.yo.android.ui.TabsHeaderActivity;
import com.yo.android.util.Constants;
import com.yo.android.util.FetchNewArticlesService;
import com.yo.android.util.Util;
import com.yo.android.voip.VoipConstants;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Rajesh Babu on 11/7/17.
 */

public class Dialogs {
    public static void recharge(final Activity activity) {
        if (activity != null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            LayoutInflater layoutInflater = LayoutInflater.from(activity);
            final View view = layoutInflater.inflate(R.layout.unfollow_alert_dialog, null);
            builder.setView(view);

            Button yesBtn = (Button) view.findViewById(R.id.yes_btn);
            Button noBtn = (Button) view.findViewById(R.id.no_btn);
            TextView tvRechargeText = (TextView) view.findViewById(R.id.dialog_content);

            yesBtn.setText(activity.getString(R.string.recharge));
            tvRechargeText.setText(activity.getString(R.string.no_sufficient_bal_recharge));


            final AlertDialog alertDialog = builder.create();
            alertDialog.setCancelable(false);
            alertDialog.show();

            yesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    activity.startActivity(new Intent(activity, TabsHeaderActivity.class));
                    activity.finish();
                }
            });


            noBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    activity.finish();
                }
            });
        }
    }

    public static void chatDeleteConformation(Context context, final DeleteConfirmationListener deleteConfirmationListener, int count) {

        if (context != null) {

            final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);

            LayoutInflater layoutInflater = LayoutInflater.from(context);
            final View view = layoutInflater.inflate(R.layout.delete_chat_confirmation, null);
            builder.setView(view);

            Button yesBtn = (Button) view.findViewById(R.id.yes_btn);
            Button noBtn = (Button) view.findViewById(R.id.no_btn);
            TextView deleteMessage = (TextView) view.findViewById(R.id.dialog_content);

            Resources res = context.getResources();
            String text = res.getQuantityString(R.plurals.delete_messages, count,count);
            deleteMessage.setText(text);

            final android.app.AlertDialog alertDialog = builder.create();
            alertDialog.setCancelable(false);
            alertDialog.show();

            yesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    if (deleteConfirmationListener != null) {
                        deleteConfirmationListener.deleteProceed();
                    }
                }
            });

            noBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    if (deleteConfirmationListener != null) {
                        deleteConfirmationListener.deleteCancle();
                    }
                }
            });
        }
    }

}

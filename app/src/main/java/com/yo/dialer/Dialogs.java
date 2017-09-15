package com.yo.dialer;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.ui.TabsHeaderActivity;

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

}

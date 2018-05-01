package com.yo.dialer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.chat.DeleteConfirmationListener;
import com.yo.android.ui.TabsHeaderActivity;

import butterknife.ButterKnife;

public class Dialogs {
    public static void recharge(final Activity activity) {
        if (activity != null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            LayoutInflater layoutInflater = LayoutInflater.from(activity);
            final View view = layoutInflater.inflate(R.layout.zero_balance_warning, null);
            builder.setView(view);

            /*Button yesBtn = (Button) view.findViewById(R.id.yes_btn);
            Button noBtn = (Button) view.findViewById(R.id.no_btn);
            TextView tvRechargeText = (TextView) view.findViewById(R.id.dialog_content);*/

            Button yesBtn = ButterKnife.findById(view, R.id.yes_btn);
            Button noBtn = ButterKnife.findById(view, R.id.no_btn);
            TextView tvRechargeText = ButterKnife.findById(view, R.id.dialog_content);

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

            /*Button yesBtn = (Button) view.findViewById(R.id.yes_btn);
            Button noBtn = (Button) view.findViewById(R.id.no_btn);
            TextView deleteMessage = (TextView) view.findViewById(R.id.dialog_content);*/

            Button yesBtn = ButterKnife.findById(view, R.id.yes_btn);
            Button noBtn = ButterKnife.findById(view, R.id.no_btn);
            TextView deleteMessage = ButterKnife.findById(view, R.id.dialog_content);

            Resources res = context.getResources();
            String text = res.getQuantityString(R.plurals.delete_messages, count, count);
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

    public static void nexgeRegistrationIssue(final Activity activity) {
        if (activity != null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            LayoutInflater layoutInflater = LayoutInflater.from(activity);
            final View view = layoutInflater.inflate(R.layout.zero_balance_warning, null);
            builder.setView(view);

            /*Button yesButton = (Button) view.findViewById(R.id.yes_btn);
            Button noButton = (Button) view.findViewById(R.id.no_btn);
            TextView tvRechargeText = (TextView) view.findViewById(R.id.dialog_content);
            */
            Button yesButton = ButterKnife.findById(view, R.id.yes_btn);
            Button noButton = ButterKnife.findById(view, R.id.no_btn);
            TextView tvRechargeText = ButterKnife.findById(view, R.id.dialog_content);


            yesButton.setText(R.string.ok);
            noButton.setVisibility(View.GONE);
            tvRechargeText.setText(activity.getString(R.string.nexge_registration_issue));


            final AlertDialog alertDialog = builder.create();
            alertDialog.setCancelable(false);
            alertDialog.show();

            yesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    activity.finish();
                }
            });
        }
    }
}

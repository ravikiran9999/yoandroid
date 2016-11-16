package com.yo.android.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.calllogs.CallLog;
import com.yo.android.model.Popup;
import com.yo.android.model.dialer.OpponentDetails;
import com.yo.android.ui.TabsHeaderActivity;
import com.yo.android.ui.fragments.DialerFragment;
import com.yo.android.ui.fragments.InviteActivity;

import java.util.Date;

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

    public static void showPopup(final PreferenceEndPoint preferenceEndPoint, final Activity activity, Popup popup, final PopupDialogListener listener) {

        if (activity != null) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

            LayoutInflater layoutInflater = LayoutInflater.from(activity);
            final View view = layoutInflater.inflate(R.layout.popup_layout, null);
            builder.setView(view);

            Button yesBtn = (Button) view.findViewById(R.id.yes_btn);
            ImageView tvDialogImage = (ImageView) view.findViewById(R.id.imv_dialog_image);
            TextView tvDialogTitle = (TextView) view.findViewById(R.id.dialog_title);
            TextView tvDialogContent = (TextView) view.findViewById(R.id.dialog_content);
            TextView tvLiveFrom = (TextView) view.findViewById(R.id.tv_live_from);
            TextView tvLiveTo = (TextView) view.findViewById(R.id.tv_live_to);
            ImageView imvClose = (ImageView) view.findViewById(R.id.imv_popup_close);

            String imageUrl = popup.getData().getImage_url();
            String title = popup.getData().getTitle();
            String message = popup.getData().getMessage();
            final String redirectTo = popup.getData().getRedirect_to();

            if (!TextUtils.isEmpty(imageUrl)) {
                tvDialogImage.setVisibility(View.VISIBLE);
                Glide.with(activity)
                        //.load(data.getImage_filename())
                        .load(imageUrl)
                        .placeholder(R.drawable.img_placeholder)
                        .centerCrop()
                        //Image size will be reduced 50%
                        .thumbnail(0.5f)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate()
                        .into(tvDialogImage);
            } else {
                tvDialogImage.setVisibility(View.GONE);
            }

            tvDialogTitle.setText(title);
            tvDialogContent.setText(message);

            if (!TextUtils.isEmpty(popup.getData().getLive_from()) && !TextUtils.isEmpty(popup.getData().getLive_to())) {
                String liveFromTime = popup.getData().getLive_from().substring(0, popup.getData().getLive_from().lastIndexOf("."));
                Date liveFromDate = Util.convertUtcToGmt(liveFromTime);
                String liveToTime = popup.getData().getLive_to().substring(0, popup.getData().getLive_to().lastIndexOf("."));
                Date liveToDate = Util.convertUtcToGmt(liveToTime);
                tvLiveFrom.setVisibility(View.VISIBLE);
                tvLiveTo.setVisibility(View.VISIBLE);
                tvLiveFrom.setText("Live From: " + liveFromDate);
                tvLiveTo.setText("Live To: " + liveToDate);
            } else {
                tvLiveFrom.setVisibility(View.GONE);
                tvLiveTo.setVisibility(View.GONE);
            }

            final AlertDialog alertDialog = builder.create();
            alertDialog.setCancelable(false);
            alertDialog.show();

            if (redirectTo.equals("AddFriends")) {
                yesBtn.setVisibility(View.VISIBLE);
                yesBtn.setText("ADD FRIENDS");
            } else if (redirectTo.equals("AddBalance")) {
                yesBtn.setVisibility(View.VISIBLE);
                yesBtn.setText("ADD BALANCE");
            } else if (redirectTo.equals("None")) {
                yesBtn.setVisibility(View.GONE);
            }

            yesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    if (listener != null) {
                        listener.closePopup();
                    }
                    //preferenceEndPoint.removePreference(Constants.POPUP_NOTIFICATION);
                    if (redirectTo.equals("AddFriends")) {
                        activity.startActivity(new Intent(activity, InviteActivity.class));
                    } else if (redirectTo.equals("AddBalance")) {
                        activity.startActivity(new Intent(activity, TabsHeaderActivity.class));
                    }
                }
            });

            imvClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    if (listener != null) {
                        listener.closePopup();
                    }
                    //preferenceEndPoint.removePreference(Constants.POPUP_NOTIFICATION);
                }
            });
        }
    }

    public static void redirectToPSTN(final Activity activity, OpponentDetails details, final DialerFragment.CallLogClearListener callLogClearListener) {


        if (activity != null) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

            LayoutInflater layoutInflater = LayoutInflater.from(activity);
            final View view = layoutInflater.inflate(R.layout.navigate_to_pstn, null);
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

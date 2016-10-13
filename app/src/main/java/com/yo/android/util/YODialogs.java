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
import com.yo.android.ui.TabsHeaderActivity;
import com.yo.android.ui.fragments.DialerFragment;
import com.yo.android.ui.fragments.InviteActivity;

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

    public static void showPopup(final PreferenceEndPoint preferenceEndPoint, final Activity activity, Popup popup) {

        if (activity != null) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

            LayoutInflater layoutInflater = LayoutInflater.from(activity);
            final View view = layoutInflater.inflate(R.layout.popup_layout, null);
            builder.setView(view);

            Button yesBtn = (Button) view.findViewById(R.id.yes_btn);
            ImageView tvDialogImage = (ImageView) view.findViewById(R.id.imv_dialog_image);
            TextView tvDialogTitle = (TextView) view.findViewById(R.id.dialog_title);
            TextView tvDialogContent = (TextView) view.findViewById(R.id.dialog_content);
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

            final AlertDialog alertDialog = builder.create();
            alertDialog.setCancelable(false);
            alertDialog.show();

            if (redirectTo.equals("AddFriends")) {
                yesBtn.setVisibility(View.VISIBLE);
                yesBtn.setText("ADD FRIENDS");
            } else if (redirectTo.equals("AddBalance")) {
                yesBtn.setVisibility(View.VISIBLE);
                yesBtn.setText("ADD BALANCE");
            } else if(redirectTo.equals("None")) {
                yesBtn.setVisibility(View.GONE);
            }

            yesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    preferenceEndPoint.removePreference(Constants.POPUP_NOTIFICATION);
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
                    preferenceEndPoint.removePreference(Constants.POPUP_NOTIFICATION);
                }
            });
        }

    }
}

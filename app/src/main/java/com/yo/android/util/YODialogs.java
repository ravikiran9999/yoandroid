package com.yo.android.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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
import com.orion.android.common.util.ToastFactory;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.calllogs.CallLog;
import com.yo.android.model.Popup;
import com.yo.android.model.dialer.OpponentDetails;
import com.yo.android.pjsip.SipHelper;
import com.yo.android.ui.TabsHeaderActivity;
import com.yo.android.ui.fragments.DialerFragment;
import com.yo.android.ui.fragments.InviteActivity;
import com.yo.android.voip.OutGoingCallActivity;
import com.yo.android.vox.BalanceHelper;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;

import de.greenrobot.event.EventBus;

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
                Date liveFromDate = DateUtil.convertUtcToGmt(liveFromTime);
                String liveToTime = popup.getData().getLive_to().substring(0, popup.getData().getLive_to().lastIndexOf("."));
                Date liveToDate = DateUtil.convertUtcToGmt(liveToTime);
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
                }
            });
        }
    }

    public static void redirectToPSTN(final EventBus bus, final Activity activity, final OpponentDetails details, PreferenceEndPoint preferenceEndPoint, BalanceHelper mBalanceHelper, final ToastFactory mToastFactory) {


        if (activity != null) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

            LayoutInflater layoutInflater = LayoutInflater.from(activity);
            final View view = layoutInflater.inflate(R.layout.navigate_to_pstn, null);
            builder.setView(view);

            Button yesBtn = (Button) view.findViewById(R.id.yes_btn);
            Button noBtn = (Button) view.findViewById(R.id.no_btn);
            TextView txtCallRate = (TextView) view.findViewById(R.id.txt_call_rate);
            TextView txtBalance = (TextView) view.findViewById(R.id.txt_balance);
            String callRate = preferenceEndPoint.getStringPreference(Constants.CALL_RATE, null);

            txtCallRate.setText(callRate);
            loadCurrentBalance(preferenceEndPoint, mBalanceHelper, activity, txtBalance);
            Button addBalance = (Button) view.findViewById(R.id.add_balance);
            addBalance.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!BuildConfig.DISABLE_ADD_BALANCE) {
                        Intent intent = new Intent(activity, TabsHeaderActivity.class);
                        intent.putExtra(Constants.OPEN_ADD_BALANCE, true);
                        activity.startActivityForResult(intent, OutGoingCallActivity.OPEN_ADD_BALANCE_RESULT);
                    } else {
                        mToastFactory.showToast(R.string.disabled);
                    }
                }
            });

            final AlertDialog alertDialog = builder.create();
            alertDialog.setCancelable(false);
            alertDialog.show();

            yesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    String stringExtra = details.getVoxUserName();

                    if (stringExtra != null && stringExtra.contains(BuildConfig.RELEASE_USER_TYPE)) {
                        try {
                            stringExtra = stringExtra.substring(stringExtra.indexOf(BuildConfig.RELEASE_USER_TYPE) + 6, stringExtra.length() - 1);
                            SipHelper.makeCall(activity, stringExtra);
                        } catch (StringIndexOutOfBoundsException e) {
                        }
                    }
                    activity.finish();
                }
            });

            noBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    bus.post(DialerFragment.REFRESH_CALL_LOGS);
                    activity.finish();
                }
            });
        }
    }

    private static void loadCurrentBalance(PreferenceEndPoint preferenceEndPoint, BalanceHelper mBalanceHelper, Context context, TextView txtBalance) {
        String balance = preferenceEndPoint.getStringPreference(Constants.CURRENT_BALANCE, "2.0");
        double val = Double.parseDouble(balance.trim());
        if (val <= 2) {
            Util.setBigStyleNotificationForBalance(context, "Credit", context.getString(R.string.low_balance), "Credit", "");
            //Util.showLowBalanceNotification(context, preferenceEndPoint);
        }
        if (mBalanceHelper != null) {
            if (mBalanceHelper.getCurrentBalance() != null && mBalanceHelper.getCurrencySymbol() != null) {
                txtBalance.setText(String.format("%s%s", mBalanceHelper.getCurrencySymbol(), mBalanceHelper.getCurrentBalance()));
            } else {
                txtBalance.setVisibility(View.GONE);
            }
        } else if (balance != null) {
            txtBalance.setText(String.format("%s", balance));
        } else {
            txtBalance.setVisibility(View.GONE);
        }
    }
}

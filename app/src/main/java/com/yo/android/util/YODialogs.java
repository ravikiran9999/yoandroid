package com.yo.android.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.orion.android.common.util.ToastFactory;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.calllogs.CallLog;
import com.yo.android.flip.MagazineFlipArticlesFragment;
import com.yo.android.model.Popup;
import com.yo.android.model.dialer.CallRateDetail;
import com.yo.android.model.dialer.OpponentDetails;
import com.yo.android.pjsip.SipHelper;
import com.yo.android.ui.MyCollections;
import com.yo.android.ui.TabsHeaderActivity;
import com.yo.android.ui.fragments.DialerFragment;
import com.yo.android.ui.fragments.InviteActivity;
import com.yo.android.voip.OutGoingCallActivity;
import com.yo.android.vox.BalanceHelper;
import com.yo.dialer.DialerLogs;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

/**
 * Created by rajesh on 8/9/16.
 */
public class YODialogs {
    public static final String TAG = YODialogs.class.getSimpleName();


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
        DialerLogs.messageI(TAG, "redirectToPSTN Dailog");

        if (activity != null) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

            LayoutInflater layoutInflater = LayoutInflater.from(activity);
            final View view = layoutInflater.inflate(R.layout.navigate_to_pstn, null);
            builder.setView(view);

            Button yesBtn = (Button) view.findViewById(R.id.yes_btn);
            Button noBtn = (Button) view.findViewById(R.id.no_btn);
            TextView txtCallRate = (TextView) view.findViewById(R.id.txt_call_rate);
            TextView txtBalance = (TextView) view.findViewById(R.id.txt_balance);
            //String callRate = preferenceEndPoint.getStringPreference(Constants.CALL_RATE, null);

            String countryCode = details.getContact().getCountryCode();
            String json = preferenceEndPoint.getStringPreference(Constants.COUNTRY_LIST);
            List<CallRateDetail> callRateDetailList = new Gson().fromJson(json, new TypeToken<List<CallRateDetail>>() {
            }.getType());
            if (callRateDetailList != null) {
                for (CallRateDetail callRateDetail : callRateDetailList) {
                    String prefix = callRateDetail.getPrefix();
                    if (countryCode != null && countryCode.equals(prefix)) {
                        String cRate = callRateDetail.getRate();
                        String cPulse = callRateDetail.getPulse();
                        String pulse;
                        if (cPulse.equals("60")) {
                            pulse = "min";
                        } else {
                            pulse = "sec";
                        }

                        String callRate = " $" + cRate + "/" + pulse;
                        txtCallRate.setText(callRate);
                        break;
                    }
                }
            }

            //txtCallRate.setText(callRate);
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
                    String stringExtra = details.getContact().getNexgieUserName();

                    if (stringExtra != null && stringExtra.contains(BuildConfig.RELEASE_USER_TYPE)) {
                        try {
                            stringExtra = stringExtra.substring(stringExtra.indexOf(BuildConfig.RELEASE_USER_TYPE) + 6, stringExtra.length() - 1);
                            SipHelper.makeCall(activity, stringExtra, true);
                        } catch (StringIndexOutOfBoundsException e) {
                            e.printStackTrace();
                        }
                    }
                    alertDialog.dismiss();
                    //activity.finish();
                }
            });

            noBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    bus.post(DialerFragment.REFRESH_CALL_LOGS);
                    //activity.finish();
                }
            });
        }
    }

    private static void loadCurrentBalance(PreferenceEndPoint preferenceEndPoint, BalanceHelper mBalanceHelper, Context context, TextView txtBalance) {
        String balance = preferenceEndPoint.getStringPreference(Constants.CURRENT_BALANCE, "2.0");
        // notification should be triggered from server
        /*double val = Double.parseDouble(balance.trim());
        if (val <= 2) {
            Util.setBigStyleNotificationForBalance(context, "Credit", context.getString(R.string.low_balance), "Credit", "");
            //Util.showLowBalanceNotification(context, preferenceEndPoint);
        }*/
        if (mBalanceHelper != null) {
            if (mBalanceHelper.getCurrentBalance() != null && mBalanceHelper.getCurrencySymbol() != null) {
                txtBalance.setText(String.format("%s %s", mBalanceHelper.getCurrencySymbol(), mBalanceHelper.getCurrentBalance()));
            } else {
                txtBalance.setVisibility(View.GONE);
            }
        } else if (balance != null) {
            txtBalance.setText(String.format("%s", balance));
        } else {
            txtBalance.setVisibility(View.GONE);
        }
    }

    public static void renewMagazine(final Activity activity, final Fragment fragment, String description, final PreferenceEndPoint preferenceEndPoint) {
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        final View view = layoutInflater.inflate(R.layout.dialog_with_check_box, null);
        final CheckBox checkBox = (CheckBox) view.findViewById(R.id.auto_renew_checkbox);
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        builder.setMessage(description);
        checkBox.setChecked(true);
        builder.setPositiveButton(activity.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (checkBox.isChecked()) {
                    preferenceEndPoint.saveBooleanPreference(Constants.AUTO_RENEWAL_SUBSCRIPTION, checkBox.isChecked());
                }

                dialog.dismiss();
                if (fragment != null && fragment instanceof MagazineFlipArticlesFragment) {
                    ((MagazineFlipArticlesFragment) fragment).llNoArticles.setVisibility(View.GONE);
                    ((MagazineFlipArticlesFragment) fragment).loadArticles(null, true);

                }

            }
        });
        builder.setNegativeButton(activity.getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }


    public static void addBalance(final Context context, String description, final PreferenceEndPoint preferenceEndPoint) {
        boolean appLockStatus = preferenceEndPoint.getBooleanPreference(Constants.APP_LOCK, false);

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage(description);
        builder.setPositiveButton(context.getResources().getString(R.string.add), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent(context, TabsHeaderActivity.class);
                intent.putExtra(Constants.RENEWAL, true);
                ((Activity) context).startActivityForResult(intent, 1001);

            }
        });
        if (!appLockStatus) {
            builder.setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();
        if (!alertDialog.isShowing()) {
            alertDialog.show();
        } else {
            alertDialog.dismiss();
        }

    }
}

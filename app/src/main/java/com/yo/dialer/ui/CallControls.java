package com.yo.dialer.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.view.View;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.yo.android.R;
import com.yo.android.pjsip.SipBinder;
import com.yo.dialer.DialerLogs;

/**
 * Created by root on 17/8/17.
 */

class CallControls {
    private static final String TAG = CallControls.class.getSimpleName();

    public static CallControlsModel getCallControlsModel() {
        return callControlsModel;
    }

    private static CallControlsModel callControlsModel = new CallControlsModel();

    public static void toggleHold(SipBinder sipBinder, View v) {
        if (sipBinder != null && sipBinder.getYOHandler() != null) {
            if (v.getTag() != null) {
                Boolean flag = Boolean.valueOf(v.getTag().toString());
                callControlsModel.setHoldOn(flag);
                changeSelection(v, flag);
                DialerLogs.messageE(TAG, "toggleHold == v.getTag = " + flag);
                sipBinder.getYOHandler().setHold(flag);
                v.setTag(!flag);
                DialerLogs.messageE(TAG, "toggleHold Changing ==" + !flag);
            } else {
                DialerLogs.messageE(TAG, "YO====toggleHold == v.getTag null");
            }
        } else {
            DialerLogs.messageE(TAG, "YO====sipBinder == null && sipBinder.getYOHandler() ==NULL");
        }
    }

    private static void changeSelection(View v, Boolean flag) {
        if (flag) {
            v.setBackgroundResource(R.drawable.mute_selector);
        } else {
            v.setBackgroundResource(0);
        }
    }

    public static boolean toggleSpeaker(AudioManager am, View v) {
        if (v.getTag() != null) {
            Boolean flag = Boolean.valueOf(v.getTag().toString());
            changeSelection(v, flag);
            am.setSpeakerphoneOn(flag);
            v.setTag(!flag);
            return flag;
        } else {
            DialerLogs.messageE(TAG, "YO====toggleSpeaker == null && sipBinder.getYOHandler() ==NULL");
        }
        return false;
    }

    public static boolean toggleMic(SipBinder sipBinder, View v) {
        if (sipBinder != null && sipBinder.getYOHandler() != null) {
            if (v.getTag() != null) {
                Boolean flag = Boolean.valueOf(v.getTag().toString());
                changeSelection(v, flag);
                sipBinder.getYOHandler().setMic(flag);
                v.setTag(!flag);
                return flag;
            } else {
                DialerLogs.messageE(TAG, "YO====toggleMic == v.getTag null");
            }
        } else {
            DialerLogs.messageE(TAG, "YO====sipBinder == null && sipBinder.getYOHandler() ==NULL");
        }
        return false;
    }

    public static void loadFullImage(Context context, String imagePath, final RelativeLayout fullImageLayout) {
        int myWidth = 512;
        int myHeight = 384;
        Glide.with(context).load(imagePath).asBitmap().into(new SimpleTarget<Bitmap>(myWidth, myHeight) {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                Drawable drawable = new BitmapDrawable(resource);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    if (fullImageLayout != null) {
                        fullImageLayout.setBackground(drawable);
                    } else {
                        DialerLogs.messageE(TAG, "YO====fULL IMAGE LAYOUT NULL");
                    }
                }
            }
        });
    }
}

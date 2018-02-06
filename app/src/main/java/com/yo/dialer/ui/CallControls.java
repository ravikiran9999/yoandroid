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
import com.yo.dialer.YoSipService;

/**
 * Created by root on 17/8/17.
 */

public class CallControls {
    private static final String TAG = CallControls.class.getSimpleName();

    public static CallControlsModel getCallControlsModel() {
        return callControlsModel;
    }

    private static CallControlsModel callControlsModel = new CallControlsModel();

    /**
     * Toggles Hold
     * @param sipBinder The SipBinder instance
     * @param v The View instance
     * @return toggle hold on or off
     */
    public static boolean toggleHold(SipBinder sipBinder, View v) {
        if (sipBinder != null && sipBinder.getYOHandler() != null) {
            if (v.getTag() != null) {
                Boolean flag = Boolean.valueOf(v.getTag().toString());
                DialerLogs.messageE(TAG, "toggleHold == v.getTag = " + flag);
                sipBinder.getYOHandler().setHold(!flag);
                if(YoSipService.changeHoldUI) {
                    changeSelection(v, !flag);
                    if (!flag) {
                        callControlsModel.setHoldOn(true);
                        DialerLogs.messageE(TAG, "setHoldOn if ==" + !flag);
                    } else {
                        DialerLogs.messageE(TAG, "setHoldOn else ==");
                        callControlsModel.setHoldOn(false);
                        if (callControlsModel.isMicOn()) {
                            DialerLogs.messageE(TAG, "If mic is on keep it on");
                            callControlsModel.setMicOn(true);
                            //sipBinder.getYOHandler().setMic(true);
                        }
                    }
                    v.setTag(!flag);
                    DialerLogs.messageE(TAG, "toggleHold Changing ==" + !flag);
                    YoSipService.changeHoldUI = false;
                }
                return flag;
            } else {
                DialerLogs.messageE(TAG, "YO====toggleHold == v.getTag null");
                return false;
            }
        } else {
            DialerLogs.messageE(TAG, "YO====sipBinder == null && sipBinder.getYOHandler() ==NULL");
            return false;
        }
    }

    /**
     * Toggles the Speaker
     * @param am The AudioManager instance
     * @param v The View instance
     * @return toggles speaker on or off
     */
    public static boolean toggleSpeaker(AudioManager am, View v) {
        if (v.getTag() != null) {
            Boolean flag = Boolean.valueOf(v.getTag().toString());
            DialerLogs.messageE(TAG, "toggleSpeaker == v.getTag = " + flag);
            changeSelection(v, !flag);
            if (!flag) {
                callControlsModel.setSpeakerOn(true);
            } else {
                callControlsModel.setSpeakerOn(false);
            }
            am.setSpeakerphoneOn(!flag);
            v.setTag(!flag);
            return flag;
        } else {
            DialerLogs.messageE(TAG, "YO====toggleSpeaker == null && sipBinder.getYOHandler() ==NULL");
        }
        return false;
    }

    /**
     * Toggles the mic
     * @param sipBinder The SipBinder instance
     * @param v The view instance
     * @return toggles mic on or off
     */
    public static boolean toggleMic(SipBinder sipBinder, View v) {
        if (sipBinder != null && sipBinder.getYOHandler() != null) {
            if (v.getTag() != null) {
                Boolean flag = Boolean.valueOf(v.getTag().toString());
                changeSelection(v, !flag);
                if (!flag) {
                    callControlsModel.setMicOn(true);
                } else {
                    callControlsModel.setMicOn(false);
                }
                sipBinder.getYOHandler().setMic(!flag);
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

    /**
     * Change the selected image
     * @param v The View instance
     * @param flag The boolean
     */
    public static void changeSelection(View v, Boolean flag) {
        if (flag) {
            v.setBackgroundResource(R.drawable.mute_selector);
        } else {
            v.setBackgroundResource(0);
        }
    }

    /**
     * Loads full user profile pic
     * @param context The context
     * @param imagePath The path of the image
     * @param fullImageLayout The layout
     */
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

    /**
     * Loads the current call mic settings
     * @param sipBinder The SipBinder instance
     * @param v The View instance
     * @return The boolean
     */
    public static boolean loadPrevMicSettings(SipBinder sipBinder, View v) {
        if (sipBinder != null && sipBinder.getYOHandler() != null) {
            if (v.getTag() != null) {
                Boolean flag = Boolean.valueOf(v.getTag().toString());
                changeSelection(v, flag);
                if (flag) {
                    callControlsModel.setMicOn(true);
                } else {
                    callControlsModel.setMicOn(false);
                }
                sipBinder.getYOHandler().setMic(flag);
                v.setTag(flag);
                return flag;
            } else {
                DialerLogs.messageE(TAG, "YO====toggleMic == v.getTag null");
            }
        } else {
            DialerLogs.messageE(TAG, "YO====sipBinder == null && sipBinder.getYOHandler() ==NULL");
        }
        return false;
    }

    /**
     * Loads the current call speaker settings
     * @param am The AudioManager instance
     * @param v The View instance
     * @return The boolean
     */
    public static boolean loadPrevSpeakerSettings(AudioManager am, View v) {
        if (v.getTag() != null) {
            Boolean flag = Boolean.valueOf(v.getTag().toString());
            DialerLogs.messageE(TAG, "toggleSpeaker == v.getTag = " + flag);
            changeSelection(v, flag);
            if (flag) {
                callControlsModel.setSpeakerOn(true);
            } else {
                callControlsModel.setSpeakerOn(false);
            }
            am.setSpeakerphoneOn(flag);
            v.setTag(flag);
            return flag;
        } else {
            DialerLogs.messageE(TAG, "YO====toggleSpeaker == null && sipBinder.getYOHandler() ==NULL");
        }
        return false;
    }

    /**
     * Toggles the speaker
     * @param am The AudioManager instance
     * @param v The View instance
     */
    public static void toggleRecSpeaker(AudioManager am, View v) {
        changeSelection(v, true);
        v.setTag(true);

    }
}

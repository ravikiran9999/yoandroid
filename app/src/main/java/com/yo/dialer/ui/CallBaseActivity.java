package com.yo.dialer.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.yo.android.R;
import com.yo.android.pjsip.SipBinder;
import com.yo.android.ui.BaseActivity;
import com.yo.dialer.CallExtras;
import com.yo.dialer.DialerLogs;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by root on 31/7/17.
 */

class CallBaseActivity extends BaseActivity {

    private static final String TAG = CallBaseActivity.class.getSimpleName();
    //User details;
    protected String calleName;
    protected String callePhoneNumber;
    protected String calleImageUrl;

    //Handler for call duration
    protected Handler mHandler = new Handler();

    //to stop timer
    protected boolean isCallStopped = true;

    protected AudioManager am;

    protected SipBinder sipBinder;

    protected TextView durationTxtview;
    protected TextView connectionStatusTxtView;
    protected RelativeLayout fullImageLayout;
    protected CircleImageView calleImageView;
    protected TextView calleNameTxt;
    protected TextView callePhoneNumberTxt;

    protected ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            sipBinder = (SipBinder) service;
            DialerLogs.messageW(TAG, "YO====Service connected to incoming call activity====" + sipBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            DialerLogs.messageW(TAG, "YO====Service disconnected to incoming call activity====");
            sipBinder = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService(new Intent(this, com.yo.dialer.YoSipService.class), connection, BIND_AUTO_CREATE);
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        callePhoneNumber = getIntent().getStringExtra(CallExtras.CALLER_NO);
        calleImageUrl = getIntent().getStringExtra(CallExtras.IMAGE);
        calleName = getIntent().getStringExtra(CallExtras.NAME);
    }

    protected void toggleHold(View v) {
        if (sipBinder != null && sipBinder.getYOHandler() != null) {
            if (v.getTag() != null) {
                Boolean flag = Boolean.valueOf(v.getTag().toString());
                changeSelection(v, flag);
                sipBinder.getYOHandler().setHold(flag);
                v.setTag(!flag);
            } else {
                DialerLogs.messageE(TAG, "YO====toggleHold == v.getTag null");
            }
        } else {
            DialerLogs.messageE(TAG, "YO====sipBinder == null && sipBinder.getYOHandler() ==NULL");
        }
    }

    protected void toggleMic(View v) {
        if (sipBinder != null && sipBinder.getYOHandler() != null) {
            if (v.getTag() != null) {
                Boolean flag = Boolean.valueOf(v.getTag().toString());
                changeSelection(v, flag);
                sipBinder.getYOHandler().setMic(flag);
                v.setTag(!flag);
            } else {
                DialerLogs.messageE(TAG, "YO====toggleMic == v.getTag null");
            }
        } else {
            DialerLogs.messageE(TAG, "YO====sipBinder == null && sipBinder.getYOHandler() ==NULL");
        }
    }

    protected void toggerSpeaker(View v) {
        if (v.getTag() != null) {
            Boolean flag = Boolean.valueOf(v.getTag().toString());
            changeSelection(v, flag);
            am.setSpeakerphoneOn(flag);
            v.setTag(!flag);
        } else {
            DialerLogs.messageE(TAG, "YO====toggerSpeaker == null && sipBinder.getYOHandler() ==NULL");
        }
    }

    private void changeSelection(View v, Boolean flag) {
        if (!flag) {
            v.setBackgroundResource(R.drawable.mute_selector);
        } else {
            v.setBackgroundResource(0);
        }
    }

    protected void rejectCall() {
        if (sipBinder != null && sipBinder.getYOHandler() != null) {
            isCallStopped = true;
            sipBinder.getYOHandler().rejectCall();
            finish();
        } else {
            DialerLogs.messageE(TAG, "YO====sipBinder == null && sipBinder.getYOHandler() ==NULL");
        }
    }

    protected void acceptCall() {
        if (sipBinder != null && sipBinder.getYOHandler() != null) {
            sipBinder.getYOHandler().acceptCall();
        } else {
            DialerLogs.messageE(TAG, "YO====sipBinder == null && sipBinder.getYOHandler() ==NULL");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sipBinder != null) {
            unbindService(connection);
        }
    }

    protected void loadCallePhoneNumber(TextView callePhoneNumberTxt, String phoneNumber) {
        callePhoneNumberTxt.setText("+" + phoneNumber);
        DialerLogs.messageI(TAG, "YO====loadCallePhoneNumber====" + phoneNumber);
    }

    protected void loadCalleeName(TextView textView, String name) {
        textView.setText(name);
        DialerLogs.messageI(TAG, "YO====loadCalleeName====" + name);
    }

    protected void loadCalleImage(ImageView imageView, String imagePath) {
        Glide.with(this).load(imagePath)
                .placeholder(R.drawable.ic_contacts)
                .dontAnimate()
                .error(R.drawable.ic_contacts).
                into(imageView);
        loadFullImage(imagePath);
        DialerLogs.messageI(TAG, "YO====loadCalleImage====" + imagePath);

    }

    protected void loadFullImage(String imagePath) {
        int myWidth = 512;
        int myHeight = 384;
        Glide.with(this).load(imagePath).asBitmap().into(new SimpleTarget<Bitmap>(myWidth, myHeight) {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                Drawable drawable = new BitmapDrawable(resource);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    fullImageLayout.setBackground(drawable);
                }
            }
        });
    }
}

package com.yo.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.voip.DialPadView;
import com.yo.android.voip.OutGoingCallActivity;
import com.yo.android.voip.SipService;

/**
 * Created by Ramesh on 27/6/16.
 */
public class DialerActivity extends BaseActivity {
    private DialPadView dialPadView;
    private ImageButton deleteButton;
    private static final int[] mButtonIds = new int[]{R.id.zero, R.id.one, R.id.two, R.id.three,
            R.id.four, R.id.five, R.id.six, R.id.seven, R.id.eight, R.id.nine, R.id.star,
            R.id.pound};
    private FloatingActionButton btnCallGreen;
    private FloatingActionButton btnDialer;
    private boolean show;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialer);
        getSupportActionBar().setTitle(R.string.activity_title_dialer);
        enableBack();
        //
        Intent in = new Intent(getBaseContext(), SipService.class);
        startService(in);

        dialPadView = (DialPadView) findViewById(R.id.dialPadView);
        btnCallGreen = (FloatingActionButton) findViewById(R.id.btnCall);
        btnDialer = (FloatingActionButton) findViewById(R.id.btnDialer);
        deleteButton = (ImageButton) findViewById(R.id.deleteButton);
        for (int id : mButtonIds) {
            dialPadView.findViewById(id).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView numberView = (TextView) v.findViewById(R.id.dialpad_key_number);
                    String prev = dialPadView.getDigits().getText().toString();
                    String current = prev + numberView.getText().toString();
                    dialPadView.getDigits().setText(current);
                    dialPadView.getDigits().setSelection(current.length());
                }
            });
        }
        dialPadView.setVisibility(View.GONE);
        btnCallGreen.setVisibility(View.GONE);
        btnDialer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialPad();
            }
        });
        btnCallGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideDialPad();
                String number = dialPadView.getDigits().getText().toString();
                if (number.length() > 0) {
                    Intent intent = new Intent(DialerActivity.this, OutGoingCallActivity.class);
                    intent.putExtra(OutGoingCallActivity.CALLER_NO, number);
                    startActivity(intent);
                }
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String prev = dialPadView.getDigits().getText().toString();
                String finalString = prev;
                if (prev.length() > 0) {
                    finalString = new StringBuilder(prev).deleteCharAt(prev.length() - 1).toString();
                }
                dialPadView.getDigits().setText(finalString);
                dialPadView.getDigits().setSelection(finalString.length());

            }
        });
    }

    private void showDialPad() {
        show = true;
        Animation bottomUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bottom_up);
        dialPadView.startAnimation(bottomUp);
        bottomUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                dialPadView.setVisibility(View.VISIBLE);
                btnCallGreen.setVisibility(View.VISIBLE);
                btnDialer.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        bottomUp.start();
    }

    private void hideDialPad() {
        show = false;
        Animation bottomUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bottom_down);
        dialPadView.startAnimation(bottomUp);
        bottomUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                dialPadView.setVisibility(View.GONE);
                btnCallGreen.setVisibility(View.GONE);
                btnDialer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        bottomUp.start();
    }

    @Override
    public void onBackPressed() {
        if (show) {
            hideDialPad();
            return;
        }
        super.onBackPressed();

    }
}

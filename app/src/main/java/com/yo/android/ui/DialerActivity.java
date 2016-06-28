package com.yo.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
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
                //TODO: Navigate to next scren
                String number = dialPadView.getDigits().getText().toString();
                Intent intent = new Intent(DialerActivity.this, OutGoingCallActivity.class);
                intent.putExtra(OutGoingCallActivity.CALLER_NO, number);
                startActivity(intent);
            }
        });

    }

    private void showDialPad() {
        show = true;
        dialPadView.setVisibility(View.VISIBLE);
        btnCallGreen.setVisibility(View.VISIBLE);
        btnDialer.setVisibility(View.GONE);
    }

    private void hideDialPad() {
        show = false;
        dialPadView.setVisibility(View.GONE);
        btnCallGreen.setVisibility(View.GONE);
        btnDialer.setVisibility(View.VISIBLE);
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

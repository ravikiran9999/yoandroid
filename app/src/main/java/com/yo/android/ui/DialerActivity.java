package com.yo.android.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.voip.DialPadView;

/**
 * Created by Ramesh on 27/6/16.
 */
public class DialerActivity extends BaseActivity {
    private static final int[] mButtonIds = new int[]{R.id.zero, R.id.one, R.id.two, R.id.three,
            R.id.four, R.id.five, R.id.six, R.id.seven, R.id.eight, R.id.nine, R.id.star,
            R.id.pound};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialer);
        getSupportActionBar().setTitle(R.string.activity_title_dialer);
        enableBack();
        //
        final DialPadView dialPadView = (DialPadView) findViewById(R.id.dialPadView);
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

    }
}

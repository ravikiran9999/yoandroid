package com.yo.android.chat.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.chat.ui.fragments.OTPFragment;
import com.yo.android.ui.BaseActivity;

import javax.inject.Inject;
import javax.inject.Named;

public class SignupActivity extends BaseActivity {

    private static final String FRAGMENT_TAG = "OTPFragment";

    @Inject
    @Named("login")
    PreferenceEndPoint preferenceEndPoint;

    EditText editPhone;

    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editPhone = (EditText)findViewById(R.id.signup_phone);
        Button signup = (Button) findViewById(R.id.email_sign_up_button);



        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber = editPhone.getText().toString();

                OTPFragment otpFragment = new OTPFragment();
                Bundle bundle = new Bundle();
                bundle.putString("phone", phoneNumber);
                otpFragment.setArguments(bundle);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.add(android.R.id.content, otpFragment, FRAGMENT_TAG);
                transaction.disallowAddToBackStack();
                transaction.commit();
            }
        });
    }


}

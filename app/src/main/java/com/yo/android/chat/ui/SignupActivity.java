package com.yo.android.chat.ui;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.model.Registration;
import com.yo.android.ui.BaseActivity;
import com.yo.android.ui.NavigationDrawerActivity;
import com.yo.android.util.DatabaseConstant;

import javax.inject.Inject;
import javax.inject.Named;

public class SignupActivity extends BaseActivity {

    @Inject
    @Named("login")
    PreferenceEndPoint preferenceEndPoint;

    private String phoneNumber;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        EditText editPhone = (EditText)findViewById(R.id.signup_phone);
        EditText editPassword = (EditText)findViewById(R.id.signup_password);
        Button signup = (Button) findViewById(R.id.email_sign_up_button);

        phoneNumber = editPhone.getText().toString();
        password = editPassword.getText().toString();

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp(phoneNumber, password);
            }
        });
    }

    private void signUp(@NonNull String phoneNumber, @NonNull String password) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(DatabaseConstant.APP_USERS);
        DatabaseReference childReference = databaseReference.child(phoneNumber);
        Registration registration = new Registration(password, phoneNumber);
        childReference.setValue(registration);
        preferenceEndPoint.saveStringPreference("phone", phoneNumber);
        //preferenceEndPoint.saveStringPreference("email", email);
        preferenceEndPoint.saveStringPreference("password", password);
        startActivity(new Intent(SignupActivity.this, NavigationDrawerActivity.class));
    }
}

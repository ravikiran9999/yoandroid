package com.yo.android.chat.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.chat.ui.fragments.OTPFragment;
import com.yo.android.model.Contact;
import com.yo.android.model.Contacts;
import com.yo.android.model.PhNumberBean;
import com.yo.android.ui.BaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;

public class SignupActivity extends BaseActivity {

    private static final String FRAGMENT_TAG = "OTPFragment";

    @Inject
    @Named("login")
    PreferenceEndPoint preferenceEndPoint;

    @Inject
    YoApi.YoService yoService;

    EditText editPhone;

    private String phoneNumber;
    private ArrayList<String> nc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editPhone = (EditText) findViewById(R.id.signup_phone);
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

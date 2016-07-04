package com.yo.android.chat.ui.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yo.android.R;
import com.yo.android.model.Registration;
import com.yo.android.ui.BottomTabsActivity;
import com.yo.android.util.Constants;

/**
 * A simple {@link Fragment} subclass.
 */
public class OTPFragment extends BaseFragment {

    private static final String tempPassword = "123456";
    private String phoneNumber;
    private EditText otp;


    public OTPFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle phoneNumberBundle = this.getArguments();
        phoneNumber = phoneNumberBundle.getString(Constants.PHONE_NUMBER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ot, container, false);


        Button verifyButton = (Button) view.findViewById(R.id.verify);
        otp = (EditText) view.findViewById(R.id.otp);

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = otp.getText().toString();
                if (password.equalsIgnoreCase(tempPassword)) {
                    signUp(phoneNumber, password);
                } else {
                    Toast.makeText(getActivity(), "Invalid OTP", Toast.LENGTH_LONG).show();
                }
            }
        });

        return view;
    }

    private void signUp(@NonNull String phoneNumber, @NonNull String password) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Constants.APP_USERS);
        DatabaseReference childReference = databaseReference.child(phoneNumber);
        Registration registration = new Registration(password, phoneNumber);
        childReference.setValue(registration);
        preferenceEndPoint.saveStringPreference(Constants.PHONE_NUMBER, phoneNumber);
        preferenceEndPoint.saveStringPreference("password", password);
        startActivity(new Intent(getActivity(), BottomTabsActivity.class));
    }

}

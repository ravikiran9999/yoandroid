package com.yo.android.chat.ui;


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
import com.yo.android.api.YoApi;
import com.yo.android.model.Registration;
import com.yo.android.ui.BottomTabsActivity;
import com.yo.android.util.DatabaseConstant;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class OTPFragment extends BaseFragment {

    private static final String tempPassword = "123456";
    private String phoneNumber;
    private EditText otp;
    @Inject
    YoApi.YoService yoService;


    public OTPFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle phoneNumberBundle = this.getArguments();
        phoneNumber = phoneNumberBundle.getString("phone");
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
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(DatabaseConstant.APP_USERS);
        DatabaseReference childReference = databaseReference.child(phoneNumber);
        Registration registration = new Registration(password, phoneNumber);
        childReference.setValue(registration);
        preferenceEndPoint.saveStringPreference("phone", phoneNumber);
        //preferenceEndPoint.saveStringPreference("email", email);
        preferenceEndPoint.saveStringPreference("password", password);
        startActivity(new Intent(getActivity(), BottomTabsActivity.class));
        //
        yoService.verifyOTP("83ade053e48c03568ab9f5c48884b8fb6fa0abb0ba5a0979da840417779e5c60",
                "1c1a8a358e287759f647285c847f2b95976993651e09d2d4523331f1f271ad49",
                "password", phoneNumber, "123456").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
        getActivity().finish();
    }

}

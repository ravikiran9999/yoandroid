package com.yo.android.chat.ui.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.orion.android.common.util.ConnectivityHelper;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.model.OTPResponse;
import com.yo.android.model.Registration;
import com.yo.android.ui.BottomTabsActivity;
import com.yo.android.ui.ProfileActivity;
import com.yo.android.util.Constants;
import com.yo.android.voip.IncomingSmsReceiver;

import java.util.Random;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class OTPFragment extends BaseFragment {

//    private static final String tempPassword = "123456";

    private String phoneNumber;
    private EditText etOtp;
    private Button verifyButton;
    private int count = 0;

    @Inject
    YoApi.YoService yoService;
    @Inject
    ContactsSyncManager contactsSyncManager;
    @Inject
    ConnectivityHelper mHelper;
    private TextView txtTimer;
    private Handler mHandler = new Handler();
    private final static int MAX_DURATION = 20;
    private int duration = MAX_DURATION;


    public OTPFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle phoneNumberBundle = this.getArguments();
        phoneNumber = phoneNumberBundle.getString(Constants.PHONE_NUMBER);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ot, container, false);


        verifyButton = (Button) view.findViewById(R.id.verify);
        etOtp = (EditText) view.findViewById(R.id.otp);
        txtTimer = (TextView) view.findViewById(R.id.txt_timer);

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = etOtp.getText().toString().trim();
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getActivity(), "OTP shouldn't empty", Toast.LENGTH_LONG).show();
                } else {
                    signUp(phoneNumber, password);

                }
            }
        });
        mHandler.post(runnable);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Debug purpose
        Random random = new Random();
        int duration = Math.max(0, random.nextInt(20));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                etOtp.setText("123456");
                //TODO: Show dialog
                mToastFactory.showToast("OTP is detected!");
                stopTimer();
            }
        }, duration * 1000L);

    }

    private void signUp(@NonNull final String phoneNumber, @NonNull final String password) {
        if (!mHelper.isConnected()) {
            mToastFactory.showToast(getActivity().getResources().getString(R.string.connectivity_network_settings));
            return;
        }
        showProgressDialog();
        count = 0;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Constants.APP_USERS);
        DatabaseReference childReference = databaseReference.child(phoneNumber);
        Registration registration = new Registration(password, phoneNumber);
        childReference.setValue(registration, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    // successfully inserted to database
                    count++;
                    if (count == 2) {
                        Intent intent = new Intent(getActivity(), ProfileActivity.class);
                        intent.putExtra(Constants.PHONE_NUMBER, phoneNumber);
                        dismissProgressDialog();
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        getActivity().finish();
                    }
                }
            }
        });

        //
        String countryCode = preferenceEndPoint.getStringPreference(Constants.COUNTRY_CODE_FROM_SIM);
        showProgressDialog();
        yoService.verifyOTP(YoApi.CLIENT_ID,
                YoApi.CLIENT_SECRET,
                "password", countryCode + phoneNumber, etOtp.getText().toString().trim()).enqueue(new Callback<OTPResponse>() {
            @Override
            public void onResponse(Call<OTPResponse> call, Response<OTPResponse> response) {
                contactsSyncManager.syncContacts();
                count++;
                navigateToNext(response, phoneNumber, password);
            }

            @Override
            public void onFailure(Call<OTPResponse> call, Throwable t) {
                dismissProgressDialog();
                mToastFactory.showToast(getActivity().getResources().getString(R.string.otp_failure));
            }
        });
    }

    private void navigateToNext(Response<OTPResponse> response, @NonNull String phoneNumber, @NonNull String password) {
        preferenceEndPoint.saveStringPreference(YoApi.ACCESS_TOKEN, response.body().getAccessToken());
        preferenceEndPoint.saveStringPreference(YoApi.REFRESH_TOKEN, response.body().getRefreshToken());
        preferenceEndPoint.saveStringPreference(Constants.PHONE_NUMBER, phoneNumber);
        preferenceEndPoint.saveStringPreference("password", password);
        dismissProgressDialog();
        if (count == 2) {
            contactsSyncManager.syncContacts();
            startActivity(new Intent(getActivity(), BottomTabsActivity.class));
            getActivity().finish();
        }
    }

    public void onEventMainThread(Bundle bundle) {
        String otp = IncomingSmsReceiver.extractOTP(bundle);
        this.etOtp.setText(otp);
        //TODO: Show dialog
        mToastFactory.showToast("OTP is detected!");
        stopTimer();
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            String timer = String.format("%d seconds left...", duration);
            txtTimer.setText(timer);
            txtTimer.setVisibility(View.VISIBLE);
            duration--;
            if (duration > 0) {
                mHandler.postDelayed(this, 1000);
                verifyButton.setEnabled(false);
            } else {
                txtTimer.setVisibility(View.GONE);
                //Reset
                duration = MAX_DURATION;
                verifyButton.setText("Re-Send OTP");
                verifyButton.setEnabled(true);
            }
        }
    };

    private void stopTimer() {
        mHandler.removeCallbacks(runnable);
        txtTimer.setVisibility(View.GONE);
        //Reset
        duration = MAX_DURATION;
        verifyButton.setEnabled(true);
    }


}

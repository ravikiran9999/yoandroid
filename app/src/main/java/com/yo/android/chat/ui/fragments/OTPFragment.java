package com.yo.android.chat.ui.fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
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

import com.orion.android.common.util.ConnectivityHelper;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.chat.ui.LoginActivity;
import com.yo.android.model.OTPResponse;
import com.yo.android.ui.BottomTabsActivity;
import com.yo.android.ui.UpdateProfileActivity;
import com.yo.android.util.Constants;
import com.yo.android.voip.IncomingSmsReceiver;
import com.yo.android.voip.VoipConstants;

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
    private final static int MAX_DURATION = 60;
    private int duration = MAX_DURATION;
    private Handler dummyOTPHandler = new Handler();
    private TextView reSendTextBtn;
    private boolean otpReceived = false;

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
        reSendTextBtn = (TextView) view.findViewById(R.id.resend);

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verifyButton.getText()
                        .equals(getString(R.string.resend_otp_text))) {
                    if (getActivity() instanceof LoginActivity) {
                        ((LoginActivity) getActivity()).callLoginService(phoneNumber);
                        mHandler.post(runnable);
                        generateDummyOTP();
                    }
                } else {
                    String password = etOtp.getText().toString().trim();
                    if (TextUtils.isEmpty(password)) {
                        mToastFactory.showToast(getString(R.string.otp_empty));
                    } else {
                        stopTimer();
                        signUp(phoneNumber, password);
                    }
                }
            }
        });
        reSendTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("Resend".equalsIgnoreCase(reSendTextBtn.getText().toString())) {
                    if (getActivity() instanceof LoginActivity) {
                        ((LoginActivity) getActivity()).callLoginService(phoneNumber);
                        mHandler.post(runnable);
                        generateDummyOTP();
                    }
                }
            }
        });
        mHandler.post(runnable);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        generateDummyOTP();
    }

    private Runnable dummyOTPRunnable = new Runnable() {
        @Override
        public void run() {
            dummyOTPHandler.removeCallbacks(this);
            try {
                // showOTPConfirmationDialog("123456");
            } catch (Exception e) {
            }
            stopTimer();
        }
    };

    private void generateDummyOTP() {
        //Debug purpose
        Random random = new Random();
        int duration = Math.max(0, random.nextInt(35));
        if (!BuildConfig.ORIGINAL_SMS_VERIFICATION) {
            dummyOTPHandler.removeCallbacks(dummyOTPRunnable);
            // mToastFactory.showToast("Your PIN will be sent in " + duration + " seconds.");
            dummyOTPHandler.postDelayed(dummyOTPRunnable, duration * 1000L);
        }
    }


    private void signUp(@NonNull final String phoneNumber, @NonNull final String password) {
        if (!mHelper.isConnected()) {
            mToastFactory.showToast(getActivity().getResources().getString(R.string.connectivity_network_settings));
            return;
        }
        showProgressDialog();
        count = 0;
        String countryCode = preferenceEndPoint.getStringPreference(Constants.COUNTRY_CODE_FROM_SIM);
        showProgressDialog();

        yoService.verifyOTP(BuildConfig.CLIENT_ID,
                BuildConfig.CLIENT_SECRET,
                "password", countryCode + phoneNumber, etOtp.getText().toString().trim()).enqueue(new Callback<OTPResponse>() {
            @Override
            public void onResponse(Call<OTPResponse> call, Response<OTPResponse> response) {
                dismissProgressDialog();
                if (response.isSuccessful()) {
                    preferenceEndPoint.saveBooleanPreference(Constants.SESSION_EXPIRE, false);
                    contactsSyncManager.syncContacts();
                    count++;
                    navigateToNext(response, phoneNumber, password);
                } else {
                    mToastFactory.showToast(getActivity().getResources().getString(R.string.otp_failure));
                }
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
        finishAndNavigateToHome();
    }

    private void finishAndNavigateToHome() {
        contactsSyncManager.syncContacts();
        //
        final boolean isNewUser = preferenceEndPoint.getBooleanPreference("isNewUser");
        if (isNewUser) {
            //TODO:Enable flag for Profile
            preferenceEndPoint.saveBooleanPreference(Constants.ENABLE_PROFILE_SCREEN, true);
            preferenceEndPoint.saveBooleanPreference(Constants.LOGED_IN, true);
            Intent intent = new Intent(getActivity(), UpdateProfileActivity.class);
            intent.putExtra(Constants.PHONE_NUMBER, phoneNumber);
            dismissProgressDialog();
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            startActivity(new Intent(getActivity(), BottomTabsActivity.class));
        }
        //
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(VoipConstants.NEW_ACCOUNT_REGISTRATION);
        getActivity().sendBroadcast(broadcastIntent);
        getActivity().finish();

    }

    public void onEventMainThread(Bundle bundle) {
        if (otpReceived) {
            mLog.e("OTPFragment", "onEventMainThread: otp is already received!");
            return;
        }
        if (!isAdded() || getActivity() == null) {
            mLog.e("OTPFragment", "onEventMainThread: activity is already destroyed");
            return;
        }
        otpReceived = true;
        stopTimer();
        String otp = IncomingSmsReceiver.extractOTP(bundle);
        this.etOtp.setText(otp);
        if (otp != null) {
            try {
                showOTPConfirmationDialog(otp);
            } catch (Exception e) {
            }
        }
    }

    public void showOTPConfirmationDialog(final String otp) {
        try {
            if (getActivity() == null) {
                mLog.e("OTPFragment", "showOTPConfirmationDialog: activity is already destroyed");
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("YoApp");
            builder.setMessage("We are detected your PIN : " + otp);
            builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    etOtp.setText(otp);
                    verifyButton.setText(getActivity().getString(R.string.otp_button_submit));
                    //Auto click
                    verifyButton.performClick();
                }
            });
            builder.setCancelable(false);
            builder.setNegativeButton("Skip", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    reSendTextBtn.setText("Resend");
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } catch (Exception e) {
        }
    }


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            String timer = String.format("%d seconds left...", duration);
            txtTimer.setText(timer);
            formatDuration(duration);
            txtTimer.setVisibility(View.GONE);
            duration--;
            if (duration > 0) {
                mHandler.postDelayed(this, 1000);
//                verifyButton.setEnabled(false);
            } else {
                txtTimer.setVisibility(View.GONE);
                reSendTextBtn.setText("Resend");
                reSendTextBtn.setEnabled(true);
                //Reset
                duration = MAX_DURATION;
                // verifyButton.setText(R.string.resend_otp_text);
                verifyButton.setEnabled(true);
                stopTimer();
            }
        }
    };

    private void formatDuration(int duration) {
        String str = duration + "";
        if (duration < 10) {
            str = "0" + duration;
        }
        reSendTextBtn.setText("Resend (00:" + str + ")");
        reSendTextBtn.setEnabled(false);
    }

    private void stopTimer() {
        dummyOTPHandler.removeCallbacks(dummyOTPRunnable);
        mHandler.removeCallbacks(runnable);
        txtTimer.setVisibility(View.GONE);
        //Reset
        duration = MAX_DURATION;
        verifyButton.setEnabled(true);
        reSendTextBtn.setText("Resend");
        reSendTextBtn.setEnabled(true);
    }


}

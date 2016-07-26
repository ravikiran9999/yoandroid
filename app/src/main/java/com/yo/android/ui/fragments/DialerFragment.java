package com.yo.android.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.SearchView;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.orion.android.common.util.ConnectivityHelper;
import com.yo.android.R;
import com.yo.android.adapters.CallLogsAdapter;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.model.dialer.CallLogsResponse;
import com.yo.android.model.dialer.CallLogsResult;
import com.yo.android.ui.CountryListActivity;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;
import com.yo.android.voip.DialPadView;
import com.yo.android.voip.OutGoingCallActivity;
import com.yo.android.vox.BalanceHelper;
import com.yo.android.vox.VoxApi;
import com.yo.android.vox.VoxFactory;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ramesh on 3/7/16.
 */
public class DialerFragment extends BaseFragment {

    public static final String REFRESH_CALL_LOGS = "com.yo.android.ACTION_REFRESH_CALL_LOGS";
    @Inject
    VoxApi.VoxService service;

    @Inject
    VoxFactory voxFactory;
    @Bind(R.id.listView)
    ListView listView;
    @Bind(R.id.progress)
    ProgressBar progress;
    @Bind(R.id.txtEmptyCallLogs)
    TextView txtEmptyCallLogs;
    @Bind(R.id.floatingDialer)
    View floatingDialer;

    private MenuItem searchMenuItem;
    private SearchView searchView;
    private static final String TAG = "DialerFragment";
    private EventBus bus = EventBus.getDefault();
    private CallLogsAdapter adapter;
    private DialPadView dialPadView;
    private EditText mDigits;
    private ImageButton deleteButton;
    private static final int[] mButtonIds = new int[]{R.id.zero, R.id.one, R.id.two, R.id.three,
            R.id.four, R.id.five, R.id.six, R.id.seven, R.id.eight, R.id.nine, R.id.star,
            R.id.pound};
    private ImageView btnCallGreen;
    private ImageView btnDialer;
    private TextView txtBalance;
    private TextView txtCallRate;
    private View bottom_layout;
    private boolean show;
    @Inject
    ConnectivityHelper mConnectivityHelper;
    @Inject
    BalanceHelper mBalanceHelper;
    @Inject
    @Named("voip_support")
    boolean isVoipSupported;
    private Menu menu;
    private List<CallLogsResult> appCalls = new ArrayList<>();
    private List<CallLogsResult> paidCalls = new ArrayList<>();
    private String sUserSimCountryCode;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_dialer_fragment, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        bus.register(this);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_dialer, menu);
        this.menu = menu;
        Util.prepareSearch(getActivity(), menu, adapter);
        Util.changeSearchProperties(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        hideDialPad(true);
        String str = null;
        if (item.getItemId() == R.id.menu_all_calls) {
            str = "all calls";
        } else if (item.getItemId() == R.id.menu_paid_calls) {
            str = "paid calls";
        } else if (item.getItemId() == R.id.menu_app_calls) {
            str = "app calls";
        } else if (item.getItemId() == R.id.menu_clear_history) {
            mToastFactory.showToast("Clear call history is not yet implemented.");
        }
        if (str != null) {
            preferenceEndPoint.saveStringPreference(Constants.DIALER_FILTER, str);
            showDataOnFilter();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);
        dialPadView = (DialPadView) view.findViewById(R.id.dialPadView);
        mDigits = dialPadView.getDigits();
        bottom_layout = view.findViewById(R.id.bottom_layout);
        txtBalance = (TextView) view.findViewById(R.id.txt_balance);
        txtCallRate = (TextView) view.findViewById(R.id.txt_call_rate);
        btnCallGreen = (ImageView) view.findViewById(R.id.btnCall);
        btnDialer = (ImageView) view.findViewById(R.id.btnDialer);
        view.findViewById(R.id.btnMessage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mToastFactory.showToast("Message: Need to implement");
            }
        });
        view.findViewById(R.id.btnContacts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mToastFactory.showToast("Contacts: Need to implement");
            }
        });
        deleteButton = (ImageButton) view.findViewById(R.id.deleteButton);
        for (int id : mButtonIds) {
            dialPadView.findViewById(id).setOnClickListener(keyPadButtonsClickListener());
        }
        dialPadView.findViewById(R.id.zero).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (dialPadView.getDigits().getText().toString().trim().length() == 0) {
                    dialPadView.getDigits().setText("+");
                    dialPadView.getDigits().setSelection(1);
                    return true;
                }
                return false;
            }
        });

        btnDialer.setVisibility(View.GONE);
        btnDialer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialPad();
            }
        });
        btnCallGreen.setOnClickListener(btnCallGreenClickListener());
        deleteButton.setOnClickListener(btnDeleteClickListener());
        deleteButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                dialPadView.getDigits().setText("");
                return true;
            }
        });
        String balance = preferenceEndPoint.getStringPreference(Constants.CURRENT_BALANCE, "2.0");
        txtBalance.setText("Balance $" + balance);
        //
        setCallRateText();
        txtCallRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getActivity(), CountryListActivity.class), 100);
            }
        });
        hideDialPad(false);
    }

    @NonNull
    private View.OnClickListener keyPadButtonsClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView numberView = (TextView) v.findViewById(R.id.dialpad_key_number);
                String keyPadText = numberView.getText().toString();
                updateDialString(keyPadText.charAt(0));
            }
        };
    }

    @NonNull
    private View.OnClickListener btnDeleteClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String prev = dialPadView.getDigits().getText().toString();
                String finalString = prev;
                int startPos = dialPadView.getDigits().getSelectionStart();
                int endPos = dialPadView.getDigits().getSelectionEnd();
                try {
                    String str = new StringBuilder(prev).replace(startPos - 1, endPos, "").toString();
                    mLog.i("Dialer", "final:" + str);
                    dialPadView.getDigits().setText(str);
                    dialPadView.getDigits().setSelection(startPos - 1);
                } catch (Exception e) {
                    mLog.w("DialerActivity", e);
                }

            }
        };
    }

    @NonNull
    private View.OnClickListener btnCallGreenClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Need to improve logic for PSTN calls
                //Begin Normalizing PSTN number
                String temp = dialPadView.getDigits().getText().toString().trim();
                String cPrefix = preferenceEndPoint.getStringPreference(Constants.COUNTRY_CODE_PREFIX, null);
                if (cPrefix != null) {
                    cPrefix = cPrefix.replace("+", "");
                }
                String number = temp.replace(" ", "").replace("+", "");
                if (cPrefix != null && !number.startsWith(cPrefix)) {
                    number = cPrefix + number;
                }
                mLog.i(TAG, "Dialing number after normalized: " + number);
                //End Normalizing PSTN number
                if (!isVoipSupported) {
                    mToastFactory.newToast(getString(R.string.voip_not_supported_error_message), Toast.LENGTH_SHORT);
                } else if (!mConnectivityHelper.isConnected()) {
                    mToastFactory.showToast(getString(R.string.connectivity_network_settings));
                } else if (!isVoipSupported) {
                    mToastFactory.newToast(getString(R.string.voip_not_supported_error_message), Toast.LENGTH_LONG);
                } else if (number.length() == 0) {
                    mToastFactory.showToast("Please enter number.");
                } else {
                    Intent intent = new Intent(getActivity(), OutGoingCallActivity.class);
                    intent.putExtra(OutGoingCallActivity.CALLER_NO, number);
                    startActivity(intent);
                }
            }
        };
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadDefaultSimCountry();
        adapter = new CallLogsAdapter(getActivity(), preferenceEndPoint);
        listView.setAdapter(adapter);
        loadCallLogs();
    }

    @OnClick(R.id.floatingDialer)
    public void onDialerClick() {
        showDialPad();
    }

    private void loadCallLogs() {
        showProgressDialog();
        appCalls.clear();
        paidCalls.clear();
        final String phone = preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER);
        service.executeAction(voxFactory.getCallLogsBody(phone)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                dismissProgressDialog();
                try {
                    CallLogsResponse callLogsResponse = new Gson().fromJson(new InputStreamReader(response.body().byteStream()), CallLogsResponse.class);
                    List<CallLogsResult> list = callLogsResponse.getDATA().getRESULT();
                    if (list != null) {
                        for (CallLogsResult callLogsResult : list) {
                            if ("SIP2SIP Call".equalsIgnoreCase(callLogsResult.getDestination_name())) {
                                appCalls.add(callLogsResult);
                            } else {
                                paidCalls.add(callLogsResult);
                            }
                        }
                        Collections.sort(appCalls, new Comparator<CallLogsResult>() {
                            @Override
                            public int compare(CallLogsResult lhs, CallLogsResult rhs) {
                                return (int) (Util.getTime(rhs.getStime()) - Util.getTime(lhs.getStime()));
                            }
                        });
                        Collections.sort(paidCalls, new Comparator<CallLogsResult>() {
                            @Override
                            public int compare(CallLogsResult lhs, CallLogsResult rhs) {
                                return (int) (Util.getTime(rhs.getStime()) - Util.getTime(lhs.getStime()));
                            }
                        });
                        showDataOnFilter();

                    }
                } catch (JsonSyntaxException e) {
                    mLog.w("DialerFragment", "loadCallLogs", e);
                }
                showEmptyText();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                dismissProgressDialog();
                showEmptyText();
            }
        });

    }

    private void showDataOnFilter() {
        final String filter = preferenceEndPoint.getStringPreference(Constants.DIALER_FILTER, "all calls");
        List<CallLogsResult> results = new ArrayList<>();
        if (filter.equalsIgnoreCase("all calls")) {
            prepare("All Calls", results, appCalls);
            prepare("Paid Calls", results, paidCalls);
        } else if (filter.equalsIgnoreCase("App Calls")) {
            prepare("App Calls", results, appCalls);
        } else {
            prepare("Paid Calls", results, paidCalls);
        }
        adapter.addItems(results);
        showEmptyText();

    }

    private void prepare(String type, List<CallLogsResult> results, List<CallLogsResult> checkList) {
        if (!checkList.isEmpty()) {
            CallLogsResult result = new CallLogsResult();
            result.setHeader(true);
            result.setHeaderTitle(type);
            results.add(result);
            results.addAll(checkList);
        }
    }

    private void showEmptyText() {
        final String filter = preferenceEndPoint.getStringPreference(Constants.DIALER_FILTER, "all calls");
        if (filter.equalsIgnoreCase("all calls")) {
            txtEmptyCallLogs.setVisibility(View.VISIBLE);
            txtEmptyCallLogs.setText("No call logs history available.");
        } else {
            txtEmptyCallLogs.setText(String.format("No %s history available.", filter));
        }
        boolean nonEmpty = show || (listView.getAdapter() != null && listView.getAdapter().getCount() > 0);
        txtEmptyCallLogs.setVisibility(nonEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void showProgressDialog() {
        progress.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissProgressDialog() {
        progress.setVisibility(View.GONE);
    }

    public Menu getMenu() {
        return menu;
    }


    /**
     * @param action
     */
    public void onEventMainThread(String action) {
        if (action.equals(REFRESH_CALL_LOGS)) {
            loadCallLogs();
            mBalanceHelper.checkBalance();
        }
    }

    private void showDialPad() {
        showOrHideTabs(false);
        show = true;
        txtEmptyCallLogs.setVisibility(View.GONE);
        dialPadView.setVisibility(View.VISIBLE);
        floatingDialer.setVisibility(View.GONE);
        Animation bottomUp = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.bottom_up);
        bottomUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                dialPadView.setVisibility(View.VISIBLE);
                btnCallGreen.setVisibility(View.VISIBLE);
                bottom_layout.setVisibility(View.VISIBLE);
                btnDialer.setVisibility(View.GONE);

                showEmptyText();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        dialPadView.startAnimation(bottomUp);
    }

    private void hideDialPad(boolean animate) {
        show = false;

        if (!animate) {
            dialPadView.setVisibility(View.GONE);
            btnCallGreen.setVisibility(View.GONE);
            bottom_layout.setVisibility(View.GONE);
            showOrHideTabs(true);
            floatingDialer.setVisibility(View.VISIBLE);
            showEmptyText();
            return;
        }
        Animation bottomUp = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_down);
        dialPadView.startAnimation(bottomUp);
        bottomUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                dialPadView.setVisibility(View.GONE);
                btnCallGreen.setVisibility(View.GONE);
                bottom_layout.setVisibility(View.GONE);
                showOrHideTabs(true);
                floatingDialer.setVisibility(View.VISIBLE);
                showEmptyText();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        bottomUp.start();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            setCallRateText();
        }
    }

    private void setCallRateText() {
        String cName = preferenceEndPoint.getStringPreference(Constants.COUNTRY_NAME, null);
        String cRate = preferenceEndPoint.getStringPreference(Constants.COUNTRY_CALL_RATE, null);
        String cPulse = preferenceEndPoint.getStringPreference(Constants.COUNTRY_CALL_PULSE, null);
        String cPrefix = preferenceEndPoint.getStringPreference(Constants.COUNTRY_CODE_PREFIX, null);

        if (!TextUtils.isEmpty(cName)) {
            String pulse;
            if (cPulse.equals("60")) {
                pulse = "min";
            } else {
                pulse = "sec";
            }

            txtCallRate.setText(cName + "\n$" + cRate + "/" + pulse);
            if (!TextUtils.isEmpty(cPrefix)) {
                //TODO: Need to improve the logic
                String str = dialPadView.getDigits().getText().toString();
                str = str.substring(str.indexOf(" ") + 1);
                dialPadView.getDigits().setText(cPrefix + " " + str);
                dialPadView.getDigits().setSelection(cPrefix.length());
            }
        }
    }

    @Override
    public boolean onBackPressHandle() {
        if (show) {
            hideDialPad(true);
            return true;
        }
        return super.onBackPressHandle();
    }
    //

    /**
     * Updates the dial string (mDigits) after inserting a Pause character (,)
     * or Wait character (;).
     */
    private void updateDialString(char newDigit) {

        int selectionStart;
        int selectionEnd;

        // SpannableStringBuilder editable_text = new SpannableStringBuilder(mDigits.getText());
        int anchor = mDigits.getSelectionStart();
        int point = mDigits.getSelectionEnd();

        selectionStart = Math.min(anchor, point);
        selectionEnd = Math.max(anchor, point);

        if (selectionStart == -1) {
            selectionStart = selectionEnd = mDigits.length();
        }

        Editable digits = mDigits.getText();

        if (canAddDigit(digits, selectionStart, selectionEnd)) {
            digits.replace(selectionStart, selectionEnd, Character.toString(newDigit));

            if (selectionStart != selectionEnd) {
                // Unselect: back to a regular cursor, just pass the character inserted.
                mDigits.setSelection(selectionStart + 1);
            }
        }
    }

    private boolean canAddDigit(CharSequence digits, int start, int end) {
        // False if no selection, or selection is reversed (end < start)
        if (start == -1 || end < start) {
            return false;
        }
        // unsupported selection-out-of-bounds state
        if (start > digits.length() || end > digits.length()) return false;

        // Special digit cannot be the first digit
        if (start == 0) return true;
        return true;
    }

    private void loadDefaultSimCountry() {
        final TelephonyManager manager = (TelephonyManager) getActivity().getSystemService(
                Context.TELEPHONY_SERVICE);
        if (manager != null) {
            sUserSimCountryCode = manager.getSimCountryIso();
        }
    }
}

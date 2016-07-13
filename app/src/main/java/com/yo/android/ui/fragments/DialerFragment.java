package com.yo.android.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.yo.android.adapters.AbstractBaseAdapter;
import com.yo.android.adapters.AbstractViewHolder;
import com.yo.android.adapters.ContactsListAdapter;
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
    @Bind(R.id.txtAppCalls)
    TextView txtAppCalls;
    @Bind(R.id.floatingDialer)
    View floatingDialer;

    private MenuItem searchMenuItem;
    private SearchView searchView;
    private static final String TAG = "DialerFragment";
    private EventBus bus = EventBus.getDefault();
    private CallLogsAdapter adapter;
    private DialPadView dialPadView;
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
        Util.prepareSearch(getActivity(), menu, adapter);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        hideDialPad(true);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        //

        dialPadView = (DialPadView) view.findViewById(R.id.dialPadView);
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
        btnDialer.setVisibility(View.GONE);
        btnDialer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialPad();
            }
        });
        btnCallGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String number = dialPadView.getDigits().getText().toString().trim();
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
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadCallLogs();
    }

    @OnClick(R.id.floatingDialer)
    public void onDialerClick() {
        showDialPad();
    }

    private void loadCallLogs() {
        adapter = new CallLogsAdapter(getActivity());
        listView.setAdapter(adapter);
        showProgressDialog();

        final String phone = preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER);
        service.executeAction(voxFactory.getCallLogsBody(phone)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                dismissProgressDialog();
                try {
                    CallLogsResponse callLogsResponse = new Gson().fromJson(new InputStreamReader(response.body().byteStream()), CallLogsResponse.class);
                    List<CallLogsResult> list = callLogsResponse.getDATA().getRESULT();
                    if (list != null) {
                        Collections.sort(list, new Comparator<CallLogsResult>() {
                            @Override
                            public int compare(CallLogsResult lhs, CallLogsResult rhs) {
                                return (int) (Util.getTime(rhs.getStime()) - Util.getTime(lhs.getStime()));
                            }
                        });
                        adapter.addItems(list);
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

    private void showEmptyText() {
        boolean nonEmpty = show || listView.getAdapter().getCount() > 0;
        txtEmptyCallLogs.setVisibility(nonEmpty ? View.GONE : View.VISIBLE);
        txtAppCalls.setVisibility(nonEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showProgressDialog() {
        progress.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissProgressDialog() {
        progress.setVisibility(View.GONE);
    }

    public class CallLogsViewHolder extends AbstractViewHolder {

        private TextView opponentName;
        private TextView timeStamp;

        private ImageView messageIcon;

        private ImageView callIcon;

        public CallLogsViewHolder(View view) {
            super(view);
            opponentName = (TextView) view.findViewById(R.id.tv_phone_number);
            timeStamp = (TextView) view.findViewById(R.id.tv_time_stamp);
            messageIcon = (ImageView) view.findViewById(R.id.iv_message_type);
            callIcon = (ImageView) view.findViewById(R.id.iv_contact_type);
        }

        public ImageView getMessageIcon() {
            return messageIcon;
        }

        public TextView getOpponentName() {
            return opponentName;
        }

        public TextView getTimeStamp() {
            return timeStamp;
        }

        public ImageView getCallIcon() {
            return callIcon;
        }
    }

    public class CallLogsAdapter extends AbstractBaseAdapter<CallLogsResult, CallLogsViewHolder> {

        public CallLogsAdapter(Context context) {
            super(context);
        }

        @Override
        public int getLayoutId() {
            return R.layout.dialer_calllogs_list_item;
        }

        @Override
        public CallLogsViewHolder getViewHolder(View convertView) {
            return new CallLogsViewHolder(convertView);
        }

        @Override
        protected boolean hasData(CallLogsResult event, String key) {
            if (event.getDialnumber().contains(key)) {
                return true;
            }
            return super.hasData(event, key);
        }

        @Override
        public void bindView(int position, CallLogsViewHolder holder, final CallLogsResult item) {
            holder.getOpponentName().setText(item.getDialnumber());
            item.getDialedstatus();//NOT  ANSWER,ANSWER
            if (item.getDialedstatus().equalsIgnoreCase("NOT ANSWER")) {
                holder.getTimeStamp().setText(Util.parseDate(item.getStime()));
                holder.getTimeStamp().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_redarrowdown, 0, 0, 0);
            } else {
                holder.getTimeStamp().setText(Util.parseDate(item.getStime()));
                holder.getTimeStamp().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_greenarrowup, 0, 0, 0);
            }
            holder.getCallIcon().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), OutGoingCallActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(OutGoingCallActivity.CALLER_NO, item.getDialnumber());
                    getActivity().startActivity(intent);
                }
            });
            holder.getMessageIcon().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContactsListAdapter.showUserChatScreen(getActivity(), preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER), item.getDialnumber());
                }
            });
        }


    }

    public void onEventMainThread(String action) {
        if (action.equals(REFRESH_CALL_LOGS)) {
            loadCallLogs();
            mBalanceHelper.checkBalance();
        }
    }

    private void showDialPad() {
        showOrHideTabs(false);
        show = true;
        dialPadView.setVisibility(View.VISIBLE);
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
                floatingDialer.setVisibility(View.GONE);
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
        floatingDialer.setVisibility(View.VISIBLE);
        if (!animate) {
            dialPadView.setVisibility(View.GONE);
            btnCallGreen.setVisibility(View.GONE);
            bottom_layout.setVisibility(View.GONE);
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
                showEmptyText();
//                btnDialer.setVisibility(View.VISIBLE);
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
                dialPadView.getDigits().setText(cPrefix);
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
}

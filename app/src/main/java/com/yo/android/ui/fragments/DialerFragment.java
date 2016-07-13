package com.yo.android.ui.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.yo.android.R;
import com.yo.android.adapters.AbstractBaseAdapter;
import com.yo.android.adapters.AbstractViewHolder;
import com.yo.android.adapters.ContactsListAdapter;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.model.dialer.CallLogsResponse;
import com.yo.android.model.dialer.CallLogsResult;
import com.yo.android.ui.DialerActivity;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;
import com.yo.android.voip.OutGoingCallActivity;
import com.yo.android.vox.VoxApi;
import com.yo.android.vox.VoxFactory;

import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

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
    private MenuItem searchMenuItem;
    private SearchView searchView;
    private static final String TAG = "DialerFragment";
    private EventBus bus = EventBus.getDefault();
    private CallLogsAdapter adapter;

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
        Util.prepareSearch(getActivity(),menu,adapter);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadCallLogs();
    }

    @OnClick(R.id.btnDialer)
    public void onDialerClick() {
        startActivity(new Intent(getActivity(), DialerActivity.class));
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
        boolean nonEmpty = listView.getAdapter().getCount() > 0;
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
        }
    }



}

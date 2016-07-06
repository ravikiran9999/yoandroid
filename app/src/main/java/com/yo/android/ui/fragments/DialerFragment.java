package com.yo.android.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import com.yo.android.voip.OutGoingCallActivity;
import com.yo.android.vox.VoxApi;
import com.yo.android.vox.VoxFactory;

import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ramesh on 3/7/16.
 */
public class DialerFragment extends BaseFragment {

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_dialer_fragment, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_dialer, menu);
        super.onCreateOptionsMenu(menu, inflater);
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
        final CallLogsAdapter adapter = new CallLogsAdapter(getActivity());
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
                    adapter.addItems(list);
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
        public void bindView(int position, CallLogsViewHolder holder, final CallLogsResult item) {
            holder.getOpponentName().setText(item.getDialnumber());
            item.getDialedstatus();//NOT  ANSWER,ANSWER
            if (item.getDialedstatus().equalsIgnoreCase("NOT ANSWER")) {
                holder.getTimeStamp().setText(parseDate(item.getStime()));
                holder.getTimeStamp().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_redarrowdown, 0, 0, 0);
            } else {
                holder.getTimeStamp().setText(parseDate(item.getStime()));
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

        private String parseDate(String s) {
            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(s);
                String timeStamp = DateUtils.getRelativeTimeSpanString(date.getTime(), System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS).toString();
                return timeStamp;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return s;
        }
    }

}

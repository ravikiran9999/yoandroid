package com.yo.android.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.yo.android.R;
import com.yo.android.adapters.AbstractBaseAdapter;
import com.yo.android.adapters.CallLogsAdapter;
import com.yo.android.calllogs.CallLog;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.helpers.SpendDetailsViewHolder;
import com.yo.android.model.dialer.CallLogsResult;
import com.yo.android.model.dialer.SpentDetailResponse;
import com.yo.android.model.dialer.SubscribersList;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;
import com.yo.android.vox.BalanceHelper;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ramesh on 25/7/16.
 */
public class SpendDetailsFragment extends BaseFragment implements Callback<ResponseBody> {

    @Bind(R.id.txtEmpty)
    TextView txtEmpty;

    @Bind(R.id.progress)
    ProgressBar progress;

    @Bind(R.id.listView)
    ListView listView;

    @Inject
    BalanceHelper mBalanceHelper;

    private CallLogsAdapter adapter;
    @Inject
    ContactsSyncManager mContactsSyncManager;

    private List<CallLogsResult> appCalls = new ArrayList<>();
    private List<CallLogsResult> paidCalls = new ArrayList<>();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_spend_details, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    private void loadCallLogs() {
        appCalls.clear();
        paidCalls.clear();
        appCalls = CallLog.Calls.getAppToAppCallLog(getActivity());
        paidCalls = CallLog.Calls.getPSTNCallLog(getActivity());
        showEmptyText();
        showDataOnFilter();
    }

    private void showDataOnFilter() {
        final String filter = preferenceEndPoint.getStringPreference(Constants.DIALER_FILTER, "all calls");
        List<CallLogsResult> results = new ArrayList<>();
        if (filter.equalsIgnoreCase("all calls")) {

            prepare("All Calls", results, CallLog.Calls.getCallLog(getActivity()));
        } else if (filter.equalsIgnoreCase("App Calls")) {
            prepare("App Calls", results, appCalls);
        } else {
            prepare("Paid Calls", results, paidCalls);
        }
        adapter.addItems(results);
        showEmptyText();

        dismissProgressDialog();

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


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new CallLogsAdapter(getActivity(), preferenceEndPoint, mContactsSyncManager);
        listView.setAdapter(adapter);
        showProgressDialog();
        loadCallLogs();


    }

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        if (isAdded() && response.body() != null) {
            dismissProgressDialog();
            try {
                String str = Util.toString(response.body().byteStream());
                SpentDetailResponse detailResponse = new Gson().fromJson(str, SpentDetailResponse.class);
                List<SubscribersList> lists = detailResponse.getData().getSubscriberslist();
//                adapter.addItems(lists);
            } catch (Exception e) {
                mLog.w("SpendDetails", "onResponse", e);
            }
            showEmptyText();
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        if (isAdded()) {
            dismissProgressDialog();
            adapter.clearAll();
            showEmptyText();
        }
    }

    private void showEmptyText() {
        boolean showEmpty = adapter.getCount() == 0;
        txtEmpty.setVisibility(showEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showProgressDialog() {
        progress.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissProgressDialog() {
        progress.setVisibility(View.GONE);
    }


    public static class SpentDetailsAdapter extends AbstractBaseAdapter<SubscribersList, SpendDetailsViewHolder> {

        public SpentDetailsAdapter(Context context) {
            super(context);
        }

        @Override
        public int getLayoutId() {
            return R.layout.frag_spent_list_row_item;
        }

        @Override
        public SpendDetailsViewHolder getViewHolder(View convertView) {
            return new SpendDetailsViewHolder(convertView);
        }

        @Override
        public void bindView(int position, SpendDetailsViewHolder holder, final SubscribersList item) {
            holder.getDate().setText(item.getTime());
            holder.getDuration().setText(item.getDuration());
            holder.getTxtPhone().setText(item.getDestination());
            holder.getTxtPrice().setText("$" + item.getCallcost());
            try {
                DecimalFormat df = new DecimalFormat("0.000");
                String format = df.format(Double.valueOf(item.getCallcost()));
                holder.getTxtPrice().setText("$" + format);
            } catch (Exception e) {
                e.printStackTrace();
            }

            holder.getArrow().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.setArrowDown(!item.isArrowDown());
                    notifyDataSetChanged();
                }
            });
            if (item.isArrowDown()) {
                holder.getArrow().setImageResource(R.drawable.ic_downarrow);
                holder.getDurationContainer().setVisibility(View.VISIBLE);
            } else {
                holder.getArrow().setImageResource(R.drawable.ic_uparrow);
                holder.getDurationContainer().setVisibility(View.GONE);
            }
        }
    }

}

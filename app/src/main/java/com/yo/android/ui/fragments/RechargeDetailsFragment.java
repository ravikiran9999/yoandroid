package com.yo.android.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.helpers.RechargeDetailsViewHolder;
import com.yo.android.model.PaymentHistoryItem;
import com.yo.android.util.Constants;
import com.yo.android.util.DateUtil;
import com.yo.android.vox.BalanceHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ramesh on 25/7/16.
 */
public class RechargeDetailsFragment extends BaseFragment implements Callback<List<PaymentHistoryItem>>, SharedPreferences.OnSharedPreferenceChangeListener {

    @Bind(R.id.txtEmpty)
    TextView txtEmpty;

    @Bind(R.id.progress)
    ProgressBar progress;

    @Bind(R.id.listView)
    RecyclerView listView;
    @Inject
    BalanceHelper mBalanceHelper;
    private RechargeDetailsAdapter adapter;

    public static RechargeDetailsFragment newInstance() {
        return new RechargeDetailsFragment();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        preferenceEndPoint.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new RechargeDetailsAdapter(getActivity(), new ArrayList<PaymentHistoryItem>());
        preferenceEndPoint.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setAdapter(adapter);
        listView.setLayoutManager(new LinearLayoutManager(getActivity()));
        if (isAdded()) {
            showProgressDialog();
        }
        mBalanceHelper.loadPaymentHistory(this);
    }

    @Override
    public void onResponse(Call<List<PaymentHistoryItem>> call, Response<List<PaymentHistoryItem>> response) {
        if (isAdded() && response.body() != null) {
            dismissProgressDialog();
            adapter.addItems(response.body());
            showEmptyText();
        }
    }

    @Override
    public void onFailure(Call<List<PaymentHistoryItem>> call, Throwable t) {
        if (isAdded()) {
            dismissProgressDialog();
            adapter.clearAll();
            showEmptyText();
        }
    }

    private void showEmptyText() {
        boolean showEmpty = adapter.getItemCount() == 0;
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

    private static class RechargeDetailsAdapter extends RecyclerView.Adapter<RechargeDetailsViewHolder> {
        private List<PaymentHistoryItem> mPaymentHistoryItemList;
        private Context mContext;

        public RechargeDetailsAdapter(Context context, List<PaymentHistoryItem> list) {
            mPaymentHistoryItemList = list;
            mContext = context;
        }


        @Override
        public RechargeDetailsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the custom layout
            //View contactView = inflater.inflate(R.layout.frag_spent_list_row_item, parent, false);
            View contactView = inflater.inflate(R.layout.frag_recharge_list_row_item, parent, false);

            // Return a new holder instance
            //SpendDetailsViewHolder viewHolder = new SpendDetailsViewHolder(contactView);
            RechargeDetailsViewHolder viewHolder = new RechargeDetailsViewHolder(contactView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RechargeDetailsViewHolder holder, int position) {
            final PaymentHistoryItem item = mPaymentHistoryItemList.get(position);
            holder.getDate().setVisibility(View.GONE);
            holder.getDuration().setVisibility(View.GONE);
            String modifiedTime = item.getUpdatedAt().substring(0, item.getUpdatedAt().lastIndexOf("."));
            holder.getTxtPhone().setText(modifiedTime);
            //                Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(modifiedTime);
            Date date = DateUtil.convertUtcToGmt(modifiedTime);
            holder.getTxtPhone().setText(new SimpleDateFormat(DateUtil.DATE_FORMAT2).format(date));

            holder.getTxtPulse().setText(item.getStatus());

            if(mContext.getResources().getString(R.string.voucher_failed).equals(item.getAddedCredit())) {
                holder.getTxtPrice().setText(item.getMessage());
                holder.getTxtPulse().setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
            } else {
                //holder.getTxtPrice().setText(String.format("%s%s", item.getCurrencySymbol(), item.getConvertedAddedCredit()));
                holder.getTxtPrice().setText(String.format("%s %s%s", item.getCurrencyCode(), item.getCurrencySymbol(), item.getConvertedAddedCredit()));
                holder.getTxtPulse().setTextColor(mContext.getResources().getColor(R.color.dial_green));
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

        public void addItems(List<PaymentHistoryItem> detailResponseList) {

            mPaymentHistoryItemList.clear();
            mPaymentHistoryItemList.addAll(detailResponseList);
            notifyDataSetChanged();
        }

        public void clearAll() {
            mPaymentHistoryItemList.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return mPaymentHistoryItemList.size();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Constants.CURRENT_BALANCE)) {
            mBalanceHelper.loadPaymentHistory(this);
        }
    }

}

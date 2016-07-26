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

import com.yo.android.R;
import com.yo.android.adapters.AbstractBaseAdapter;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.helpers.SpendDetailsViewHolder;
import com.yo.android.model.PaymentHistoryItem;
import com.yo.android.vox.BalanceHelper;

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
public class RechargeDetailsFragment extends BaseFragment implements Callback<List<PaymentHistoryItem>> {

    @Bind(R.id.txtEmpty)
    TextView txtEmpty;

    @Bind(R.id.progress)
    ProgressBar progress;

    @Bind(R.id.listView)
    ListView listView;
    @Inject
    BalanceHelper mBalanceHelper;
    private RechargeDetailsAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new RechargeDetailsAdapter(getActivity());
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
        showProgressDialog();
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

    private static class RechargeDetailsAdapter extends AbstractBaseAdapter<PaymentHistoryItem, SpendDetailsViewHolder> {

        public RechargeDetailsAdapter(Context context) {
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
        public void bindView(int position, SpendDetailsViewHolder holder, final PaymentHistoryItem item) {
            holder.getDate().setVisibility(View.GONE);
            holder.getDuration().setVisibility(View.GONE);
            holder.getTxtPhone().setText(item.getUpdatedAt());
            holder.getTxtPulse().setText(item.getStatus());
            holder.getTxtPulse().setTextColor(mContext.getResources().getColor(R.color.dial_green));
            holder.getTxtPrice().setText(String.format("$%s", item.getAddedCredit()));
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

package com.yo.android.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.adapters.AbstractBaseAdapter;
import com.yo.android.chat.ui.NonScrollListView;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.helpers.SpendDetailsViewHolder;
import com.yo.android.model.dialer.SubscribersList;
import com.yo.android.util.Util;
import com.yo.android.vox.BalanceHelper;

import java.io.InputStreamReader;
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

    private static final String PSTN = "PSTN";
    @Bind(R.id.txtEmpty)
    TextView txtEmpty;

    @Bind(R.id.progress)
    ProgressBar progress;

    @Bind(R.id.listView)
    NonScrollableListView listView;

    @Inject
    BalanceHelper mBalanceHelper;

    private SpentDetailsAdapter adapter;

    public static final String BALANCE_TRANSFER = "BalanceTransfer";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new SpentDetailsAdapter(getActivity());
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
        mBalanceHelper.loadSpentDetailsHistory(this);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                /* Check if the first item is already reached to top.*/
                if (listView.getFirstVisiblePosition() == 0) {
                    View firstChild = listView.getChildAt(0);
                    int topY = 0;
                    if (firstChild != null) {
                        topY = firstChild.getTop();
                    }


                }
            }
        });

    }

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        if (getActivity() != null) {
            if (response.body() != null) {
                dismissProgressDialog();
                try {
                    List<SubscribersList> detailResponseList = new Gson().fromJson(new InputStreamReader(response.body().byteStream()), new TypeToken<List<SubscribersList>>() {
                    }.getType());
                    if (detailResponseList != null && !detailResponseList.isEmpty()) {
                        List<SubscribersList> removedFreeSpents = new ArrayList<>();
                        for (SubscribersList item : detailResponseList) {
                            if (item.getCalltype().equals(PSTN) || item.getCalltype().equals(BALANCE_TRANSFER)) {
                                if (Float.valueOf(item.getCallcost()) != 0f) {
                                    removedFreeSpents.add(item);
                                }
                            }
                        }
                        adapter.addItems(removedFreeSpents);
                    }
                } catch (Exception e) {
                    mLog.w("SpendDetails", "onResponse", e);
                }
                showEmptyText();
            }
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        if (getActivity() != null) {
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

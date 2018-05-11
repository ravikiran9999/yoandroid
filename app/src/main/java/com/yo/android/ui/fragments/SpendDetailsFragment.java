package com.yo.android.ui.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yo.android.R;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.helpers.Helper;
import com.yo.android.helpers.SpendDetailsViewHolder;
import com.yo.android.model.SpendDetails;
import com.yo.android.model.dialer.SubscribersList;
import com.yo.android.provider.YoAppContactContract;
import com.yo.android.util.DateUtil;
import com.yo.android.util.Util;
import com.yo.android.vox.BalanceHelper;

import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;


import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpendDetailsFragment extends BaseFragment implements Callback<ResponseBody> {

    private static final String PSTN = "PSTN";
    public static final String BALANCE_TRANSFER = "BalanceTransfer";
    public static final String MAGAZINES = "Magzines";


    @BindView(R.id.txtEmpty)
    TextView txtEmpty;
    @BindView(R.id.progress)
    ProgressBar progress;
    @BindView(R.id.listView)
    RecyclerView listView;

    @Inject
    BalanceHelper mBalanceHelper;

    private SpentDetailsAdapter adapter;
    private Context mContext;


    public static final String AUTHORITY = YoAppContactContract.CONTENT_AUTHORITY;

    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/callLogs");


    public static SpendDetailsFragment newInstance() {
        return new SpendDetailsFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new SpentDetailsAdapter(mContext, new ArrayList<SubscribersList>());
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
        listView.setLayoutManager(new LinearLayoutManager(mContext));
        EventBus.getDefault().register(this);
        showProgressDialog();
        mBalanceHelper.loadSpentDetailsHistory(this);
    }

    public void onEventMainThread(SpendDetails action) {
        mBalanceHelper.loadSpentDetailsHistory(this);
    }

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        if (response.body() != null) {
            dismissProgressDialog();
            try {
                List<SubscribersList> detailResponseList = new Gson().fromJson(new InputStreamReader(response.body().byteStream()), new TypeToken<List<SubscribersList>>() {
                }.getType());
                if (detailResponseList != null && !detailResponseList.isEmpty()) {
                    List<SubscribersList> removedFreeSpents = new ArrayList<>();
                    for (SubscribersList item : detailResponseList) {
                        if (item.getCalltype().equals(PSTN) || item.getCalltype().equals(BALANCE_TRANSFER) || item.getCalltype().equals(MAGAZINES)) {

                            if (!TextUtils.isEmpty(item.getCallcost()))
                                removedFreeSpents.add(item);
                        }
                    }
                    java.util.Collections.sort(removedFreeSpents, new Comparator<SubscribersList>() {
                        @Override
                        public int compare(SubscribersList lhs, SubscribersList rhs) {
                            return rhs.getTime().compareTo(lhs.getTime());
                        }
                    });
                    adapter.addItems(removedFreeSpents);
                }
            } catch (Exception e) {
                mLog.w("SpendDetails", "onResponse", e);
            }
            showEmptyText();
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


    public class SpentDetailsAdapter extends RecyclerView.Adapter<SpendDetailsViewHolder> {
        private List<SubscribersList> mSubscribersList;
        private Context mContext;

        public SpentDetailsAdapter(Context context, List<SubscribersList> list) {
            mSubscribersList = list;
            mContext = context;
        }

        @Override
        public SpendDetailsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);

            View contactView = inflater.inflate(R.layout.frag_spent_list_row_item, parent, false);
            return new SpendDetailsViewHolder(contactView);
        }

        @Override
        public void onBindViewHolder(SpendDetailsViewHolder holder, int position) {
            final SubscribersList item = mSubscribersList.get(position);
            String date = null;
            try {
                date = DateUtil.formatDateMMMddyyyy(item.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            holder.getDate().setText(date);

            if (item.getDuration() != null) {
                if (item.getDuration().contains(":")) {
                    String[] tokens = item.getDuration().split(":");
                    int hours = Integer.parseInt(tokens[0]);
                    int minutes = Integer.parseInt(tokens[1]);
                    int seconds = Integer.parseInt(tokens[2]);
                    String duration = "";

                    if (hours == 0 && minutes == 0) {
                        duration = String.format("%02d secs", seconds);
                    } else if (hours == 0 && seconds == 0) {
                        duration = String.format("%02d mins", minutes, seconds);
                    } else if (hours == 0) {
                        duration = String.format("%02d mins %02d secs", minutes, seconds);
                    } else {
                        duration = String.format("%02d hrs %02d mins %02d secs", hours, minutes, seconds);
                    }
                    holder.getDuration().setText(duration);
                } else if (!item.getCalltype().equals(MAGAZINES)) {
                    String duration = "";
                    duration = Util.convertSecToHMmSs(Long.parseLong(item.getDuration()));
                    holder.getDuration().setText(duration);

                } else {
                    holder.getDuration().setText("");
                }
            } else {
                holder.getDuration().setText(item.getDuration());
            }

            String phoneName = Helper.getContactName(mContext, item.getDestination());
            if (phoneName != null) {
                holder.getTxtPhone().setText(phoneName);
            }

            holder.getTxtPrice().setText(mBalanceHelper.currencySymbolLookup(item.getCallcost()));
            holder.getTxtReason().setText(getReason(item.getCalltype()));
        }

        @Override
        public int getItemCount() {
            return mSubscribersList.size();
        }

        public void addItems(List<SubscribersList> detailResponseList) {
            mSubscribersList.clear();
            mSubscribersList.addAll(detailResponseList);
            notifyDataSetChanged();
        }

        public void clearAll() {
            mSubscribersList.clear();
            notifyDataSetChanged();
        }
    }

    private String getReason(String type) {
        switch (type) {
            case PSTN:
                return "Call";
            case BALANCE_TRANSFER:
                return "Transfer";
            case MAGAZINES:
                return MAGAZINES;
            default:
                return "";
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }
}
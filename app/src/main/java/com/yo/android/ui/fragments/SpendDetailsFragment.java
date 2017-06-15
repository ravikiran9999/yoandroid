package com.yo.android.ui.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
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
import com.yo.android.calllogs.CallLog;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.helpers.Helper;
import com.yo.android.helpers.SpendDetailsViewHolder;
import com.yo.android.model.dialer.SubscribersList;
import com.yo.android.provider.YoAppContactContract;
import com.yo.android.util.DateUtil;
import com.yo.android.util.Util;
import com.yo.android.vox.BalanceHelper;

import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    RecyclerView listView;

    @Inject
    BalanceHelper mBalanceHelper;

    private SpentDetailsAdapter adapter;

    public static final String BALANCE_TRANSFER = "BalanceTransfer";

    public static final String AUTHORITY = YoAppContactContract.CONTENT_AUTHORITY;

    /**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER = "date DESC";
    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/callLogs");


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new SpentDetailsAdapter(getActivity(), new ArrayList<SubscribersList>());
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
        showProgressDialog();
        mBalanceHelper.loadSpentDetailsHistory(this);


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
                                /*if (Float.valueOf(item.getCallcost()) != 0f) {
                                    removedFreeSpents.add(item);
                                }*/
                                if (!TextUtils.isEmpty(item.getCallcost()))
                                    removedFreeSpents.add(item);
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


    public static class SpentDetailsAdapter extends RecyclerView.Adapter<SpendDetailsViewHolder> {
        private List<SubscribersList> mSubscribersList;
        private Context mContext;

        public SpentDetailsAdapter(Context context, List<SubscribersList> list) {
            mSubscribersList = list;
            mContext = context;
        }

        @Override
        public SpendDetailsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the custom layout
            View contactView = inflater.inflate(R.layout.frag_spent_list_row_item, parent, false);

            // Return a new holder instance
            SpendDetailsViewHolder viewHolder = new SpendDetailsViewHolder(contactView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(SpendDetailsViewHolder holder, int position) {
            final SubscribersList item = mSubscribersList.get(position);

            // Set item views based on your views and data model
            //holder.getDate().setText(item.getTime());
            String modifiedTime = item.getTime().substring(0, item.getTime().lastIndexOf("."));
            holder.getDate().setText(modifiedTime);
            //                Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(modifiedTime);
            Date date = DateUtil.convertUtcToGmt(modifiedTime);
            holder.getDate().setText(new SimpleDateFormat("dd/MM/yyyy").format(date));
            if (item.getDuration() != null) {
                if (item.getDuration().contains(":")) {
                    String[] tokens = item.getDuration().split(":");
                    int hours = Integer.parseInt(tokens[0]);
                    int minutes = Integer.parseInt(tokens[1]);
                    int seconds = Integer.parseInt(tokens[2]);
                    String duration = "";
                    /*if (seconds > 30) {
                        minutes++;
                    }
                    if (hours == 0) {
                        duration = String.format("%02d mins", minutes);
                    } else {
                        duration = String.format("%02d hrs %02d mins", hours, minutes);
                    }*/

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
                } else {
                    String duration = "";
                    duration = Util.convertSecToHMmSs(Long.parseLong(item.getDuration()));
                    holder.getDuration().setText(duration);
                }
            } else {
                holder.getDuration().setText(item.getDuration());
            }
            String phoneName = Helper.getContactName(mContext, item.getDestination());
            if (phoneName != null) {
                holder.getTxtPhone().setText(phoneName);
            } else {
                final ContentResolver resolver = mContext.getContentResolver();
                Cursor c = null;
                try {
                    c = resolver.query(
                            CONTENT_URI,
                            null,
                            CallLog.Calls.NUMBER + " = " + item.getDestination(),
                            null,
                            DEFAULT_SORT_ORDER);
                    if (c == null || !c.moveToFirst()) {
                        String contactName = getContactName(item.getDestination());
                        if (!TextUtils.isEmpty(contactName)) {
                            holder.getTxtPhone().setText(contactName);
                        } else {
                            holder.getTxtPhone().setText(item.getDestination());
                        }
                    } else {
                        String name = c.getString(c.getColumnIndex(CallLog.Calls.CACHED_NAME));
                        if (!TextUtils.isEmpty(name)) {
                            holder.getTxtPhone().setText(name);
                        } else {
                            holder.getTxtPhone().setText(item.getDestination());
                        }
                    }
                } finally {
                    if (c != null) c.close();
                }
            }
            //holder.getTxtPrice().setText("US $ " + item.getCallcost());
            holder.getTxtPrice().setText(item.getCallcost());
            holder.getTxtReason().setText(item.getCalltype());

            //Todo remove this block
            /*try {
                DecimalFormat df = new DecimalFormat("0.000");
                String format = df.format(Double.valueOf(item.getCallcost()));
                holder.getTxtPrice().setText("US $ " + format);
            } catch (Exception e) {
                e.printStackTrace();
            }*/

            /*holder.getArrow().setOnClickListener(new View.OnClickListener() {
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
            }*/
        }

        @Override
        public int getItemCount() {
            return mSubscribersList.size();
        }

        public void addItems(List<SubscribersList> detailResponseList) {
            mSubscribersList.addAll(detailResponseList);
            notifyDataSetChanged();
        }

        public void clearAll() {
            mSubscribersList.clear();
            notifyDataSetChanged();
        }

        public String getContactName(final String phoneNumber) {
            Uri uri;
            String[] projection;
            Uri mBaseUri = Contacts.Phones.CONTENT_FILTER_URL;
            projection = new String[]{android.provider.Contacts.People.NAME};
            try {
                Class<?> c = Class.forName("android.provider.ContactsContract$PhoneLookup");
                mBaseUri = (Uri) c.getField("CONTENT_FILTER_URI").get(mBaseUri);
                projection = new String[]{"display_name"};
            } catch (Exception e) {
            }


            uri = Uri.withAppendedPath(mBaseUri, Uri.encode(phoneNumber));
            Cursor cursor = mContext.getContentResolver().query(uri, projection, null, null, null);

            String contactName = "";

            if (cursor.moveToFirst()) {
                contactName = cursor.getString(0);
            }

            cursor.close();
            cursor = null;

            return contactName;
        }

    }

}

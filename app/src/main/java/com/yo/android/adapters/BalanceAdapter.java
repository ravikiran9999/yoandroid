package com.yo.android.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.helpers.BalanceViewHolder;
import com.yo.android.helpers.DenominationViewHolder;
import com.yo.android.helpers.EmptyViewHolder;
import com.yo.android.helpers.SeperatorViewHolder;
import com.yo.android.model.MoreData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rdoddapaneni on 6/20/2017.
 */

public class BalanceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_MORE_ITEMS = 1;
    private static final int VIEW_TYPE_DENOMINATION_ITEMS = 2;
    private static final int VIEW_TYPE_SEPERATOR = 3;
    private static final int VIEW_TYPE_PACKAGE_ITEMS = 4;

    private Context mContext;
    private ArrayList<Object> mData;
    private Fragment mFragment;
    private LayoutInflater mInflater;
    private MoreItemListener mMoreItemListener;

    public interface MoreItemListener {
        void onRowSelected(int position);
    }

    public BalanceAdapter(Context context, ArrayList<Object> data, ArrayList<Object> denominationData, Fragment fragment) {
        mData = data;
        mFragment = fragment;
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_MORE_ITEMS:
                return new BalanceViewHolder(mInflater.inflate(R.layout.item_with_options, parent, false));

            case VIEW_TYPE_DENOMINATION_ITEMS:
                if (!BuildConfig.NEW_YO_CREDIT_SCREEN) {
                    return new DenominationViewHolder(mContext, mInflater.inflate(R.layout.item_inner_recycler_view, parent, false), mFragment);
                }
            case VIEW_TYPE_SEPERATOR:
                return new SeperatorViewHolder(mInflater.inflate(R.layout.line_margin_medium, parent, false));
            /*case VIEW_TYPE_PACKAGE_ITEMS:
                return new ItemPackageViewHolder(mContext, mInflater.inflate(R.layout.item_inner_recycler_view, parent, false), mFragment);*/
            default:
                return new EmptyViewHolder(mInflater.inflate(R.layout.empty_view, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final int viewType = getItemViewType(position);
        switch (viewType) {
            case VIEW_TYPE_MORE_ITEMS:
                final BalanceViewHolder balanceViewHolder = (BalanceViewHolder) holder;
                balanceViewHolder.bind(mData.get(position));
                balanceViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = balanceViewHolder.getAdapterPosition();
                        if (position < 0) {
                            return;
                        }
                        if (mMoreItemListener != null) {
                            mMoreItemListener.onRowSelected(position);
                        }
                    }
                });
                break;
            case VIEW_TYPE_DENOMINATION_ITEMS:
                if (mData != null && mData.get(position) != null && mData.get(position) instanceof ArrayList) {
                    ArrayList<Object> mDenominationData = (ArrayList<Object>) mData.get(position);
                    final DenominationViewHolder denominationViewHolder = (DenominationViewHolder) holder;
                    denominationViewHolder.bind(mDenominationData);
                    denominationViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int position = denominationViewHolder.getAdapterPosition();
                            if (position < 0) {
                                return;
                            }
                            if (mMoreItemListener != null) {
                                mMoreItemListener.onRowSelected(position);
                            }
                        }
                    });
                }
                break;


        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        Object item = mData.get(position);
        if (item instanceof MoreData) {
            return VIEW_TYPE_MORE_ITEMS;
        } else if (item instanceof ArrayList) {
            return VIEW_TYPE_DENOMINATION_ITEMS;
        } else if (item instanceof Integer) {
            return VIEW_TYPE_SEPERATOR;
        } else if (item instanceof List) {
            return VIEW_TYPE_PACKAGE_ITEMS;
        }
        return super.getItemViewType(position); // Will never fall under this
    }

    public void setMoreItemListener(MoreItemListener listener) {
        mMoreItemListener = listener;
    }

    public void addItems(ArrayList<Object> data) {
        if (mData.size() > 0) {
            mData.clear();
            mData = data;
        } else {
            mData = data;
        }
        notifyDataSetChanged();
    }

    public MoreData getItem(int position) {
        if (mData != null && mData.size() > 0) {
            return (MoreData) mData.get(position);
        }
        return new MoreData();
    }
}

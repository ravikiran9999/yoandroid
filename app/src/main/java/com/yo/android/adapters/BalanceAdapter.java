package com.yo.android.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yo.android.R;
import com.yo.android.helpers.BalanceViewHolder;
import com.yo.android.helpers.DenominationViewHolder;
import com.yo.android.helpers.EmptyViewHolder;
import com.yo.android.helpers.MenuViewHolder;
import com.yo.android.model.MenuData;
import com.yo.android.model.MoreData;
import com.yo.android.model.denominations.Denominations;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rdoddapaneni on 6/20/2017.
 */

public class BalanceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_MORE_ITEMS = 1;
    private static final int VIEW_TYPE_DENOMINATION_ITEMS = 2;

    private Context mContext;
    private ArrayList<Object> mData;
    private ArrayList<Object> mDenominationData;
    private Fragment mFragment;
    private LayoutInflater mInflater;
    private MoreItemListener mMoreItemListener;

    public interface MoreItemListener {
        void onRowSelected(int position);
    }

    public BalanceAdapter(Context context, ArrayList<Object> data, ArrayList<Object> denominationData, Fragment fragment) {
        mData = data;
        mFragment = fragment;
        mDenominationData = new ArrayList<>();
        if (denominationData != null) {
            mDenominationData.addAll(denominationData);
        }
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_MORE_ITEMS:
                return new BalanceViewHolder(mInflater.inflate(R.layout.item_with_options, parent, false));
            case VIEW_TYPE_DENOMINATION_ITEMS:
                return new DenominationViewHolder(mContext, mInflater.inflate(R.layout.item_inner_recycler_view, parent, false), mFragment);
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
                balanceViewHolder.bind((MoreData) mData.get(position));
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
                if (mDenominationData != null) {
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
        }
        return super.getItemViewType(position); // Will never fall under this
    }

    public void setMoreItemListener(MoreItemListener listener) {
        mMoreItemListener = listener;
    }

}

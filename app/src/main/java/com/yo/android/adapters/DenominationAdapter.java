package com.yo.android.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yo.android.R;
import com.yo.android.helpers.DenominationItemViewHolder;
import com.yo.android.model.denominations.Denominations;

import java.util.ArrayList;

/**
 * Created by rdoddapaneni on 6/22/2017.
 */

public class DenominationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Object> mData;
    private LayoutInflater mInflater;
    private DenominationItemListener denominationItemListener;

    public interface DenominationItemListener {
        void onItemSelected(String sku, float price);
    }

    public DenominationAdapter(Context context, ArrayList<Object> data) {
        mData = data;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DenominationItemViewHolder(mInflater.inflate(R.layout.item_with_payment_options, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final DenominationItemViewHolder denominationItemViewHolder = (DenominationItemViewHolder) holder;
        denominationItemViewHolder.bind((Denominations) mData.get(position));
        denominationItemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = denominationItemViewHolder.getAdapterPosition();
                if (position < 0) {
                    return;
                }
                if (denominationItemListener != null) {
                    Denominations item = (Denominations)mData.get(position);
                    denominationItemListener.onItemSelected(item.getProductID(), item.getDenomination());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setDenominationItemListener(DenominationItemListener listener) {
        denominationItemListener = listener;
    }
}

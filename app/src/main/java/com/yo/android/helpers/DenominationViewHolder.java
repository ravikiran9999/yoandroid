package com.yo.android.helpers;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.yo.android.R;
import com.yo.android.adapters.DenominationAdapter;
import com.yo.android.ui.fragments.CreditAccountFragment;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by rdoddapaneni on 6/22/2017.
 */

public class DenominationViewHolder extends RecyclerView.ViewHolder implements DenominationAdapter.DenominationItemListener{


    @Bind(R.id.inner_view)
    RecyclerView recyclerView;

    private Fragment mFragment;
    private Context mContext;

    public DenominationViewHolder(Context context, View itemView, Fragment fragment) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        mContext = context;
        mFragment = fragment;
    }

    public void bind(ArrayList<Object> data) {
        ArrayList denominationsList = (ArrayList) data;
        DenominationAdapter denominationAdapter = new DenominationAdapter(mContext, denominationsList);
        denominationAdapter.setDenominationItemListener(this);
        recyclerView.setAdapter(denominationAdapter);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new GridLayoutManager(mContext, 3));

    }

    @Override
    public void onItemSelected(String sku, float price) {
        if(mFragment instanceof CreditAccountFragment) {
            ((CreditAccountFragment)mFragment).addGooglePlayBalance(sku, price);
        }

    }
}

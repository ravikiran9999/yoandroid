package com.yo.android.helpers;

import android.view.View;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractViewHolder;


import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Ramesh on 9/7/16.
 */
public class CallRatesCountryViewHolder extends AbstractViewHolder {
    @BindView(R.id.txt_country)
    TextView country;
    @BindView(R.id.local_call_rate)
    TextView localCallRate;
    @BindView(R.id.global_call_rate)
    TextView globalCallRate;


    public CallRatesCountryViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public TextView getLocalCallRateView() {
        return localCallRate;
    }

    public TextView getGlobalCallRateView() {
        return globalCallRate;
    }

    public TextView getCountryView() {
        return country;
    }

}

package com.yo.android.helpers;

import android.view.View;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractViewHolder;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Ramesh on 9/7/16.
 */
public class CallRatesCountryViewHolder extends AbstractViewHolder {
    @Bind(R.id.txt_country)
    TextView country;

    @Bind(R.id.txt_call_rate)
    TextView callRate;

    public CallRatesCountryViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public TextView getCallRateView() {
        return callRate;
    }

    public TextView getCountryView() {
        return country;
    }
}

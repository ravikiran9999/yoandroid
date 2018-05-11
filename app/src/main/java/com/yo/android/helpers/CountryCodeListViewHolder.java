package com.yo.android.helpers;

/**
 * Created by mtuity-desk-13 on 23/1/17.
 */

import android.view.View;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractViewHolder;


import butterknife.BindView;
import butterknife.ButterKnife;

import android.view.View;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractViewHolder;


import butterknife.ButterKnife;

/**
 * Created by Ramesh on 9/7/16.
 */
public class CountryCodeListViewHolder extends AbstractViewHolder {
    @BindView(R.id.country_name_txt)
    TextView countryName;

    @BindView(R.id.country_code_txt)
    TextView countrycode;

    public CountryCodeListViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public TextView getCountryNameView() {
        return countryName;
    }

    public TextView getCountrycodeView() {
        return countrycode;
    }
}
package com.yo.android.ui.fragments;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractViewHolder;

/**
 * Created by rajesh on 22/11/16.
 */
public class CurrencyViewHolder extends AbstractViewHolder {
    protected Button button;

    public CurrencyViewHolder(View view) {
        super(view);
        button = (Button) view.findViewById(R.id.btn1);
    }

    public Button getButtonView() {
        return button;
    }


}
